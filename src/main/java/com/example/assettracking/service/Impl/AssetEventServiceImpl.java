package com.example.assettracking.service.Impl;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.example.assettracking.model.AssetEvent;
import com.example.assettracking.model.AssetEventDTO;
import com.example.assettracking.service.BL.AssetEventServiceBL;
import com.example.assettracking.service.BL.CustomCacheServiceBL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@Service
public class AssetEventServiceImpl implements AssetEventServiceBL {

	private final DynamoDBMapper dynamoDBMapper;

	@Autowired
	private CustomCacheServiceBL redisCache;
	

	Logger logger = LoggerFactory.getLogger(AssetEventServiceImpl.class);

	public AssetEventServiceImpl(DynamoDBMapper dynamoDBMapper) {
		this.dynamoDBMapper = dynamoDBMapper;
		
	}

	// Use case 1
	// @Cacheable(value = "assetEvents", key = "#asset")
	public Page<AssetEventDTO> getEventsForAssetAndTimeRange(int asset, String start, String end, int page, int size) {
		// Create a PageRequest object to handle pagination
		PageRequest pageRequest = PageRequest.of(page, size);

		DynamoDBScanExpression scanExpression = createScanExpression(pageRequest);

		scanExpression.withFilterConditionEntry("asset", new Condition().withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue().withN(asset + "")));
		scanExpression.withFilterConditionEntry("createdAt",
				new Condition().withComparisonOperator(ComparisonOperator.BETWEEN).withAttributeValueList(
						new AttributeValue().withN(toUnixTimeMillis(start) + ""),
						new AttributeValue().withN(toUnixTimeMillis(end) + "")));

		List<AssetEvent> paginatedScanList = dynamoDBMapper.scan(AssetEvent.class, scanExpression);

		List<AssetEventDTO>  eventsDTOsList = convertToDTO(paginatedScanList);
		
		return implementPagination(eventsDTOsList, pageRequest);
		
	}

	// Use case 2
	@Override
	public AssetEventDTO getAssetEventsByEventId(String id) {
		String KEY = "getAssetEventsByEventId-" + id;
		AssetEventDTO assetEventDTO = (AssetEventDTO) redisCache.get(KEY);
		
		if (assetEventDTO ==null) {
			assetEventDTO = scanByFieldS("id", id).stream().findFirst().map(this::convertToDTO).orElse(null);
			redisCache.put(KEY, assetEventDTO, 600, TimeUnit.SECONDS);
		}
		return assetEventDTO;
		
	}

	// Use case 3
	@Override
	public Page<AssetEventDTO> getLatestEventForAllAssets(int page, int size) {

		Set<Long> assetIds = getAllAssetId();

		logger.debug("assetID" + assetIds.toString());
		PageRequest pageRequest = PageRequest.of(page, size);

		List<AssetEventDTO> latestEventsDTOs = new ArrayList<AssetEventDTO>();

		for (Long a : assetIds) {

			AssetEventDTO assetDTO = getLatestEventByAssetID(a);
			if (assetDTO != null) {
				logger.debug("asset.toAssetEventDTO().toString()" + assetDTO.toString());
				latestEventsDTOs.add(assetDTO);
			}

		}
		return implementPagination(latestEventsDTOs, pageRequest);
		

	}

	// Use Case 4
	@Override
	public Page<AssetEventDTO> getEventsByAssetAndTrip(int asset, int trip, int page, int size) {
		// Create a PageRequest object to handle pagination

		String KEY = "getEventsByAssetAndTrip-" + asset + "-" + trip;
		@SuppressWarnings("unchecked")
		List<AssetEventDTO> eventsDTOsList = (List<AssetEventDTO>) redisCache.get(KEY);
		PageRequest pageRequest = PageRequest.of(page, size);

		if (eventsDTOsList == null) {

			DynamoDBScanExpression scanExpression = createScanExpression(pageRequest);

			scanExpression.addFilterCondition("asset", new Condition().withComparisonOperator(ComparisonOperator.EQ)
					.withAttributeValueList(new AttributeValue().withN(asset + "")));
			scanExpression.addFilterCondition("trip", new Condition().withComparisonOperator(ComparisonOperator.EQ)
					.withAttributeValueList(new AttributeValue().withN(trip + "")));

			List<AssetEvent> paginatedScanList = dynamoDBMapper.scan(AssetEvent.class, scanExpression);
			logger.debug("paginatedScanList.size(123):" + paginatedScanList.size());

			eventsDTOsList = convertToDTO(paginatedScanList);

			redisCache.put(KEY, eventsDTOsList, 60, TimeUnit.SECONDS);
		}

		
		return implementPagination(eventsDTOsList, pageRequest);

	}

	private Page<AssetEventDTO> implementPagination(List<AssetEventDTO> eventsDTOsList, PageRequest pageRequest) {

		// Implement pagination
		int total = eventsDTOsList.size();
		logger.debug("eventsDTOsList.size():" + eventsDTOsList.size());
		int start = Math.min((int) (pageRequest.getOffset() - pageRequest.getPageSize()), total);
		int end = Math.min((int) pageRequest.getOffset(), total);

		logger.debug("pageRequest - getOffset:" + pageRequest.getOffset() + ", getPageNumber:"
				+ pageRequest.getPageNumber() + ", getPageSize:" + pageRequest.getPageSize());
		logger.debug("start:" + start + ", end:" + end);
		logger.debug("eventsDTOsList.size()" + eventsDTOsList.size());
		List<AssetEventDTO> paginatedList = eventsDTOsList.subList(start, end);
		logger.debug("paginatedList.size()" + paginatedList.size());

		return new PageImpl<>(paginatedList, pageRequest, total);

	}

	@Cacheable(value = "latestEventByAssetID", key = "#assetId")
	private AssetEventDTO getLatestEventByAssetID(long assetId) {

		String KEY = "latestEventByAssetID-" + assetId;
		AssetEventDTO assetEventDTO = (AssetEventDTO) redisCache.get(KEY);

		if (assetEventDTO == null) {
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			scanExpression.withFilterConditionEntry("asset",
					new Condition().withComparisonOperator(ComparisonOperator.EQ)
							.withAttributeValueList(new AttributeValue().withN(assetId + "")));
			scanExpression.withLimit(1);

			PaginatedScanList<AssetEvent> scanResult = dynamoDBMapper.scan(AssetEvent.class, scanExpression);

			AssetEvent asset = null;
			if (!scanResult.isEmpty()) {
				asset = scanResult.get(0);
			}

			if (asset!=null) {
				assetEventDTO  = asset.toAssetEventDTO();						
			}
			redisCache.put(KEY, assetEventDTO, 60, TimeUnit.SECONDS);
		}
		return assetEventDTO;

	}

	@Cacheable(value = "allAssetId")
	private Set<Long> getAllAssetId() {

		String KEY = "allAssetId";
		@SuppressWarnings("unchecked")
		Set<Long> assetIds = (Set<Long>) redisCache.get(KEY);

		if (assetIds == null) {

			// redisCacheManager.cac
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			List<AssetEvent> paginatedScanList = dynamoDBMapper.scan(AssetEvent.class, scanExpression);

			assetIds = paginatedScanList.stream().map(asset -> asset.getAsset()).collect(Collectors.toSet());

			redisCache.put(KEY, assetIds, 300, TimeUnit.SECONDS);

		}
		return assetIds;

	}

	private DynamoDBScanExpression createScanExpression(PageRequest pageRequest) {

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		long scanTo = pageRequest.getOffset() + (2 * pageRequest.getPageSize());
		scanExpression.setLimit((int) Math.min(scanTo, Integer.MAX_VALUE));
		return scanExpression;

	}



	private List<AssetEvent> scanByFieldS(String fieldName, String value) {
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		scanExpression.addFilterCondition(fieldName, new Condition().withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue().withS(value)));
		return dynamoDBMapper.scan(AssetEvent.class, scanExpression);
	}

//	private Page<AssetEventDTO> toPageAssetEventDTO(PageRequest pageRequest, DynamoDBScanExpression scanExpression,
//			List<AssetEvent> paginatedScanList) {
//
//		Iterator<AssetEvent> iterator = paginatedScanList.iterator();
//
//		if (pageRequest.getOffset() > 0) {
//			long processedCount = scanThroughResults(iterator, pageRequest.getOffset());
//			if (processedCount < pageRequest.getOffset())
//				return new PageImpl<>(new ArrayList<AssetEventDTO>());
//		}
//
//		// Scan ahead to retrieve the next page count
//		List<AssetEvent> results = readPageOfResults(iterator, pageRequest.getPageSize());
//
//		int totalCount = dynamoDBMapper.count(AssetEvent.class, scanExpression);
//
//		return new PageImpl<>(convertToDTO(results), pageRequest, totalCount);
//
//	}
//	private long scanThroughResults(Iterator<AssetEvent> paginatedScanListIterator, long resultsToScan) {
//		long processed = 0;
//		while (paginatedScanListIterator.hasNext() && processed < resultsToScan) {
//			paginatedScanListIterator.next();
//			processed++;
//		}
//		return processed;
//	}
//
//	private List<AssetEvent> readPageOfResults(Iterator<AssetEvent> paginatedScanListIterator, int pageSize) {
//		int processed = 0;
//		List<AssetEvent> resultsPage = new ArrayList<>();
//		while (paginatedScanListIterator.hasNext() && processed < pageSize) {
//			resultsPage.add(paginatedScanListIterator.next());
//			processed++;
//		}
//		return resultsPage;
//	}

	private AssetEventDTO convertToDTO(AssetEvent assetEvent) {

		return assetEvent.toAssetEventDTO();

	}

	private List<AssetEventDTO> convertToDTO(List<AssetEvent> assetEvent) {

		return assetEvent.stream().map(this::convertToDTO).collect(Collectors.toList());

	}

	private static long toUnixTimeMillis(String dateTimeString) {

		// The ISO 8601 date string
		//String isoDate = "2019-01-04T07:18:30Z";

		// Parse the ISO 8601 string to a ZonedDateTime
		ZonedDateTime zdt = ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);

		// Convert ZonedDateTime to Instant
		Instant instant = zdt.toInstant();

		// Get the Unix time as long
		long unixTimeMillis = instant.toEpochMilli();

		return unixTimeMillis;

	}

}

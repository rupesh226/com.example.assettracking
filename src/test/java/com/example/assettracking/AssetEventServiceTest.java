package com.example.assettracking;

import com.example.assettracking.model.AssetEventDTO;
import com.example.assettracking.service.BL.AssetEventServiceBL;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AssetEventServiceTest {

	@Autowired
	private AssetEventServiceBL assetEventService;

	@Test
	@DisplayName("Use Case 1: Get Events for a given asset and time range")
	public void testGetEventsForAssetAndTimeRange() {
		int asset = 1;
		String start = "2019-10-26T05:40:10Z";
		String end = "2019-10-28T05:42:00Z";
		int page = 1;
		int size = 1000;

//        Pageable pageable = PageRequest.of(page, size);
//        AssetEventDTO assetEvent = new AssetEventDTO();
//        Page<AssetEventDTO> assetEventsPage = new PageImpl<>(Collections.singletonList(assetEvent), pageable, 203);
//
//        when(assetEventService.getEventsForAssetAndTimeRange(asset, start, end, page, size))
//                .thenReturn(assetEventsPage);

		Page<AssetEventDTO> result = assetEventService.getEventsForAssetAndTimeRange(asset, start, end, page, size);

		assertEquals(203, result.getNumberOfElements());
	}

	@Test
	@DisplayName("Use Case 3: Get Latest events for all Asset")
	public void testGetLatestEventForAllAssets() {
		int page = 1;
		int size = 1000;

//        Pageable pageable = PageRequest.of(page, size);
//        AssetEventDTO assetEvent = new AssetEventDTO();
//        Page<AssetEventDTO> assetEventsPage = new PageImpl<>(Collections.singletonList(assetEvent), pageable, 1);
//
//        when(assetEventService.getLatestEventForAllAssets(page, size)).thenReturn(assetEventsPage);

		Page<AssetEventDTO> result = assetEventService.getLatestEventForAllAssets(page, size);

		assertEquals(3, result.getNumberOfElements());
	}

	@Test
	@DisplayName("Use Case 2: Get event by EventID")
	public void testGetAssetEventsByEventId() {

		int page = 1;
		int size = 1000;
		String id = "";
		Page<AssetEventDTO> result = assetEventService.getLatestEventForAllAssets(page, size);

		for (AssetEventDTO assetEventDTO : result) {
			id = assetEventDTO.getId();
			break;
		}

		AssetEventDTO assetEvent = new AssetEventDTO();
		assetEvent.setId(id);

//        when(assetEventService.getAssetEventsByEventId(id)).thenReturn(assetEvent);

		AssetEventDTO result_ = assetEventService.getAssetEventsByEventId(id);

		assertEquals(id, result_.getId());
	}

	@Test
	@DisplayName("Use Case 4: [Optional] Get Events for given asset and trip")
	public void testGetEventsByAssetAndTrip() {
		int asset = 1;
		int trip = 646;
		int page = 1;
		int size = 1000;

//        Pageable pageable = PageRequest.of(page, size);
//        AssetEventDTO assetEvent = new AssetEventDTO();
//        Page<AssetEventDTO> assetEventsPage = new PageImpl<>(Collections.singletonList(assetEvent), pageable, 1);
//
//        when(assetEventService.getEventsByAssetAndTrip(asset, trip, page, size)).thenReturn(assetEventsPage);

		Page<AssetEventDTO> result = assetEventService.getEventsByAssetAndTrip(asset, trip, page, size);

		assertEquals(118, result.getNumberOfElements());
	}
}

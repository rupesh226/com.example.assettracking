package com.example.assettracking.service.BL;


import org.springframework.data.domain.Page;

import com.example.assettracking.model.AssetEventDTO;

public interface AssetEventServiceBL {
	
	
		
	
	//For given asset and time range return all appropriate events.
	Page<AssetEventDTO> getEventsForAssetAndTimeRange(int asset, String start, String end,
				int page, int size);
		
	// Query for single event by an Id.
	AssetEventDTO getAssetEventsByEventId(String id);
	
	//Return latest event for all assets.
	Page<AssetEventDTO> getLatestEventForAllAssets(int page, int size);

	
	//(Optional) For given asset and trip return all appropriate events
	Page<AssetEventDTO> getEventsByAssetAndTrip(int asset, int trip, int page, int size);

}

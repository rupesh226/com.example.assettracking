package com.example.assettracking.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.assettracking.model.AssetEventDTO;
import com.example.assettracking.service.BL.AssetEventServiceBL;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Asset Event Web Controller", description = "Asset Event Web Controller Management APIs")
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/events")
public class EventController {

	private final AssetEventServiceBL assetEventServiceBL;

	public EventController(AssetEventServiceBL assetEventServiceBL) {
		this.assetEventServiceBL = assetEventServiceBL;
	}

	@Operation(summary = "Use Case 1: Get events for a given asset and time range", description = "Returns a paginated list of events for the specified asset and time range.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
		@ApiResponse(responseCode = "400", description = "Invalid input parameters"),
		@ApiResponse(responseCode = "404", description = "Events not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("/asset/{asset}")
	public Page<AssetEventDTO> getEventsForAsset(@PathVariable int asset, @RequestParam String start,
			@RequestParam String end, @RequestParam int page, @RequestParam int size) {
		return assetEventServiceBL.getEventsForAssetAndTimeRange(asset, start, end, page, size);
	}

	@Operation(summary = "Use Case 2: Get event by ID", description = "Returns an event for the specified event ID.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved event"),
		@ApiResponse(responseCode = "404", description = "Event not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("/{id}")
	public AssetEventDTO getEventById(@PathVariable String id) {
		return assetEventServiceBL.getAssetEventsByEventId(id);
	}

	@Operation(summary = "Use Case 3: Get latest events for all assets", description = "Returns a paginated list of the latest events for all assets.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved latest events"),
		@ApiResponse(responseCode = "400", description = "Invalid input parameters"),
		@ApiResponse(responseCode = "404", description = "Events not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("/latest")
	public Page<AssetEventDTO> getLatestEventForAllAssets(@RequestParam int page, @RequestParam int size) {
		return assetEventServiceBL.getLatestEventForAllAssets(page, size);
	}

	
	@Operation(summary = "Use Case 4 (Optional): Get events for a given asset and trip", description = "Returns a paginated list of events for the specified asset and trip.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
		@ApiResponse(responseCode = "400", description = "Invalid input parameters"),
		@ApiResponse(responseCode = "404", description = "Events not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("/asset")
	public Page<AssetEventDTO> getEventsByAssetAndTrip(@RequestParam int asset, @RequestParam int trip,
			@RequestParam int page, @RequestParam int size) {
		return assetEventServiceBL.getEventsByAssetAndTrip(asset, trip, page, size);
	}

}

package com.example.assettracking.model;


import lombok.Data;

@Data
public class AssetEventDTO {

	private String id;

	private long asset;

	private String createdAt;

	private long trip;

	private Double x;

	private Double y;
	
	public AssetEventDTO () {
	}
	public AssetEventDTO (AssetEvent e) {
		this.id = e.getId();
		this.asset = e.getAsset();
		this.createdAt = e.getCreatedAtISOStr();
		this.trip = e.getTrip();
		this.x = e.getX();
		this.y =e.getY();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getAsset() {
		return asset;
	}

	public void setAsset(long asset) {
		this.asset = asset;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public long getTrip() {
		return trip;
	}

	public void setTrip(long trip) {
		this.trip = trip;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "AssetEventDTO [id=" + id + ", asset=" + asset + ", createdAt=" + createdAt + ", trip=" + trip + ", x="
				+ x + ", y=" + y + "]";
	}
	
	

}

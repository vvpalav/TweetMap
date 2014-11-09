package com;

public class TweetNode {

	private int id;
	private String latitude;
	private String longitude;
	private String timestamp;
	
	public TweetNode(int id, String lon, String lat, String timestamp){
		this.id = id;
		this.latitude = lat;
		this.longitude = lon;
		this.timestamp = timestamp;
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getLongitude(){
		return this.longitude;
	}
	
	public String getLatitude(){
		return this.latitude;
	}
	
	public String getTimestamp(){
		return this.timestamp;
	}
	
	public String toString() {
		return "Tweet Id: " + id + " Latitude: " + latitude + " Longitude: "
				+ longitude + " Timestamp: " + timestamp;
	}
}

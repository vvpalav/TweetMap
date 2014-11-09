package com;

import twitter4j.JSONException;
import twitter4j.JSONObject;

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
	
	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("latitude", getLatitude());
			json.put("longitude", getLongitude());
			json.put("timestamp", getTimestamp());
			json.put("id", getId());
		} catch (JSONException e) {
			System.out.println("Failed to convert TweetNode to JSONObject");
			e.printStackTrace();
		}
		return json;
	}
}

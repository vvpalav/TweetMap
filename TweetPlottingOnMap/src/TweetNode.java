import java.util.Date;

import com.amazonaws.util.json.JSONObject;

import com.amazonaws.util.json.JSONException;

public class TweetNode {

	private long id;
	private double latitude;
	private double longitude;
	private Date timestamp;
	private String username;
	private String text;
	private String sentiment;

	public TweetNode(long id, String user, String text, double lat, double lon, Date timestamp) {
		this.id = id;
		this.username = user;
		this.text = text;
		this.latitude = lat;
		this.longitude = lon;
		this.timestamp = timestamp;
	}
	
	public TweetNode(JSONObject json){
		try {
			this.id = json.getLong("id");
			this.latitude = json.getDouble("latitude");
			this.longitude = json.getDouble("longitude");
			this.text = json.getString("text");
			this.username = json.getString("username");
			this.timestamp = new Date(json.getLong("timestamp"));
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	public void setSentiment(String str){
		this.sentiment = str;
	}

	public String getSentiment(){
		return this.sentiment;
	}
	
	public long getId() {
		return this.id;
	}

	public String getUsername() {
		return username;
	}
	
	public String getText() {
		return text;
	}
	
	public double getLongitude() {
		return this.longitude;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public String toString() {
		return "Tweet Id: " + id + " username: " + username + " text: " + text
				+ " Latitude: " + latitude + " Longitude: " + longitude
				+ " Timestamp: " + timestamp.toString();
	}
	
	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("id", this.id);
			json.put("latitude", this.latitude);
			json.put("longitude", this.longitude);
			json.put("text", this.text);
			json.put("username", this.username);
			json.put("timestamp", this.timestamp.getTime());
			if(this.sentiment != null){
				json.put("sentiment", this.sentiment);
			}
		} catch (JSONException e){
			e.printStackTrace();
		}
		return json;
	}

	public String getLatLongValue() {
		return latitude + " " + longitude + " " + text;
	}
}
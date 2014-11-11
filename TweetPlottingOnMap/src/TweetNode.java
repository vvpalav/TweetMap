import java.util.Date;

public class TweetNode {

	private long id;
	private double latitude;
	private double longitude;
	private Date timestamp;
	private String username;
	private String text;

	public TweetNode(long id, String user, String text, double lat, double lon, Date timestamp) {
		this.id = id;
		this.username = user;
		this.text = text;
		this.latitude = lat;
		this.longitude = lon;
		this.timestamp = timestamp;
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

	public String getValue() {
		return latitude + " " + longitude;
	}
}
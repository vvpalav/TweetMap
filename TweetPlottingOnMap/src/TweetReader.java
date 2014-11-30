import java.util.Date;
import java.util.LinkedList;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public final class TweetReader implements StatusListener {

	private final DBHelper db;
	private final ConfigurationBuilder cb;
	private LinkedList<Long> list;
	private TwipMapSQSHandler sqs;

	public static void main(String[] args) throws TwitterException,
			InterruptedException {
		TwipMapSQSHandler sqs = TwipMapSQSHandler.initializeTwipMapSQSHandler();
		TweetReader reader = new TweetReader(sqs);
		try {
			// reader.db.deleteAllTweetsFromDB();
			TwitterStream twitterStream = new TwitterStreamFactory(
					reader.cb.build()).getInstance();
			twitterStream.addListener(reader);
			twitterStream.sample();
			while (reader.db.getTweetCount() <= 200) {
				Thread.sleep(5000);
			}
			twitterStream.removeListener(reader);
		} finally {
			Thread.sleep(6000);
			reader.db.close();
			System.exit(0);
		}
	}

	public TweetReader(TwipMapSQSHandler sqs) {
		this.list = new LinkedList<Long>();
		this.sqs = sqs;
		this.db = new DBHelper(this.sqs);
		this.cb = new ConfigurationBuilder();
		this.cb.setDebugEnabled(true)
				.setOAuthConsumerKey(Configuration.twitterConsumerKey)
				.setOAuthConsumerSecret(Configuration.twitterConsumerSecret)
				.setOAuthAccessToken(Configuration.twitterAccessKey)
				.setOAuthAccessTokenSecret(Configuration.twitterTokenPrivate);
	}

	public void onStatus(Status status) {
		if (status == null || status.getGeoLocation() == null)
			return;

		System.out.println("@" + status.getUser().getScreenName() + " - "
				+ status.getText());
		double latitude = status.getGeoLocation().getLatitude();
		double longitude = status.getGeoLocation().getLongitude();
		long id = status.getId();
		Date timestamp = status.getCreatedAt();
		String user = status.getUser().getScreenName();
		String text = status.getText();
		TweetNode node = new TweetNode(id, user, text, latitude, longitude, timestamp);
		if (text.contains("@"))
			db.insertTweetIntoDB(node);
	}

	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		if (statusDeletionNotice == null)
			return;
		list.add(statusDeletionNotice.getStatusId());
		if (list.size() >= 100) {
			System.out.println("deleting Tweets from DB");
			db.deleteTweetWithStatusId(list);
			list.clear();
		}
	}

	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		System.out.println("track limitation: " + numberOfLimitedStatuses);
	}

	public void onScrubGeo(long userId, long upToStatusId) {
		System.out.println("Got scrub_geo event userId:" + userId
				+ " upToStatusId:" + upToStatusId);
	}

	public void onStallWarning(StallWarning warning) {
		System.out.println("Got stall warning:" + warning);
	}

	public void onException(Exception ex) {
		ex.printStackTrace();
	}
}
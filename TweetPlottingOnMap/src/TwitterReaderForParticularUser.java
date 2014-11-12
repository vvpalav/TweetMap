import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterReaderForParticularUser {

	private final ConfigurationBuilder cb;
	private final String consumerKey = "x5R4hzLsACZXQjGsK0u49riNi";
	private final String consumerSecret = "yfl9C3O9mhsm1BinTjD0NfoAy5idZAhVVUbi6xb2RWexiHzgYw";
	private final String accessKey = "2869307315-TAMtDAoMzhiUvgaLjxejTuEyTjvl2XcXaYw4L3X";
	private final String tokenPrivate = "gpvuKSifiNZnwk1egNtVOumZmpNNk6MVXwOMdTL3lPP2X";

	public static void main(String[] args) throws TwitterException {

		// Initiate Twitter Reader
		TwitterReaderForParticularUser tb = new TwitterReaderForParticularUser();
		TwitterFactory tf = new TwitterFactory(tb.cb.build());
		Twitter twitter = tf.getInstance();

		// Fetch Tweets for User
		System.out.println("Fetching Tweets for Narendra Modi");
		Query query = new Query("narendramodi");
		QueryResult result;
		do {
			result = twitter.search(query);
			List<Status> tweets = result.getTweets();
			for (Status tweet : tweets) {
				System.out.println("@" + tweet.getUser().getScreenName()
						+ " - " + tweet.getText());
			}
		} while ((query = result.nextQuery()) != null);
	}

	public TwitterReaderForParticularUser() {
		this.cb = new ConfigurationBuilder();
		this.cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessKey)
				.setOAuthAccessTokenSecret(tokenPrivate);
	}
}

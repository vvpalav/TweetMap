import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterReaderForParticularUser extends HttpServlet {

	private static final long serialVersionUID = -6128144911835523415L;
	private DBHelper db;
	private ConfigurationBuilder cb;
	private TwipMapSQSHandler sqs;
	
	public static void main(String[] args) throws ServletException, IOException {
		new TwitterReaderForParticularUser().doPost(null, null);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
			int count = 0;
			sqs = TwipMapSQSHandler.initializeTwipMapSQSHandler();
			db = new DBHelper(sqs);
			cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
					.setOAuthConsumerKey(Configuration.twitterConsumerKey)
					.setOAuthConsumerSecret(Configuration.twitterConsumerSecret)
					.setOAuthAccessToken(Configuration.twitterAccessKey)
					.setOAuthAccessTokenSecret(Configuration.twitterTokenPrivate);

			String name = req.getParameter("username");
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();

			Query query = new Query(name);
			QueryResult result = null;
			do {
				result = twitter.search(query);
				if (result == null)
					break;
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					if (tweet.getGeoLocation() != null) {
						count++;
						System.out.println("@" + tweet.getUser().getScreenName() + " - "
								+ tweet.getText());
						double latitude = tweet.getGeoLocation().getLatitude();
						double longitude = tweet.getGeoLocation().getLongitude();
						long id = tweet.getId();
						Date timestamp = tweet.getCreatedAt();
						String user = tweet.getUser().getScreenName();
						String text = tweet.getText();
						TweetNode node = new TweetNode(id, user, text, latitude, longitude, timestamp);
						if (text.contains("@"))
							db.insertTweetIntoDB(node);
					}
				}
			} while (((query = result.nextQuery()) != null) && (count < 100));
		} catch (TwitterException e) {
			e.printStackTrace();
		} 
	}
}

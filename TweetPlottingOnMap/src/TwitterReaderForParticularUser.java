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

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class TwitterReaderForParticularUser extends HttpServlet {

	private static final long serialVersionUID = -6128144911835523415L;
	private ConfigurationBuilder cb;
	private TwipMapSQSHandler sqs;
	private String queueUrl;
	private static final int threadCount = 3;
	private int tweetReceived = 0; 

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
			sqs = TwipMapSQSHandler.getSQSHandler();
			queueUrl = sqs.getQueueURL(Configuration.queueName);
			cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
					.setOAuthConsumerKey(Configuration.twitterConsumerKey)
					.setOAuthConsumerSecret(Configuration.twitterConsumerSecret)
					.setOAuthAccessToken(Configuration.twitterAccessKey)
					.setOAuthAccessTokenSecret(
							Configuration.twitterTokenPrivate);

			startAlchemyThreads(sqs);
			String name = req.getParameter("username");
			sqs.deleteAllMessagesFromQueue(Configuration.queueName);
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			System.out.println("Received twit analysis for " + name);
			Query query = new Query(name);
			QueryResult result = null;
			do {
				result = twitter.search(query);
				if (result == null)
					break;
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					tweetReceived++;
					if (tweet.getGeoLocation() != null
							&& tweet.getGeoLocation().getLatitude() != 0
							&& tweet.getGeoLocation().getLongitude() != 0) {
						count++;
						double latitude = tweet.getGeoLocation().getLatitude();
						double longitude = tweet.getGeoLocation()
								.getLongitude();
						long id = tweet.getId();
						Date timestamp = tweet.getCreatedAt();
						String user = tweet.getUser().getScreenName();
						String text = tweet.getText();
						TweetNode node = new TweetNode(id, user, text,
								latitude, longitude, timestamp);
						node.setType("live");
						System.out.println(node.toString());
						sqs.sendMessageToQueue(queueUrl, node.toJSON()
								.toString());
					}
				}
				if (count >= 40)
					break;
			} while ((query = result.nextQuery()) != null);
			JSONObject json = new JSONObject();
			json.put("msg", "processing");
			resp.getWriter().write(json.toString());
			resp.getWriter().flush();
			resp.getWriter().close();
			Thread.sleep(2000);
			sendStopProcessingMsg();
		} catch (TwitterException e) {
			System.out.println("Exception processed tweets: " + tweetReceived);
			sendStopProcessingMsg();
			JSONObject json = new JSONObject();
			try {
				json.put("msg", "exception");
				json.put("text", e.getMessage());
				resp.getWriter().write(json.toString());
				resp.getWriter().flush();
				resp.getWriter().close();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (InterruptedException e1) {
			sendStopProcessingMsg();
			e1.printStackTrace();
		} catch (JSONException e) {
			sendStopProcessingMsg();
			e.printStackTrace();
		} finally {
			System.out.println("Finished Processing all tweets");
			sqs.deleteAllMessagesFromQueue(Configuration.queueName);
		}
	}

	public void sendStopProcessingMsg() {
		sqs.sendMessageToQueue(queueUrl, Configuration.stopProcessingMsg);
		while (AlchemyAPIHandler.getLiveThreadsValue() > 0) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void startAlchemyThreads(final TwipMapSQSHandler sqs) {
		sqs.deleteAllMessagesFromQueue(Configuration.queueName);
		for (int i = 0; i < threadCount; ++i) {
			AlchemyAPIHandler.changeLiveThreadsValue(1);
			new Thread(new Runnable() {
				public void run() {
					new AlchemyAPIHandler(sqs).processSQSMessage();
					System.out.println("Threads - 1");
				}
			}).start();
		}
	}
}

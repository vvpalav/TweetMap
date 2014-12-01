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

import com.amazonaws.services.sqs.model.Message;

public class TwitterReaderForParticularUser extends HttpServlet {

	private static final long serialVersionUID = -6128144911835523415L;
	private ConfigurationBuilder cb;
	private TwipMapSQSHandler sqs;
	private String queueUrl;
	private static final int threadCount = 1;

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
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();

			Query query = new Query(name);
			QueryResult result = null;
			do {
				result = twitter.search(query);
				if (result == null)
					break;
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					if (tweet.getGeoLocation() != null
							&& tweet.getText().contains("@")) {
						count++;
						double latitude = tweet.getGeoLocation().getLatitude();
						double longitude = tweet.getGeoLocation().getLongitude();
						long id = tweet.getId();
						Date timestamp = tweet.getCreatedAt();
						String user = tweet.getUser().getScreenName();
						String text = tweet.getText();
						TweetNode node = new TweetNode(id, user, text,
								latitude, longitude, timestamp);
						System.out.println(node.toString());
						sqs.sendMessageToQueue(queueUrl, node.toJSON().toString());
					}
				}
				if (count >= 10)
					break;
			} while ((query = result.nextQuery()) != null);
			Thread.sleep(2000);
			sqs.sendMessageToQueue(queueUrl, Configuration.stopProcessingMsg);
			while (AlchemyAPIHandler.getLiveThreadsValue() > 0) {
				Thread.sleep(5000);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			System.out.println("Finished Processing all tweets, deleting Queue and topic");
			new TwitMapSNSHandler(Configuration.queueRegion)
				.deleteTopicIfExists(Configuration.snsTopic);
			sqs.deleteQueue(queueUrl);
		}
	}

	public void startAlchemyThreads(final TwipMapSQSHandler sqs) {
		List<Message> list = sqs.getMessagesFromQueue(queueUrl);
		for (Message m : list) {
			sqs.deleteMessageFromQueue(queueUrl, m.getReceiptHandle());
		}
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

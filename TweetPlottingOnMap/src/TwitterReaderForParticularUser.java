import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterReaderForParticularUser extends HttpServlet {

	private static final long serialVersionUID = -6128144911835523415L;
	private static ConfigurationBuilder cb;
	private final static String consumerKey = "x5R4hzLsACZXQjGsK0u49riNi";
	private final static String consumerSecret = "yfl9C3O9mhsm1BinTjD0NfoAy5idZAhVVUbi6xb2RWexiHzgYw";
	private final static String accessKey = "2869307315-TAMtDAoMzhiUvgaLjxejTuEyTjvl2XcXaYw4L3X";
	private final static String tokenPrivate = "gpvuKSifiNZnwk1egNtVOumZmpNNk6MVXwOMdTL3lPP2X";

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

		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();

		try {
			cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
					.setOAuthConsumerSecret(consumerSecret)
					.setOAuthAccessToken(accessKey)
					.setOAuthAccessTokenSecret(tokenPrivate);
			
			String name = req.getParameter("username");
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();

			Query query = new Query(name);
			QueryResult result = null;
			do {
				result = twitter.search(query);
				if(result == null) break;
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					if (tweet.getGeoLocation() != null) {
						System.out.println(tweet.getText());
						array.put(tweet.getGeoLocation().getLatitude() + " "
								+ tweet.getGeoLocation().getLongitude());
					}
				}
			} while (((query = result.nextQuery()) != null)
					&& (array.length() < 20));

			if(array.length() > 0){
				json.put("latlon", array);
				json.put("error", "success");
			} else {
				json.put("error", "failed");
				json.put("msg", "Twitter returned null resultset");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			try {
				json.put("error", "failed");
				json.put("msg", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (TwitterException e) {
			e.printStackTrace();
			try {
				json.put("error", "failed");
				json.put("msg", e.getErrorMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} finally {
			resp.setContentType("text/json");
			resp.getWriter().println(json.toString());
			resp.getWriter().flush();
			resp.getWriter().close();
		}
	}
}

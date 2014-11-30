import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class AlchemyAPIHandler {

	private TwipMapSQSHandler sqs;
	private String queueUrl;
	private TwitMapSNSHandler sns;
	private String snsTopicArn;
	private static int liveThreads = 0;

	public AlchemyAPIHandler(TwipMapSQSHandler sqs) {
		this.sqs = sqs;
		this.queueUrl = sqs.getQueueURL(Configuration.queueName);
		this.sns = new TwitMapSNSHandler(Configuration.queueRegion);
		this.snsTopicArn = this.sns.createSNSTopic(Configuration.httpEndpoint);
	}

	public static synchronized void changeLiveThreadsValue(int value) {
		liveThreads = liveThreads + (value);
	}

	public static synchronized int getLiveThreadsValue() {
		return liveThreads;
	}

	public void processSQSMessage() {
		while (true) {
			try {
				Thread.sleep(5000);
				List<Message> list = sqs.getMessagesFromQueue(this.queueUrl);
				if (list != null) {
					for (Message m : list) {
						if (m.getBody().equals(Configuration.stopProcessingMsg)) {
							changeLiveThreadsValue(-1);
							break;
						}
						sqs.deleteMessageFromQueue(this.queueUrl,
								m.getReceiptHandle());
						TweetNode node = new TweetNode(new JSONObject(
								m.getBody()));
						JSONObject json = performSentimentAnalysisOnTweet(node
								.getText());
						if (!json.getString("status").equalsIgnoreCase("error")) {
							node.setSentiment(json
									.getJSONObject("docSentiment").getString("type"));
							sns.sendNotification(this.snsTopicArn, node
									.toJSON().toString());
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public JSONObject performSentimentAnalysisOnTweet(String text) {
		try {
			String data = makeParamString(text);
			URL url = new URL(Configuration.alchemyURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.addRequestProperty("Content-Length",
					Integer.toString(data.length()));
			DataOutputStream ostream = new DataOutputStream(
					conn.getOutputStream());
			ostream.write(data.getBytes());
			ostream.flush();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			StringBuilder response = new StringBuilder();
			int charCode = -1;
			while ((charCode = in.read()) != -1) {
				response.append((char) charCode);
			}
			ostream.close();
			in.close();
			conn.disconnect();
			System.out.println("response: " + response.toString());
			return new JSONObject(response.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String makeParamString(String text) {
		StringBuilder data = new StringBuilder();
		try {
			data.append("apikey=").append(Configuration.alchemyAPIKey);
			data.append("&text=").append(URLEncoder.encode(text, "UTF-8"));
			data.append("&outputMode=json").append("&showSourceText=1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return data.toString();
	}
}
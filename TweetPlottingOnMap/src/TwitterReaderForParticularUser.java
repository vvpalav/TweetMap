import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private Logger log = Logger.getLogger(TwitterReaderForParticularUser.class
			.getName());

	public static void main(String[] args) throws ServletException, IOException {
		new TwitterReaderForParticularUser().doPost(null, null);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	/*
	 * @Override protected void doPost(HttpServletRequest req,
	 * HttpServletResponse resp) throws ServletException, IOException {
	 * 
	 * JSONObject json = new JSONObject(); JSONArray array = new JSONArray();
	 * 
	 * try { cb = new ConfigurationBuilder();
	 * cb.setDebugEnabled(true).setOAuthConsumerKey
	 * (Configuration.twitterConsumerKey)
	 * .setOAuthConsumerSecret(Configuration.twitterConsumerSecret)
	 * .setOAuthAccessToken(Configuration.twitterAccessKey)
	 * .setOAuthAccessTokenSecret(Configuration.twitterTokenPrivate);
	 * 
	 * String name = req.getParameter("username"); Twitter twitter = new
	 * TwitterFactory(cb.build()).getInstance();
	 * 
	 * Query query = new Query(name); QueryResult result = null; do { result =
	 * twitter.search(query); if(result == null) break; List<Status> tweets =
	 * result.getTweets(); for (Status tweet : tweets) { if
	 * (tweet.getGeoLocation() != null) { System.out.println(tweet.getText());
	 * array.put(tweet.getGeoLocation().getLatitude() + " " +
	 * tweet.getGeoLocation().getLongitude()); } } } while (((query =
	 * result.nextQuery()) != null) && (array.length() < 20));
	 * 
	 * if(array.length() > 0){ json.put("latlon", array); json.put("error",
	 * "success"); } else { json.put("error", "failed"); json.put("msg",
	 * "Twitter returned null resultset"); } } catch (JSONException e) {
	 * e.printStackTrace(); try { json.put("error", "failed"); json.put("msg",
	 * e.getMessage()); } catch (JSONException e1) { e1.printStackTrace(); } }
	 * catch (TwitterException e) { e.printStackTrace(); try { json.put("error",
	 * "failed"); json.put("msg", e.getErrorMessage()); } catch (JSONException
	 * e1) { e1.printStackTrace(); } } finally {
	 * resp.setContentType("text/json");
	 * resp.getWriter().println(json.toString()); resp.getWriter().flush();
	 * resp.getWriter().close(); } }
	 */

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			SecurityException {
		// Get the message type header.
		String messagetype = request.getHeader("x-amz-sns-message-type");
		// If message doesn't have the message type header, don't process it.
		if (messagetype == null)
			return;

		// Parse the JSON message in the message body
		// and hydrate a SNSMessage object with its contents
		// so that we have easy access to the name/value pairs
		// from the JSON message.
		Scanner scan = new Scanner(request.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		scan.close();
		SNSMessage msg = readMessageFromJson(builder.toString());

		// The signature is based on SignatureVersion 1.
		// If the sig version is something other than 1,
		// throw an exception.
		if (msg.getSignatureVersion().equals("1")) {
			// Check the signature and throw an exception if the signature
			// verification fails.
			if (isMessageSignatureValid(msg))
				log.info(">>Signature verification succeeded");
			else {
				log.info(">>Signature verification failed");
				throw new SecurityException("Signature verification failed.");
			}
		} else {
			log.info(">>Unexpected signature version. Unable to verify signature.");
			throw new SecurityException(
					"Unexpected signature version. Unable to verify signature.");
		}

		// Process the message based on type.
		if (messagetype.equals("Notification")) {
			// TODO: Do something with the SNSMessage and Subject.
			// Just log the subject (if it exists) and the message.
			String logMsgAndSubject = ">>Notification received from topic "
					+ msg.getTopicArn();
			if (msg.getSubject() != null)
				logMsgAndSubject += " Subject: " + msg.getSubject();
			logMsgAndSubject += " SNSMessage: " + msg.getMessage();
			log.info(logMsgAndSubject);
		} else if (messagetype.equals("SubscriptionConfirmation")) {
			// TODO: You should make sure that this subscription is from the
			// topic you expect. Compare topicARN to your list of topics
			// that you want to enable to add this endpoint as a subscription.

			// Confirm the subscription by going to the subscribeURL location
			// and capture the return value (XML message body as a string)
			Scanner sc = new Scanner(
					new URL(msg.getSubscribeURL()).openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			sc.close();
			log.info(">>Subscription confirmation (" + msg.getSubscribeURL()
					+ ") Return value: " + sb.toString());
			// TODO: Process the return value to ensure the endpoint is
			// subscribed.
		} else if (messagetype.equals("UnsubscribeConfirmation")) {
			// TODO: Handle UnsubscribeConfirmation message.
			// For example, take action if unsubscribing should not have
			// occurred.
			// You can read the SubscribeURL from this message and
			// re-subscribe the endpoint.
			log.info(">>Unsubscribe confirmation: " + msg.getMessage());
		} else {
			log.info(">>Unknown message type.");
		}
		log.info(">>Done processing message: " + msg.getMessageId());
	}

	private static boolean isMessageSignatureValid(SNSMessage msg) {
		try {
			URL url = new URL(msg.getSigningCertUrl());
			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf
					.generateCertificate(inStream);
			inStream.close();

			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(getMessageBytesToSign(msg));
			return sig.verify(Base64.decodeBase64(msg.getSignature()));
		} catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);
		}
	}

	private static byte[] getMessageBytesToSign(SNSMessage msg) {
		byte[] bytesToSign = null;
		if (msg.getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.getType().equals("SubscriptionConfirmation")
				|| msg.getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}

	// Build the string to sign for Notification messages.
	public static String buildNotificationStringToSign(SNSMessage msg) {
		String stringToSign = null;

		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "SNSMessage\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		if (msg.getSubject() != null) {
			stringToSign += "Subject\n";
			stringToSign += msg.getSubject() + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	// Build the string to sign for SubscriptionConfirmation
	// and UnsubscribeConfirmation messages.
	public static String buildSubscriptionStringToSign(SNSMessage msg) {
		String stringToSign = null;
		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "SNSMessage\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += msg.getSubscribeURL() + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "Token\n";
		stringToSign += msg.getToken() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	private SNSMessage readMessageFromJson(String string) {
		ObjectMapper mapper = new ObjectMapper();
		SNSMessage message = null;
		try {
			message = mapper.readValue(string, SNSMessage.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return message;
	}
}

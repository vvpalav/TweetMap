import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpEndpointForTwiMapSNS extends HttpServlet {

	private static final long serialVersionUID = 2306967918597987927L;
	private Logger log = Logger.getLogger(HttpEndpointForTwiMapSNS.class
			.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException, SecurityException {
		String messagetype = req.getHeader("x-amz-sns-message-type");
		if (messagetype == null)
			return;

		Scanner scan = new Scanner(req.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		scan.close();
		SNSMessage msg = readMessageFromJson(builder.toString());
		validateMessage(msg);

		if (messagetype.equals("Notification")) {
			String logMsgAndSubject = "Notification: Topic "
					+ msg.getTopicArn();
			logMsgAndSubject += " Subject: " + msg.getSubject();
			logMsgAndSubject += " Message: " + msg.getMessage();
			log.info(logMsgAndSubject);
			forwardMessageToJSPPage(req, resp, msg.getMessage());
		}
		log.info("Done processing message: " + msg.getMessageId());
	}

	private void validateMessage(SNSMessage msg) {
		if (msg.getSignatureVersion().equals("1")) {
			if (isMessageSignatureValid(msg))
				log.info("Signature verification succeeded");
			else {
				log.info("Signature verification failed");
				throw new SecurityException("Signature verification failed.");
			}
		} else {
			log.info("Unexpected signature version. Unable to verify signature.");
			throw new SecurityException("Unable to verify signature.");
		}
	}

	private void forwardMessageToJSPPage(HttpServletRequest request,
			HttpServletResponse response, String message) {
		try {
			request.setAttribute("twitterMsg", new JSONObject(message).toString());
			getServletContext().getRequestDispatcher("/index.jsp").forward(
					request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	public static String buildNotificationStringToSign(SNSMessage msg) {
		String stringToSign = null;
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

	public static String buildSubscriptionStringToSign(SNSMessage msg) {
		String stringToSign = null;
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

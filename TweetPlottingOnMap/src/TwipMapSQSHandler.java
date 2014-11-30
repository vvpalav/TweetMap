import java.util.HashSet;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class TwipMapSQSHandler {
	private AmazonSQS sqsHandler;
	private HashSet<String> queueList;
	private static TwipMapSQSHandler twipMapSQSHandler;

	private TwipMapSQSHandler() {
		AWSCredentials credentials = new ProfileCredentialsProvider("default").getCredentials();
		sqsHandler = new AmazonSQSClient(credentials);
		Region region = Region.getRegion(Regions.fromName(Configuration.queueRegion));
		sqsHandler.setRegion(region);
		createMessageQueue(Configuration.queueName);
		queueList = new HashSet<String>();
	}

	/**
	 * This function creates singleton SQS handler in passed in Region.
	 * 
	 * @param regionName
	 *            Region name in which SQS handler to be created.
	 * 
	 * @return <b>TwipMapSQSHandler</b>: Instance of TwipMapSQSHandler
	 */
	public static synchronized TwipMapSQSHandler getSQSHandler() {
		if (twipMapSQSHandler == null) {
			twipMapSQSHandler = new TwipMapSQSHandler();
		}
		return twipMapSQSHandler;
	}

	/**
	 * This function creates queue using SQS service. Queue name is checked in
	 * queue list and if it's already taken then request is rejected.
	 * 
	 * @param myQueueName
	 *            Queue name to be created.
	 * 
	 * @return <b>null</b>: If queue name is already taken <br/>
	 *         <b>queueURL</b>: URL of newly created queue
	 */
	public synchronized String createMessageQueue(String myQueueName) {
		if (!queueList.contains(myQueueName)) {
			queueList.add(myQueueName);
			CreateQueueRequest request = new CreateQueueRequest(myQueueName);
			return sqsHandler.createQueue(request).getQueueUrl();
		}
		return null;
	}
	
	public String getQueueURL(String queueName){
		if (!queueList.contains(queueName)) {
			GetQueueUrlRequest url = new GetQueueUrlRequest(queueName);
			return sqsHandler.getQueueUrl(url).getQueueUrl();
		}
		return null;
	}

	/**
	 * This function sends message to passed in queue using SQS service. Queue
	 * name is checked in existing queue list.
	 * 
	 * @param myQueueUrl
	 *            Queue to which message needs to be send.
	 * @param message
	 *            Message text to be send
	 * 
	 * @return <b>true</b>: If message is send to queue successfully <br/>
	 *         <b>false</b>: If queue name is not found then request is
	 *         rejected.
	 */
	public synchronized boolean sendMessageToQueue(String myQueueUrl,
			String message) {
		if (queueList.contains(myQueueUrl) && message != null) {
			sqsHandler.sendMessage(new SendMessageRequest(myQueueUrl, message));
			return true;
		}
		return false;
	}

	/**
	 * This function receive message from passed in queue using SQS service.
	 * Queue name is checked in existing queue list.
	 * 
	 * @param myQueueUrl
	 *            Queue from which message needs to be received.
	 * 
	 * @return <b>true</b>: If message is send to queue successfully <br/>
	 *         <b>false</b>: If queue name is not found then request is
	 *         rejected.
	 */
	public synchronized List<Message> getMessagesFromQueue(String myQueueUrl) {
		if (queueList.contains(myQueueUrl)) {
			ReceiveMessageRequest request = new ReceiveMessageRequest(myQueueUrl);
			return sqsHandler.receiveMessage(request).getMessages();
		}
		return null;
	}

	/**
	 * This function deletes message from passed in queue using SQS service.
	 * Queue name is checked in existing queue list.
	 * 
	 * @param myQueueUrl
	 *            Queue from which message needs to be deleted.
	 * @param messageRecieptHandle
	 *            Message handler
	 * 
	 * @return <b>true</b>: If message is deleted from queue successfully <br>
	 *         <b>false</b>: If queue name is not found then request is
	 *         rejected.
	 */
	public synchronized boolean deleteMessageFromQueue(String myQueueUrl,
			String messageRecieptHandle) {
		if (queueList.contains(myQueueUrl) && messageRecieptHandle != null) {
			sqsHandler.deleteMessage(new DeleteMessageRequest(myQueueUrl,
					messageRecieptHandle));
			return true;
		}
		return false;
	}

	/**
	 * This function deletes passed in queue using SQS service. Queue name is
	 * checked in existing queue list.
	 * 
	 * @param myQueueUrl
	 *            Queue which needs to be deleted.
	 * 
	 * @return <b>true</b>: If message is deleted from queue successfully <br>
	 *         <b>false</b>: If queue name is not found then request is
	 *         rejected.
	 */
	public synchronized boolean deleteQueue(String myQueueUrl) {
		if (queueList.contains(myQueueUrl)) {
			sqsHandler.deleteQueue(new DeleteQueueRequest(myQueueUrl));
			return true;
		}
		return false;
	}
}

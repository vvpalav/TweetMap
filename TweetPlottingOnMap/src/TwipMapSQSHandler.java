import java.util.HashMap;
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
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class TwipMapSQSHandler {
	private AmazonSQS sqsHandler;
	private HashMap<String, String> queueList;
	private static TwipMapSQSHandler twipMapSQSHandler;

	private TwipMapSQSHandler() {
		queueList = new HashMap<String, String>();
		AWSCredentials credentials = new ProfileCredentialsProvider("EC2").getCredentials();
		
		sqsHandler = new AmazonSQSClient(credentials);
		Region region = Region.getRegion(Regions.fromName(Configuration.queueRegion));
		sqsHandler.setRegion(region);
		queueList.put(Configuration.queueName, createMessageQueue(Configuration.queueName));
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
		ListQueuesResult l = sqsHandler.listQueues();
		for (String s :l.getQueueUrls()){
			if(s.contains(myQueueName)){
				deleteQueue(getQueueURL(myQueueName));
				queueList.remove(myQueueName);
				try {
					System.out.println("Wait for 70 secs before "
							+ "creating new queue after delete queue");
					Thread.sleep(70 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		CreateQueueRequest request = new CreateQueueRequest(myQueueName);
		return sqsHandler.createQueue(request).getQueueUrl();
	}
	
	public String getQueueURL(String queueName){
		if (queueList.containsKey(queueName)) {
			return queueList.get(queueName);
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
		if (queueList.containsValue(myQueueUrl) && message != null) {
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
		if (queueList.containsValue(myQueueUrl)) {
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
		if (queueList.containsValue(myQueueUrl) && messageRecieptHandle != null) {
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
		if (queueList.containsValue(myQueueUrl)) {
			sqsHandler.deleteQueue(new DeleteQueueRequest(myQueueUrl));
			return true;
		}
		return false;
	}
}

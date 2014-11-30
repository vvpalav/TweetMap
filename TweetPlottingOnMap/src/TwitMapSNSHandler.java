import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;


public class TwitMapSNSHandler {

	private AmazonSNSClient snsClient;
	
	public TwitMapSNSHandler(String region){
		
		AWSCredentials credentials = new ProfileCredentialsProvider("default").getCredentials();
		this.snsClient = new AmazonSNSClient(credentials);		                           
		this.snsClient.setRegion(Region.getRegion(Regions.fromName(region)));
	}
	
	public String createSNSTopic(String httpEndpoint){
		CreateTopicRequest createTopicRequest = new CreateTopicRequest(Configuration.snsTopic);
		String topicArn = snsClient.createTopic(createTopicRequest).getTopicArn();
		SubscribeRequest subRequest = new SubscribeRequest(topicArn, "http", httpEndpoint);
		this.snsClient.subscribe(subRequest);
		return topicArn;
	}
	
	public void sendNotification(String topicArn, String msg){
		PublishRequest publishRequest = new PublishRequest(topicArn, msg);
		this.snsClient.publish(publishRequest);
	}
	
	public void deleteSNSTopic(String topicArn){
		DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(topicArn);
		this.snsClient.deleteTopic(deleteTopicRequest);
	}
}

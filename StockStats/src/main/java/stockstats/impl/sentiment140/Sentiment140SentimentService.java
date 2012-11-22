package stockstats.impl.sentiment140;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import stockstats.Sentiment;
import stockstats.impl.SentimentService;
import stockstats.impl.Tweet;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * A SentimentService implementation that delegates to the Sentiment140 cloud API.
 */
public class Sentiment140SentimentService implements SentimentService {
	
	private String appId;
	private Client client;
	private static final String BASE_URL = "http://www.sentiment140.com/api/";
	
	public Sentiment140SentimentService(String appId) {
		this.appId = appId;
		client = Client.create();
	}

	@Override
	public void classify(List<Tweet> tweets) {
		
		WebResource webResource = client.resource(BASE_URL + "bulkClassifyJson");
		
		BulkClassifyRequest bulkRequest = new BulkClassifyRequest();
		Map<String,Tweet> idToTweet = new HashMap<String, Tweet>();
		List<ClassifyRequest> data = new ArrayList<ClassifyRequest>();
		for (Tweet tweet: tweets) {
			ClassifyRequest request = new ClassifyRequest();
			request.setId(tweet.getId());
			request.setText(tweet.getText());
			data.add(request);
			idToTweet.put(tweet.getId(), tweet);
		}
		bulkRequest.setData(data);
		
		ClientResponse response = webResource.queryParam("appid", appId)
				.type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.post(ClientResponse.class, bulkRequest);
		
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new RuntimeException("Sentiment140 returned status " + response.getStatus());
		}
		
		String jsonStr = response.getEntity(String.class);
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonStr);
			JsonNode dataNode = rootNode.path("data");
			for (Iterator<JsonNode> i = dataNode.getElements(); i.hasNext();) {
				JsonNode resultNode = i.next();
				String id = resultNode.path("id").getTextValue();
				int polarity = resultNode.path("polarity").getIntValue();
				
				Sentiment sentiment;
				if (polarity == 0) {
					sentiment = Sentiment.NEGATIVE;
				} else if (polarity == 2) {
					sentiment = Sentiment.NEUTRAL;
				} else {
					sentiment = Sentiment.POSITIVE;
				}
				
				Tweet tweet = idToTweet.get(id);
				tweet.setSentiment(sentiment);
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

}

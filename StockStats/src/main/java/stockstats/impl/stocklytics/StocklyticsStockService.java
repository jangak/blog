package stockstats.impl.stocklytics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import stockstats.StockStats;
import stockstats.impl.InvalidStockSymbol;
import stockstats.impl.StockService;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * A StockService implementation that delegates to the Stocklytics cloud API.
 */
public class StocklyticsStockService implements StockService {
	
	private String apiKey;
	private Client client;
	private static final String FORMAT = "JSON";
	private static final String BASE_URL = "http://api.stocklytics.com/";
	
	public StocklyticsStockService(String apiKey) {
		this.apiKey = apiKey;
		client = Client.create();
	}

	@Override
	public StockStats getHistoricalPrices(String stock, Date closingDate) throws InvalidStockSymbol {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String closingDateStr = df.format(closingDate);
		
		WebResource webResource = client.resource(BASE_URL + "historicalPrices/1.0");
		
		ClientResponse response = webResource.queryParam("api_key", apiKey)
				.queryParam("format", FORMAT)
				.queryParam("stock", stock)
				.queryParam("start", closingDateStr)
				.queryParam("end", closingDateStr)
				.type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.get(ClientResponse.class);
		
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new RuntimeException("Stockylitics returned status " + response.getStatus());
		}
		
		String jsonStr = response.getEntity(String.class);
		
		// A 200 status is sent back for invalid stocks
		if (jsonStr.contains("Error: Stocklytics doesn't have data for that stock.")) {
			throw new InvalidStockSymbol(stock);
		}
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonStr);
			StockStats price = new StockStats();
			price.setClosingDate(closingDateStr);
			price.getFinancialStats().setOpen(rootNode.path(closingDateStr).path("open").getValueAsDouble());
			price.getFinancialStats().setClose(rootNode.path(closingDateStr).path("close").getValueAsDouble());
			price.getFinancialStats().setHigh(rootNode.path(closingDateStr).path("high").getValueAsDouble());
			price.getFinancialStats().setLow(rootNode.path(closingDateStr).path("low").getValueAsDouble());
			price.getFinancialStats().setVolume(rootNode.path(closingDateStr).path("volume").getValueAsLong());
		
			return price;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
	}

}

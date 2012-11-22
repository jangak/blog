package stockstats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import stockstats.impl.InvalidStockSymbol;
import stockstats.impl.SentimentService;
import stockstats.impl.StockService;
import stockstats.impl.StockStatsResourceImpl;
import stockstats.impl.Tweet;
import stockstats.impl.TwitterService;

public class StockStatsResourceImplTest {
	
	private StockService mockStockService;
	private TwitterService mockTwitterService;
	private SentimentService mockSentimentService;
	private StockStatsResourceImpl stockStatsService;
	
	@Before
	public void setup() {
		mockStockService = Mockito.mock(StockService.class);
		mockTwitterService = Mockito.mock(TwitterService.class);
		mockSentimentService = Mockito.mock(SentimentService.class);
		
		stockStatsService = new StockStatsResourceImpl();
		stockStatsService.setStockService(mockStockService);
		stockStatsService.setTwitterService(mockTwitterService);
		stockStatsService.setSentimentService(mockSentimentService);
	}
	
	@Test
	public void testStockStatsPositive() throws Exception {

		// Request data
		String stock = "XYZ";
		String asOfDateStr = "2012-11-01";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date asOfDate = df.parse(asOfDateStr);
		
		// Mock stats data
		StockStats mockStats = new StockStats();
		mockStats.setClosingDate(asOfDateStr);
		FinancialStats mockFinancialStats = mockStats.getFinancialStats();
		mockFinancialStats.setClose(60.51);
		mockFinancialStats.setOpen(58.23);
		mockFinancialStats.setHigh(60.74);
		mockFinancialStats.setLow(58.12);
		mockFinancialStats.setVolume(12345678);
		
		// Mock Twitter data
		List<Tweet> mockTweets = new ArrayList<Tweet>();
		final Tweet positiveTweet = new Tweet();
		positiveTweet.setId("1");
		positiveTweet.setText("Love it.");
		final Tweet negativeTweet = new Tweet();
		negativeTweet.setId("2");
		negativeTweet.setText("Hate it.");
		final Tweet neutralTweet = new Tweet();
		neutralTweet.setId("3");
		neutralTweet.setText("Take it or leave it.");
		mockTweets.add(positiveTweet);
		mockTweets.add(negativeTweet);
		mockTweets.add(neutralTweet);
		
		// Set up stubs
		Mockito.when(mockStockService.getHistoricalPrices(stock, asOfDate)).thenReturn(mockStats);
		Mockito.when(mockTwitterService.search(stock, asOfDate, 100, 1)).thenReturn(mockTweets);
		Mockito.doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				List<Tweet> tweets = (List<Tweet>) invocation.getArguments()[0];
				for (Tweet t: tweets) {
					if (t.getText().equals(positiveTweet.getText())) {
						t.setSentiment(Sentiment.POSITIVE);
					} else if (t.getText().equals(negativeTweet.getText())) {
						t.setSentiment(Sentiment.NEGATIVE);
					} else {
						t.setSentiment(Sentiment.NEUTRAL);
					}
				}
				return null;
			}
			
		}).when(mockSentimentService).classify(mockTweets);
		
		// Invoke the method under test
		StockStats actualStats = stockStatsService.search(stock, asOfDateStr);
		
		// Set up expected results
		StockStats expectedStats = new StockStats();
		expectedStats.setClosingDate(asOfDateStr);
		expectedStats.getFinancialStats().setClose(mockFinancialStats.getClose());
		expectedStats.getFinancialStats().setOpen(mockFinancialStats.getOpen());
		expectedStats.getFinancialStats().setHigh(mockFinancialStats.getHigh());
		expectedStats.getFinancialStats().setLow(mockFinancialStats.getLow());
		expectedStats.getFinancialStats().setVolume(mockFinancialStats.getVolume());
		expectedStats.getSocialStats().addTweet(positiveTweet);
		expectedStats.getSocialStats().addTweet(negativeTweet);
		expectedStats.getSocialStats().addTweet(neutralTweet);
		
		// Asser that we got what was expected
		Assert.assertEquals(expectedStats, actualStats);
		
	}

	@Test(expected=WebApplicationException.class)
	public void testStockStatsBadStock() throws Exception {

		// Request data
		String stock = "XYZ";
		String asOfDateStr = "2012-11-01";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date asOfDate = df.parse(asOfDateStr);
		
		InvalidStockSymbol e = new InvalidStockSymbol(stock);
		
		// Set up stubs
		Mockito.when(mockStockService.getHistoricalPrices(stock, asOfDate)).thenThrow(e);
		
		try {
			stockStatsService.search(stock, asOfDateStr);
		} catch (WebApplicationException e1) {
			Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e1.getResponse().getStatus());
			throw e1;
		}
	}
	
	@Test(expected=WebApplicationException.class)
	public void testStockStatsNullStock() throws Exception {

		// Request data
		String asOfDateStr = "2012-11-01";
		
		try {
			stockStatsService.search(null, asOfDateStr);
		} catch (WebApplicationException e1) {
			Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e1.getResponse().getStatus());
			throw e1;
		}
	}
	
	@Test(expected=WebApplicationException.class)
	public void testStockStatsInvalidDate() throws Exception {

		// Request data
		String stock = "XYZ";
		String asOfDateStr = "2012-11-xx";
		
		try {
			stockStatsService.search(stock, asOfDateStr);
		} catch (WebApplicationException e1) {
			Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e1.getResponse().getStatus());
			throw e1;
		}
	}
	
	@Test(expected=WebApplicationException.class)
	public void testStockStatsInvalidDate2() throws Exception {

		// Request data
		String stock = "XYZ";
		String asOfDateStr = "2012-11-01x";
		
		try {
			stockStatsService.search(stock, asOfDateStr);
		} catch (WebApplicationException e1) {
			Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e1.getResponse().getStatus());
			throw e1;
		}
	}
	
	@Test(expected=WebApplicationException.class)
	public void testStockStatsNullDate() throws Exception {

		// Request data
		String stock = "XYZ";
		
		try {
			stockStatsService.search(stock, null);
		} catch (WebApplicationException e1) {
			Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e1.getResponse().getStatus());
			throw e1;
		}
	}
	
	@Test(expected=WebApplicationException.class)
	public void testStockStatsInternalError() throws Exception {

		// Request data
		String stock = "XYZ";
		String asOfDateStr = "2012-11-01";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date asOfDate = df.parse(asOfDateStr);
		
		// Set up stubs
		Mockito.when(mockStockService.getHistoricalPrices(stock, asOfDate)).thenThrow(new RuntimeException("KABOOM!!!"));
		
		try {
			stockStatsService.search(stock, asOfDateStr);
		} catch (WebApplicationException e1) {
			Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e1.getResponse().getStatus());
			throw e1;
		}
	}
}

package stockstats.impl;

import java.util.Date;

import stockstats.StockStats;

/**
 * Defines an interface for classes that can fetch historical stock stats.
 */
public interface StockService {

	StockStats getHistoricalPrices(String stock, Date date) throws InvalidStockSymbol;
	
}

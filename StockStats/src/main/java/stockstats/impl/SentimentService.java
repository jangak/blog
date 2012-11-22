package stockstats.impl;

import java.util.List;

/**
 * Defines an interface for classes that can classify Twitter tweets
 * as positive, negative or neutral.
 */
public interface SentimentService {

	void classify(List<Tweet> tweets);
	
}

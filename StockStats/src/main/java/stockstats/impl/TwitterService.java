package stockstats.impl;

import java.util.Date;
import java.util.List;

/**
 * Defines an interface for classes that can search Twitter.
 */
public interface TwitterService {

	List<Tweet> search(String stock, Date date, int pageSize, int page);
	
}

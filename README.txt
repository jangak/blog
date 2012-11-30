StockStats Service
==================

1. Overview
This project demonstrates how Component Bindings can be used to allow Java POJOs hosted in
Mule ESB to use the bus to make outbound calls without being tied to proprietary Mule APIs.  
The project consists of a RESTful service which supports a single HTTP GET operation that 
aggregates historical financial and Twitter sentiment data for a given stock on a given 
as-of date.  The back-end dependencies are Stocklytics, Twitter and Sentiment140.  The Twitter
integration uses a Component Binding which in turn uses Mule's Twitter Cloud Connector.

Mule Studio should be used to explore and run the service.


2. Before running the service
Obtain API keys for Stocklytics and Sentiment140.  A Twitter API key is not required.  Once you 
have acquired the API keys, edit the StockStats.launch file and enter the key values for the 
stockstats.stocklyticsApiKey and stockstats.sentiment140ApiKey system properties.  Below are the 
links for the Stocklytics and Sentiment140 API pages.

    Stocklytics API Page - http://developer.stocklytics.com
    Sentiment140 API Page - http://help.sentiment140.com/api


3. Running the service
Right click on the StockStats.launch file and choose "Run As --> Mule Application".


4. Testing the service
Note that the service will listen on port 8180.  If you wish to change the port, you can do so 
by changing the port number for the HTTP inbound-endpoint defined in the StockServiceFlow flow.

You can invoke the service from a web browser or other client with a URL like this:  
    http://localhost:8180/api/stockStats?stock=AAPL&date=2012-11-27

The "stock" parameter is the stock symbol to look up and the "date" parameter is the as-of date.

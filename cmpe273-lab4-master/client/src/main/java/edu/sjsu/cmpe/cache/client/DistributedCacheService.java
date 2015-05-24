package edu.sjsu.cmpe.cache.client;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import java.util.concurrent.ConcurrentHashMap;
import com.mashape.unirest.http.Unirest;
import java.util.concurrent.Future;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.List;

/**
 * Distributed cache service
 * 
 */
public class DistributedCacheService implements CacheServiceInterface {
	private final String cacheServerUrl;

	ConcurrentHashMap<String, String> currentOpStatus = new ConcurrentHashMap<String, String>();

	CRDTClientPOC crdtClientPOC;

	public DistributedCacheService(String serverUrl) {
		this.cacheServerUrl = serverUrl;
	}

	public DistributedCacheService(String cacheServerUrl, ConcurrentHashMap<String, String> currentOpStatus) {
		this.cacheServerUrl = cacheServerUrl;
		this.currentOpStatus = currentOpStatus;
	}

	public DistributedCacheService(String cacheServerUrl, CRDTClientPOC crdtClientPOC) {
		this.cacheServerUrl = cacheServerUrl;
		this.crdtClientPOC = crdtClientPOC;
	}

	public String getCacheServerURL(){
		return this.cacheServerUrl;
	}
	/**
	 * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#get(long)
	 */
	@Override
	public void get(long key) {
		Future<HttpResponse<JsonNode>> contentsFuture = Unirest.get(this.cacheServerUrl + "/cache/{key}")
				.header("accept", "application/json")
				.routeParam("key", Long.toString(key))
				.asJsonAsync(new Callback<JsonNode>() {

					public void failed(UnirestException e) {
						System.out.println("The Get request has been failed");
						crdtClientPOC.currentGetStatus.put(cacheServerUrl, "fail");
					}

					public void completed(HttpResponse<JsonNode> response) {
						if(response.getCode() != 200) {
							crdtClientPOC.currentGetStatus.put(cacheServerUrl, "a");
						} else {
							String serverValue = response.getBody().getObject().getString("value");
							System.out.println("Get value from the server: "+cacheServerUrl+": "+serverValue);
							crdtClientPOC.currentGetStatus.put(cacheServerUrl, serverValue);
						}
					}

					public void cancelled() {
						System.out.println("The Get operation has been cancelled");
						crdtClientPOC.currentGetStatus.put(cacheServerUrl, "fail");
					}

				});
	}

	/**
	 * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#put(long,
	 *      java.lang.String)
	 */
	@Override
	public void put(long key, String value) {
		System.out.println("Key-value that is being sent via put operation: "+key+", "+value);
		Future<HttpResponse<JsonNode>> contentsFuture = Unirest.put(this.cacheServerUrl + "/cache/{key}/{value}")
				.header("accept", "application/json")
				.routeParam("key", Long.toString(key))
				.routeParam("value", value)
				.asJsonAsync(new Callback<JsonNode>() {

					public void failed(UnirestException e) {
						System.out.println("The put request has been failed");
						crdtClientPOC.currentPutStatus.put(cacheServerUrl, "fail");
					}

					public void completed(HttpResponse<JsonNode> response) {
						if (response == null || response.getCode() != 200) {
							System.out.println("Failed to add to the cache.");
							crdtClientPOC.currentPutStatus.put(cacheServerUrl, "fail");
						} else {
							System.out.println("The put operation has done successfully");
							crdtClientPOC.currentPutStatus.put(cacheServerUrl, "pass");
						}
					}

					public void cancelled() {
						System.out.println("The put operation has been cancelled");
						crdtClientPOC.currentPutStatus.put(cacheServerUrl, "fail");
					}

				});
	}

	/**
	 * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#delete(long)
	 */
	@Override
	public boolean delete(long key) {
		HttpResponse<JsonNode> response = null;
		try {
            response = Unirest.delete(this.cacheServerUrl + "/cache/{key}")
					.header("accept", "application/json")
					.routeParam("key", Long.toString(key)).asJson();
		} catch (UnirestException e) {
			System.err.println(e);
		}

		if(response ==null || response.getCode() != 204) {
			System.out.println("Delete operation has failed");
			return false;
		} else{
			System.out.println("Delete operation has done successfully");
			return true;
		}
	}
}

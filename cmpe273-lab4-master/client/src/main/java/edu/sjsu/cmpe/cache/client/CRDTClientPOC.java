package edu.sjsu.cmpe.cache.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.List;

public class CRDTClientPOC {

    long threadSleepTime = 1000;
	public ConcurrentHashMap<String, String> currentPutStatus = new ConcurrentHashMap<String, String>();
	private ArrayList<DistributedCacheService> serversList = new ArrayList<DistributedCacheService>();
    public ConcurrentHashMap<String, String> currentGetStatus = new ConcurrentHashMap<String, String>();

    public void addCacheServer(String cacheServerURL) {
		serversList.add(new DistributedCacheService(cacheServerURL,this));
	}
	
	
	public void put(long key, String value) {
		for(DistributedCacheService cacheServer: serversList) {
            cacheServer.put(key, value);
		}
		
		while(true) {
        	if(currentPutStatus.size() < 3) {
        		try {
        			System.out.println("Put request is getting processed. Wait....");
					Thread.sleep(threadSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	} else{
        		int fail = 0;
        		int pass = 0;
        		for(DistributedCacheService cacheServer: serversList) {
        			System.out.println("For ("+cacheServer.getCacheServerURL()+ ") Put status is : "+currentPutStatus.get(cacheServer.getCacheServerURL()));
        			if(currentPutStatus.get(cacheServer.getCacheServerURL()).equalsIgnoreCase("fail"))
            			fail++;
            		else
            			pass++;
        		}
        		
        		if(fail > 1) {
        			System.out.println("Put operation is geetting Rolled back on all servers");
        			for(DistributedCacheService cacheServer: serversList) {
                        cacheServer.delete(key);
        			}
        		} else {
        			System.out.println("/ ***--- Servers are updated Successfully ---*** / \n");
        		}
        		currentPutStatus.clear();
        		break;
        	}
        }
	}
	
	public String get(long key){
		for(DistributedCacheService cacheServer: serversList) {
            cacheServer.get(key);
		}
		
		while(true) {
        	if(currentGetStatus.size() < 3) {
        		try {
        			System.out.println("Get request is getting processed. Wait....");
					Thread.sleep(threadSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	} else{
        		HashMap<String, List<String>> checkOutcomes = new HashMap<String, List<String>>();
        		for(DistributedCacheService cacheServer: serversList) {
        			if(currentGetStatus.get(cacheServer.getCacheServerURL()).equalsIgnoreCase("fail"))
            			System.out.println("Value is not got from server: "+cacheServer.getCacheServerURL());
            		else {
            			if(checkOutcomes.containsKey(currentGetStatus.get(cacheServer.getCacheServerURL()))) {
            				checkOutcomes.get(currentGetStatus.get(cacheServer.getCacheServerURL())).add(cacheServer.getCacheServerURL());
            			} else {
            				List<String> tempList = new ArrayList<String>();
                            tempList.add(cacheServer.getCacheServerURL());
            				checkOutcomes.put(currentGetStatus.get(cacheServer.getCacheServerURL()),tempList);
            			}
            		}
        		}
        		
        		if(checkOutcomes.size() != 1) {
        			System.out.println("Values on each server are different. Inconsistency occured...");
        			Iterator<Entry<String, List<String>>> tempIterator = checkOutcomes.entrySet().iterator();
        			int majorityCount = 0;
        			String finalValueStr = null;
        			ArrayList <String> updateServerList = new ArrayList<String>();
        		    while (tempIterator.hasNext()) {
        		        Map.Entry<String, List<String>> pairEntries = (Map.Entry<String, List<String>>)tempIterator.next();
        		        if(pairEntries.getValue().size() > majorityCount) {
        		        	majorityCount = pairEntries.getValue().size();
        		        	finalValueStr = pairEntries.getKey();
        		        } else {
        		        	for (String str: pairEntries.getValue()){
        		        		updateServerList.add(str);
        		        	}
        		        }
        		    }
        		    
        			System.out.println("Updating values to make the servers consistent.");
        			for(String s: updateServerList){
        				for(DistributedCacheService cacheServer: serversList) {
            				if(cacheServer.getCacheServerURL().equalsIgnoreCase(s)){
            					System.out.println("Correcting value for server: "+cacheServer.getCacheServerURL()+" as: "+finalValueStr);
                                cacheServer.put(key, finalValueStr);
            				}
            			}
        			}
        			currentGetStatus.clear();
        			return finalValueStr;
        		} else {
        			System.out.println("/ ***--- Get Operation on all servers is successfully done ---*** / ");
        			currentGetStatus.clear();
        			return checkOutcomes.keySet().toArray()[0].toString();
        		}
        	}
        }
		
	}

}

package edu.sjsu.cmpe.cache.client;

public class Client {

    public static void main(String[] args) throws Exception {

        long threadSleepTime = 30*1000;
        CRDTClientPOC crdtClientPOC = new CRDTClientPOC();
        crdtClientPOC.addCacheServer("http://localhost:3000");
        crdtClientPOC.addCacheServer("http://localhost:3001");
        crdtClientPOC.addCacheServer("http://localhost:3002");

        System.out.println("/ ***---- Starting Cache Client... ----*** / \n");
        // First HTTP put call to store "a" value to key-1
        crdtClientPOC.put(1, "a");
        System.out.println("/ ***---- Stop Server A to make one server inconsistent----*** / \n");
        Thread.sleep(threadSleepTime);

        //2nd HTTP put call to update key-1 value to "b"
        //stop any one server to make one server inconsistent
        crdtClientPOC.put(1, "b");
        System.out.println("/ ***---- Start Server-A again. Values from all servers will be read and values on each server will be made consistent ----*** / \n");
        Thread.sleep(threadSleepTime);
        
        //3rd Get call to make values on each server connsistent
        System.out.println("Now value in all servers is: "+crdtClientPOC.get(1));
        
        System.out.println("/ ***---- Exiting CRDT Cache Client program... ----*** / \n");
    }

}

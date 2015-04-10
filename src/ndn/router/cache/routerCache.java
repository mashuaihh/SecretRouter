/**
 * Content store and routing
 */
package ndn.router.cache;

import java.util.*;
/**
 * @author Administrator
 *
 */
public class routerCache {

	/**
	 * 
	 */
	public routerCache(int cachesize, int routerstrategy) {
		this.cachesize = cachesize;
		this.routerstrategy = routerstrategy;
        this.remainingCacheSize = cachesize;
        Llist = new LinkedList<routerResource>();
        rResource = new LinkedList<routerResource>();
        mRandom = new Random(); 
        LFUAccessFrequece = new long[routerMain.resourceNum];  // store object access frequence
        for(int i=0; i<routerMain.resourceNum;i++){
        	LFUAccessFrequece[i] = 0;     // object never comes through this node
        }
	}

	/**
	 * reset cache size 
	 */
	public void resetCacheSize(int sizes){
		this.cachesize = sizes;
		this.remainingCacheSize = cachesize;
	}



	/**
	 * packet is coming in, tackle it via LRU
	 */
	public boolean routing(simulationEvent se){
		// determine whether to cache a resource 
		
////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////testing cache//////////////////////////////////////////////
      //if(se.getEventTime()>50000) System.out.println(Llist.size());
		
		
		////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		routerResource rR1 = se.getrouterResource();
		if(mRandom.nextDouble() > rR1.getcacheProbability()){ // do not tackle it
			if(Llist.contains(rR1))
				return true;
			else
				return false;
		}
		
		// cache the resource 
		if(Llist.contains(rR1)){  // resource is cached
		    // rearrange the list
			Llist.remove(rR1);
			Llist.addFirst(rR1);
			return true;
		}
		else{
			scheduleLRU(rR1);
		}
		return false;
	}

	/**
	 * LRU schedule
	 */
    private void scheduleLRU(routerResource rR1){
    	int iSize = rR1.getSize();
    	if(iSize > cachesize)return; // too large to store
    	
    	// kick out old cached resources until there's enough space
    	while(!Llist.isEmpty()&&(remainingCacheSize < iSize)){
    		routerResource trR = Llist.removeLast();
    		remainingCacheSize = remainingCacheSize + trR.getSize();
    	}
    	Llist.addFirst(rR1);    // First use, First out
    	remainingCacheSize = remainingCacheSize - rR1.getSize();
    }
	
	/**
	 * packet is coming in, tackle it via LFU
	 */
	public boolean routingLFU(simulationEvent se){
		// determine whether to cache a resource 
		routerResource rR1 = se.getrouterResource();
		if(mRandom.nextDouble() > rR1.getcacheProbability()){
			if(Llist.contains(rR1))
				return true;
			else
				return false;
		}
		
		// cache the resource 
		if(Llist.contains(rR1)){  // resource is cached
		    // add access frequence
			LFUAccessFrequece[rR1.getID()]++;
			return true;
		}
		else{
			scheduleLFU(rR1);
		}
		return false;
	}
	
	/**
	 * LFU schedule
	 */
    private void scheduleLFU(routerResource rR1){
    	int iSize = rR1.getSize();
    	if(iSize > cachesize)return; // too large to store
    	
    	// kick out old cached resources until there's enough space
    	while(!Llist.isEmpty()&&(remainingCacheSize < iSize)){
    		// search for the least frequence vistited object
    		Iterator<routerResource> ebIter = Llist.iterator();
    		routerResource temprR = ebIter.next();
    		while(ebIter.hasNext()){
    			routerResource temprR2 = ebIter.next();
    			if(LFUAccessFrequece[temprR.getID()] > LFUAccessFrequece[temprR2.getID()]){
    				temprR = temprR2;
    			}
    		}
    		
    		Llist.remove(temprR);
    		remainingCacheSize = remainingCacheSize + temprR.getSize();
    	}
    	Llist.addFirst(rR1);    // First use, First out
    	LFUAccessFrequece[rR1.getID()]++;
    	remainingCacheSize = remainingCacheSize - rR1.getSize();
    }
    
	
	/**
	 * add the resource into the list
	 */
	public void putResource(routerResource rR){
		rResource.addLast(rR);
	}
	
	/**
	 * get the resource. 
	 */
	public LinkedList<routerResource> getResource(){
		return rResource;
	}
	
	/**
	 * get the resource. 
	 */
	public int getRemainingcachesize(){
		return remainingCacheSize;
	}
	/*
	private class resourceUseInfo{

        private int useFrequency = 0;
        private routerResource rR;  // point to the resource		
	}
	*/
	
	private int cachesize = 0;              // average resource size:store 10 ones on average
	private int routerstrategy = 0;         // router strategy 0-LRU
	private routerNode routernode;          // corresponding router node
	private LinkedList<routerResource> rResource; // store resources
    private int remainingCacheSize;         // remaining cache size
    private LinkedList<routerResource> Llist; // store cached resources 
    private Random mRandom;                // determine whether to cache 
    private long[] LFUAccessFrequece;       // store resource access frequence in this node
}

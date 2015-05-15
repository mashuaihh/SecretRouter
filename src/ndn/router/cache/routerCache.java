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
        Llist = new LinkedList<routerResource>(); //store the cached resources
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
	 * @return true this cache contains the resource, false this cache does not contain the resource.
	 */
	public boolean routing(simulationEvent se){
		routerResource resource = se.getrouterResource();

		//add resource frequency
		this.addResourceCount(resource);

		// cache the resource 
		if(Llist.contains(resource)){  // the cahce contains the resource?
			System.out.println("yes " + resource.getID());
		    // rearrange the list
			Llist.remove(resource);
			Llist.addFirst(resource);
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * LRU schedule
	 */
    public boolean scheduleLRU(routerResource rR1, routerNode node){
    	int iSize = rR1.getSize();
    	if(iSize > cachesize)
    		return false; // too large to store
    	
    	// kick out old cached resources until there's enough space
    	while(!Llist.isEmpty()&&(remainingCacheSize < iSize)){
    		routerResource trR = Llist.removeLast();
    		this.outResourceList.add(trR);
    		System.out.println();
//    		System.out.println("Cache: " + node.getid() + ", ");
    		System.out.println("Removing resource " + trR.getID());
    		remainingCacheSize = remainingCacheSize + trR.getSize();
    	}
    	Llist.addFirst(rR1); 
    	remainingCacheSize = remainingCacheSize - rR1.getSize();
    	return true;
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
	
	public boolean hasResource(routerResource resource) {
		return rResource.contains(resource)|| Llist.contains(resource)||this.isServer;
	}
	
	/**
	 * get the resource. 
	 */
	public LinkedList<routerResource> getResource(){
		return rResource;
	}
	
	public void removeResource(routerResource resource) {
		this.Llist.remove(resource);
	}
	
	/**
	 * get the resource. 
	 */
	public int getRemainingcachesize(){
		return remainingCacheSize;
	}
	
	public void setServer() {
		this.isServer = true;
	}
	
	public boolean isServer() {
		return this.isServer;
	}
	
	/**
	 * The resources that are flushed out because of the 
	 * newly cached resources
	 * @author Mashuai
	 */
	public void removeOutResource(routerResource se) {
		this.outResourceList.remove(se);
	}
	
	public boolean isOutResourceListEmpty() {
		return this.outResourceList.isEmpty();
	}

	public List<routerResource> getOutResourceList() {
		return this.outResourceList;
	}
	
	public void addResourceCount(routerResource res) {
		if (this.resCounter.containsKey(res)) {
			Integer idx = this.resCounter.get(res);
			this.resCounter.remove(res);
			this.resCounter.put(res, idx+1);
		} else {
			this.resCounter.put(res, 1);
		}
	}
	
	public Integer getResourceCount(routerResource res) {
		if (this.resCounter.containsKey(res)) {
			Integer idx = this.resCounter.get(res);
			return idx;
		} else {
			return 0;
		}
	}

	private List<routerResource> outResourceList = new ArrayList<routerResource>(); //store the flushed out resources.
	
	private int cachesize = 0;              // average resource size:store 10 ones on average
	private int routerstrategy = 0;         // router strategy 0-LRU
	private LinkedList<routerResource> rResource; // store resources
    private int remainingCacheSize;         // remaining cache size
    private LinkedList<routerResource> Llist; // store cached resources 
    private Random mRandom;                // determine whether to cache 
    private long[] LFUAccessFrequece;       // store resource access frequence in this node
    private boolean isServer = false;
    //for CLS++
    private Map<routerResource, Integer> resCounter = new HashMap<routerResource, Integer>();
    
    public static void main(String[] args) {
    	routerResource resource1 = new routerResource(1, 3);
    	routerResource resource2 = new routerResource(2, 5);
    	routerResource resource3 = new routerResource(3, 6);
    	routerCache cache = new routerCache(7, 0);
    	cache.scheduleLRU(resource2, null);
    	cache.scheduleLRU(resource1, null);
    	cache.scheduleLRU(resource3, null);
    	System.out.println("Resource2 " + cache.getResourceCount(resource2));
    	System.out.println("Resource1 " + cache.getResourceCount(resource1));
    }
    
}

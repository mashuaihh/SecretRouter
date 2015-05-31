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
	
	public boolean hasCountListResourceInCache(routerResource resource) {
		for (int i = 0; i < this.Llist.size(); i++) {
			routerResource cacheResource = this.Llist.get(i);
			if (cacheResource.getID() == resource.getID()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasResource(routerResource resource) {
		return Llist.contains(resource)||this.isServer;
	}
	
	/**
	 * get the resource. 
	 */
	public LinkedList<routerResource> getResource(){
		return rResource;
	}
	
	public LinkedList<routerResource> getCacheResourceList() {
		return this.Llist;
	}
	
	public routerResource getResourceById(int idx) {
		for (routerResource res : this.Llist) {
			if (res.getID() == idx) {
				return res;
			}
		}
		return null;
	}
	
	public routerResource getResourceById(int idx, boolean boo) {
		for (routerResource r : this.allResourcesList) {
			if (r.getID() == idx) {
				return r;
			}
		}
		return null;
	}
	
	
	public routerResource getCountListResourceById(int idx) {
		for (routerResource res : this.resourceCountList) {
			if (res.getID() == idx) {
				return res;
			}
		}
		return null;
	}
	
	public void removeResource(routerResource resource) {
		this.Llist.remove(resource);
		this.remainingCacheSize += resource.getSize();
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
		routerResource resource = getCountResourceById(res.getID());
		resource.addCount();
	}
	
	private routerResource getCountResourceById(int index) {
		for (int i = 0; i < this.resourceCountList.size(); i++) {
			routerResource each = this.resourceCountList.get(i);
			if (each.getID() == index) {
				return each;
			}
		}
		return null;
	}
	
	public Integer getResourceCount(routerResource res) {
		routerResource resource = getCountResourceById(res.getID());
		return resource.getCount();
	}
	
	public void addInResourceCountList(routerResource[] array) {
		for (int i = 0; i < array.length; i++) {
			routerResource res = array[i];
			routerResource newResource = new routerResource(res.getID(), res.getSize());
			this.resourceCountList.add(newResource);
		}
	}
	
	public List<routerResource> getResourceCountList() {
		Collections.sort(this.resourceCountList);
		return this.resourceCountList;
	}
	
	public boolean hasEnoughRemainingCacheSize(routerResource res) {
		int resourceSize = res.getSize();
		if (resourceSize > this.remainingCacheSize) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * the remaining size is not enough for the resource.
	 * @param thisResource
	 * @return null if no need to cache this resource. A list<routerResource> to be replaced if 
	 * the replace is necessary.
	 */
	public List<routerResource> saveThisResource(routerResource thisResource) {
		List<routerResource> replacedResourceList = new ArrayList<routerResource>();

		Collections.sort(this.resourceCountList);

		int size = thisResource.getSize();
		int sumSize = this.remainingCacheSize;

		for (routerResource se : this.resourceCountList) {
//			if (this.hasResourceInCountList(se) && thisResource.getID() != se.getID()) {
//			if (this.hasResource(thisResource) && thisResource.getID() != se.getID()) {
			if (this.hasCountListResourceInCache(se) && thisResource.getID() != se.getID()) {
				replacedResourceList.add(se);
				int eachSize = se.getSize();
				sumSize += eachSize;
				if (sumSize >= size) {
					break;
				}
			}
		}
		//hotter than all resources to be replaced
		if (this.largerThanAllResources(replacedResourceList, thisResource)) {
			//cache this resource!
			return replacedResourceList;
		} else {
			return null;
		}
	}

	/**
	 * overloading the CLS+ saveThisResource function in which
	 * popularity of content is not considered.
	 * the remaining size is not enough for the resource.
	 * @param thisResource
	 * @return null if no need to cache this resource. A list<routerResource> to be replaced if 
	 * the replace is necessary.
	 */
	public List<routerResource> saveThisResource(routerResource thisResource, boolean isCls) {
		List<routerResource> replacedResourceList = new ArrayList<routerResource>();

		int size = thisResource.getSize();
		int sumSize = this.remainingCacheSize;

		for (routerResource se : this.resourceCountList) {
//			if (this.hasResourceInCountList(se) && thisResource.getID() != se.getID()) {
//			if (this.hasResource(thisResource) && thisResource.getID() != se.getID()) {
			if (this.hasCountListResourceInCache(se) && thisResource.getID() != se.getID()) {
				replacedResourceList.add(se);
				int eachSize = se.getSize();
				sumSize += eachSize;
				if (sumSize >= size) {
					break;
				}
			}
		}
		
		return replacedResourceList;
	}
	
	/**
	 * a litter tricky function. res1.compareTo(res2) if res1 hotter than res2
	 * return 1;
	 * @param list
	 * @return
	 */
	public boolean largerThanAllResources(List<routerResource> list, routerResource resource) {
		routerResource res = this.getCountListResourceById(resource.getID());
		for (routerResource e : list) {
			if (e.compareTo(res) > 0) {
				return false;
			}
		}
		return true;
	}
	
	public int getSize() {
		return this.cachesize;
	}
	
	public void addAllResources(routerResource[] rQueue) {
		for (routerResource e : rQueue) {
			this.allResourcesList.add(e);
		}
	}
	
	public void addAllResource(routerResource res) {
		this.allResourcesList.add(res);
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
    private List<routerResource> resourceCountList = new ArrayList<routerResource>();
    private List<routerResource> allResourcesList = new ArrayList<routerResource>();
    
    public static void main(String[] args) {

    	routerResource resource1 = new routerResource(1, 3);
    	routerResource resource2 = new routerResource(2, 5);
    	routerResource resource3 = new routerResource(3, 6);
    	
    	routerResource target = new routerResource(4,8);
    	routerCache cache = new routerCache(14, 0);

    	routerResource[] resArr = {resource1, resource2, resource3, target};
    	cache.addInResourceCountList(resArr);
    	for (int i = 0; i < 3; i++) {
    		cache.addResourceCount(resource1);
    	}
    	for (int i = 0; i < 2; i++) {
    		cache.addResourceCount(resource2);
    	}
   		cache.addResourceCount(resource3);
   		System.out.println(resource2.getCount());
   		List<routerResource> list = cache.getResourceCountList();
   		for (routerResource e : list) {
   			System.out.println(e.getID() + "  " + e.getCount() + " size " + e.getSize());
   		}
   		int i = 0;
   		while(i < 6) {
   			cache.addResourceCount(resource3);
   			i++;
   		}
   		System.out.println();
   		list = cache.getResourceCountList();
   		for (routerResource e : list) {
   			System.out.println(e.getID() + "  " + e.getCount() + " size " + e.getSize());
   		}
   		for (int j = 0; j < 2; j++) {
   			cache.addResourceCount(target);
   		}

   		list = cache.getResourceCountList();
   		System.out.println();
   		for (routerResource e : list) {
   			System.out.println(e.getID() + "  " + e.getCount() + " size " + e.getSize());
   		}
   		
   		List<routerResource> vlist = cache.saveThisResource(target);
   		if (vlist != null) {
   			System.out.println("cache");
   		} else {
   			System.out.println("not cache");
   		}
   		
   		System.out.println("res3.compareTo(res2) " + resource3.compareTo(resource2));
   		System.out.println("res2.compareTo(res3) " + resource2.compareTo(resource3));
   		
    }
    
}

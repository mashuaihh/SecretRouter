/**
 *  resource 
 */
package ndn.router.cache;

import java.util.List;

/**
 * @author Administrator
 *
 */
public class routerResource implements Comparable<routerResource> {

	@Override
	public int compareTo(routerResource other) {
		int other_count = other.getCount();
		if (this.count - other_count > 0) {
			return 1;
		} else if (this.count - other_count == 0) {
			return 0;
		} else {
			return -1;
		}
	}
	/**
	 * 
	 */
	public routerResource(int id, int size) {
		this.id = id;
		this.size = size;
	}


	/**
	 * set resource info.
	 */
	public void setResource(int id, int size) {
		this.id = id;
		this.size = size;
	}
	
	/**
	 * get resource id
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * get resource size
	 */
	public int getSize()
	{
		return size;
	}
	
	/**
	 * get resource size
	 */
	public void addFrequence(int times)
	{
		accessfrequence += times;
	}
	
	/**
	 * get resource Frequence
	 */
	public long getFrequence()
	{
		return accessfrequence;
	}

	/**
	 * set cache probability
	 */
	public void setcacheprobability(double cp)
	{
		this.cacheProbability = cp;
	}
	
	/**
	 * get cache probability
	 */
	public double getcacheProbability()
	{
		return cacheProbability;
	}

	
	/**
	 * set resource access probability
	 */
	public void setaccessprobability(double ap)
	{
		this.accessProbability = ap;
	}

	/**
	 * get resource access probability
	 */
	public double getaccessprobability()
	{
		return accessProbability;
	}
	
	public void addCount() {
		this.count++;
	}
	
	public int getCount() {
		return this.count;
	}
	
	
	private int id;       // resource identity
	private int size;   // resource size
	private long accessfrequence = 0; // access frequence 
	private double cacheProbability = 0.0; // cache probability
	private double accessProbability = 0.0; // cache probability
	//for cls+
	private int count = 0;
	
	public static void main(String[] args) {
    	routerResource resource1 = new routerResource(1, 3);
    	routerResource resource2 = new routerResource(2, 5);
    	routerResource resource3 = new routerResource(3, 6);
    	routerCache cache = new routerCache(7, 0);
    	routerResource[] resArr = {resource1, resource2, resource3};
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
   			System.out.println(e.getID() + "  " + e.getCount());
   		}
   		int i = 0;
   		while(i < 6) {
   			cache.addResourceCount(resource3);
   			i++;
   		}
   		System.out.println();
   		list = cache.getResourceCountList();
   		for (routerResource e : list) {
   			System.out.println(e.getID() + "  " + e.getCount());
   		}
   		
System.out.println();
   		System.out.println(resource3.compareTo(resource1));
   		System.out.println(resource1.compareTo(resource3));
	}
}

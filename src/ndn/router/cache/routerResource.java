/**
 *  resource 
 */
package ndn.router.cache;

/**
 * @author Administrator
 *
 */
public class routerResource {

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
	
	
	
	private int id;       // resource identity
	private int size;   // resource size
	private long accessfrequence = 0; // access frequence 
	private double cacheProbability = 0.0; // cache probability
	private double accessProbability = 0.0; // cache probability
}

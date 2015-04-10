/**
 *  router node
 */
package ndn.router.cache;

/**
 * @author Administrator
 *
 */
public class routerNode {

	/**
	 * 
	 */
	public routerNode(int id) {
		this.id = id;

	}
	
	/**
	 * for debugging
	 */
	public String toString(){
		return "" + id;
	}

	public int getid(){
		return id;
	}
	
	private int id;       // node identity
}

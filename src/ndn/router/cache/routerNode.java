/**
 *  router node
 */
package ndn.router.cache;

import java.util.ArrayList;
import java.util.List;

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
	
	public void setHop(int i) {
		this.hop = i;
	}
	
	public int getHop() {
		return this.hop;
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
	private int hop;

	/**
	 * for CLS implementation
	 */
	private List<routerTuple> tupleList = new ArrayList<routerTuple>();
	
	public List<routerTuple> getTupleList() {
		return this.tupleList;
	}
	
	public void addInTupleList(routerResource[] resourceList) {
		for (int i = 0; i < resourceList.length; i++) {
			routerResource resource = resourceList[i];
			routerTuple tuple = new routerTuple(resource);
			this.tupleList.add(tuple);
		}
	}
}

/**
 *  router node
 */
package ndn.router.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Map<routerResource, routerTuple> tupleMap = new HashMap<routerResource, routerTuple>();
	
	public Map<routerResource, routerTuple> getTupleMap() {
		return this.tupleMap;
	}
	
	public routerTuple getTuple(routerResource se) {
		routerTuple tuple = this.tupleMap.get(se);
		return tuple;
	}
				
	public void addInTupleList(routerResource[] resourceList) {
		for (int i = 0; i < resourceList.length; i++) {
			routerResource resource = resourceList[i];
			routerTuple tuple = new routerTuple(resource);
			this.tupleMap.put(resource, tuple);
		}
	}
}

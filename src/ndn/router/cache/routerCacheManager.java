/**
 * manage cache size of all nodes
 */
package ndn.router.cache;

import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 *
 */
public class routerCacheManager {

	/**
	 * 
	 */
	public routerCacheManager(Graph<routerNode, routerLink> gGraph) {
		this.gGraph = gGraph;
		// store the association in a hash map: key/value--routerNode/routerCache 
		routercachetable = new HashMap<routerNode, routerCache>();		
		for (routerNode v : gGraph.getVertices()) {
			routercachetable.put(v, new routerCache(0, 0)); // cache size will be changed later
		}
	}

	/**
	 * return router cache
	 */
    public routerCache getrouterCache(routerNode rN){
    	return routercachetable.get(rN);
    }	
    
    
	/**
	 * return router cache
	 */
    public Map<routerNode, routerCache> getrouterTable(){
    	return routercachetable;
    }	
    
	/**
	 * set resource number
	 */
    public void setresourceNum(int rN){
    	this.resourceNum = rN;
    }	

	/**
	 * return resource number
	 */
    public int getresourceNum(){
    	return resourceNum;
    }	
    
	/**
	 * return graph
	 */
    public Graph<routerNode, routerLink> getgraph(){
    	return gGraph;
    }	
	
	private Graph<routerNode, routerLink> gGraph;
	private Map<routerNode, routerCache> routercachetable; 
	private int resourceNum;
}

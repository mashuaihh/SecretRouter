/**
 * routing packets via shortest path
 */
package ndn.router.cache;

import ndn.router.newalgo.Lcd;
import ndn.router.newalgo.Mcd;
import ndn.router.newalgo.NewAlgo;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.*;
import edu.uci.ics.jung.graph.Graph;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.*;

import edu.uci.ics.jung.graph.util.Pair;


/**
 * @author Administrator
 *
 */
public class routingDijkstraShortestPath {

	/**
	 * 
	 */
	public routingDijkstraShortestPath(DistributionResource resDistribution) {
        this.DResource =  resDistribution;
        this.gGraph = resDistribution.getgraph();
        this.rMap = resDistribution.getrouterTable();
        
        // create transformer class for getting graph edge weight
		wtTransformer = new Transformer<routerLink, Integer>() {
			                public Integer transform(routerLink link) {
			                    return link.getWeight();
		                	}
			            };		
       
			            try{

			                    graphout= new PrintWriter(new FileWriter("C:/Results1.txt"));
			               
			        	    
			            }catch(Exception e){
			            	e.printStackTrace();
			            }

	}
	
	/**
	 * route the source via shortest path
	 */
	public void routing(simulationEvent se){
		// compute the frequency of the requested resource 
		routerResource rResource = se.getrouterResource();
		rResource.addFrequence(1);
		
		/**
		 * get vlist: requestNode -> ... -> serverNode
		 */
		routerNode requestNode = se.getEventNode();
		routerNode serverNode = se.getResourceNode();
		List<routerNode> vlist = getPathVertexList(requestNode, serverNode);
		
//		Lcd lcd = new Lcd(se, vlist, this.rMap);
//		lcd.routing();
//		lcd.showPath();
//		lcd.stat();
		
		Mcd mcd = new Mcd(se, vlist, this.rMap);
		mcd.routing();
		
	}
	
	
	private List<routerNode> getPathVertexList(routerNode beginNode, routerNode endNode) {
		List<routerNode> vlist;
		DijkstraShortestPath<routerNode, routerLink> DSPath = 
	    	    new DijkstraShortestPath<routerNode, routerLink>(gGraph, wtTransformer);
        	// get the shortest path in the form of edge list 
        List<routerLink> elist = DSPath.getPath(beginNode, endNode);
		
    		// get the node list form the shortest path
	    Iterator<routerLink> ebIter = elist.iterator();
	    vlist = new ArrayList<routerNode>(elist.size() + 1);
    	vlist.add(0, beginNode);
	    for(int i=0; i<elist.size(); i++){
		   routerLink aLink = ebIter.next();
	    	   	// get the nodes corresponding to the edge
    		Pair<routerNode> endpoints = gGraph.getEndpoints(aLink);
	        routerNode V1 = endpoints.getFirst();
	        routerNode V2 = endpoints.getSecond();
	    	if(vlist.get(i) == V1)
	    	   vlist.add(i+1, V2);
	        else
	        	vlist.add(i+1, V1);
	    }
	    return vlist;
	}
	
    private Map<routerNode, routerCache> rMap;
	private Graph<routerNode, routerLink> gGraph;
	private DistributionResource DResource;
	Transformer<routerLink, Integer> wtTransformer;      // transformer for getting edge weight
	private BigDecimal inoCache = BigDecimal.valueOf(0); // traffic in no cache condition
	private BigDecimal iCache = BigDecimal.valueOf(0);   // traffic in cache condition
	private BigDecimal rateStatistic;                    // iCache/inoCache
	private long outputcontrol = 0;
    private PrintWriter graphout;
    private long noCachecounts = 0;                      // no cache times 
	private BigDecimal pinoCache = BigDecimal.valueOf(0.1); // path length in no cache condition
	private BigDecimal piCache = BigDecimal.valueOf(0.1);   // path length in cache condition
	private BigDecimal prateStatistic;                    // iCache/inoCache
}

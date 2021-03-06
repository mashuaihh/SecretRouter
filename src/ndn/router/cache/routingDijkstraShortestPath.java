/**
 * routing packets via shortest path
 */
package ndn.router.cache;

import ndn.router.newalgo.Ccn;
import ndn.router.newalgo.Cls;
import ndn.router.newalgo.ClsPlus;
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
	public routingDijkstraShortestPath(DistributionResource resDistribution, String algotype) {
        this.DResource =  resDistribution;
        this.gGraph = resDistribution.getgraph();
        this.rMap = resDistribution.getrouterTable();
        this.algoType = algotype;
        
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


		if (algoType.equals("lcd")) {
			Lcd algo = new Lcd(se, vlist, this.rMap);
			algo.routing();
			algo.addPathNum();
			algo.showPath();
			algo.stat();
			this.HitRate = algo.getHitRate();
			this.PathStr = algo.getPathStr();
			this.HitNumber = algo.getHitNum();
			this.extraHop = algo.getExtraHop();
			this.extraLoad = algo.getExtraLoad();
		} else if (algoType.equals("cls")) {
			Cls algo = new Cls(se, vlist, this.rMap);
			algo.routing();
			algo.addPathNum();
			algo.showPath();
			algo.stat();
			this.HitRate = algo.getHitRate();
			this.PathStr = algo.getPathStr();
			this.HitNumber = algo.getHitNum();
			this.extraHop = algo.getExtraHop();
			this.extraLoad = algo.getExtraLoad();

		} else if (algoType.equals("ccn")) {
			Ccn algo = new Ccn(se, vlist, this.rMap);
			algo.routing();
			algo.addPathNum();
			algo.showPath();
			algo.stat();
			this.HitRate = algo.getHitRate();
			this.PathStr = algo.getPathStr();
			this.HitNumber = algo.getHitNum();
			this.extraHop = algo.getExtraHop();
			this.extraLoad = algo.getExtraLoad();
			
		} else if (algoType.equals("cls+")) {
			ClsPlus algo = new ClsPlus(se, vlist, this.rMap);
			algo.showPath();
			algo.routing();
			algo.addPathNum();
			algo.showPath();
			algo.stat();
			this.HitRate = algo.getHitRate();
			this.PathStr = algo.getPathStr();
			this.HitNumber = algo.getHitNum();
			this.extraHop = algo.getExtraHop();
			this.extraLoad = algo.getExtraLoad();

		}
		
		
//		Lcd lcd = new Lcd(se, vlist, this.rMap);
//		lcd.routing();
//		lcd.showPath();
//		lcd.stat();
		
	}
	
	public void clearResourceCount() {
		Collection<routerNode> col = this.gGraph.getVertices();
		for (routerNode node : col) {
			routerCache cache = this.rMap.get(node);
			cache.clearResourceCountList();
		}
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
	
	public void setAlgoType(String str) {
		this.algoType = str;
	}
	
	public double getHitRate() {
		return this.HitRate;
	}
	
	public double getPathStr() {
		return this.PathStr;
	}
	
	public int getExtraHop() {
		return this.extraHop;
	}
	
	public int getExtraLoad() {
		return this.extraLoad;
	}
	
	public static void clearStat() {
		NewAlgo.clearStat();
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
	private String algoType = "lcd";
	private double HitRate = 0;
	public int HitNumber = 0;
	public int requestNum = 0; 
	private double PathStr = 0;
	public int extraHop = 0; //for storing ousted resource's moving hops
	public int extraLoad = 0; //for storing ousted resources' moving load
}

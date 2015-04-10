/**
 * 
 */
package ndn.router.cache;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * @author Administrator
 *
 */
public class routingEFRPath {

	/**
	 * 
	 */
	public routingEFRPath(DistributionResource resDistribution) {
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

		                    graphout= new PrintWriter(new FileWriter("C:/Results.txt"));
		               
		        	    
		            }catch(Exception e){
		            	e.printStackTrace();
		            }
			            
		// create corresponding directed graph
	    transformGraph();     
       
	}
	
	
	/**
	 * create a directed graph for EFR routing
	 */
	private void transformGraph(){
		gGraphDirected = new DirectedSparseGraph<routerNode, routerLink>();
        List<routerNode> vlist = new ArrayList<routerNode>(Collections.unmodifiableCollection(gGraph.getVertices()));

        // add nodes
        for(int i=0;i<vlist.size();i++){
			routerNode rNode = new routerNode(vlist.get(i).getid());
			gGraphDirected.addVertex(rNode);
		}
		
        List<routerNode> vlistnew = new ArrayList<routerNode>(Collections.unmodifiableCollection(gGraphDirected.getVertices()));
        
        // add edges
        for(int i=0;i<vlist.size();i++){
        	int degrees = gGraph.degree(vlist.get(i));
        	
        	// search the node in the new directed graph
    		int mainnodeid = vlist.get(i).getid();
    		int mainnodeidnew=0;
    		for(; mainnodeidnew< vlistnew.size();mainnodeidnew++){
    			if(mainnodeid == vlistnew.get(mainnodeidnew).getid())
    				break;
    		}
       	
    		// add edges
        	for(routerNode V: gGraph.getNeighbors(vlist.get(i))){
        		int nodeid = V.getid();
        		int j=0;
        		// search the neighbors in the new directed graph
        		for(; j< vlistnew.size();j++){
        			if(nodeid == vlistnew.get(j).getid())
        				break;
        		}
        		// add edge
        		gGraphDirected.addEdge(new routerLink(vlistnew.get(mainnodeidnew).getid(),vlistnew.get(j).getid(), 1, degrees),
        				vlistnew.get(mainnodeidnew), vlistnew.get(j));
        	}
        }
	}
	
	
	/**
	 * for testing
	 */
	public void TestshortestPath(){
		DijkstraShortestPath<routerNode, routerLink> DSPath = 
		    new DijkstraShortestPath(gGraphDirected, wtTransformer);
		
    	// create edges
	    Collection<routerNode> vertices = gGraphDirected.getVertices();
	        for(routerNode v : vertices) {
	            for(routerNode w : vertices) {
	                if (v.equals(w) == false) // don't include self-distances
	                {
	                	System.out.println("" + v.getid()+ "-" + w.getid()  + DSPath.getPath(v, w).toString());
	                	System.out.println(DSPath.getDistance(v, w).toString());
	                }
	        }
	        }
		
		
		
	}
	

	/**
	 * get nodes with ID
	 */
	private routerNode getrouterNode(int id, Graph<routerNode, routerLink> gGraph){
	    Collection<routerNode> vertices = gGraph.getVertices();
		for(routerNode v : vertices){
			if(v.getid() == id) return v;
		}
		return null;
	}
	
	
	/**
	 * route the source via shortest path
	 */
	public void routing(simulationEvent se){
		// compute the frequency of the requested resource 
		routerResource rResource = se.getrouterResource();
		rResource.addFrequence(1);
		
		// routing
		List<routerNode> vlist;
		// request and resource are in the same node
		if(se.getEventNode()==se.getResourceNode()){
		 	vlist = new ArrayList<routerNode>();
		   	vlist.add(0, se.getEventNode());
		}
		//else if(false){
		else if(se.getrouterResource().getaccessprobability() > routerMain.EFRThreshold){/// shortest path routing
		    // get the shortest path
        	DijkstraShortestPath<routerNode, routerLink> DSPath = 
	    	    new DijkstraShortestPath<routerNode, routerLink>(gGraph, wtTransformer);
        	// get the shortest path in the form of edge list 
        	List<routerLink> elist = DSPath.getPath(se.getEventNode(), se.getResourceNode());
		
    		// get the node list form the shortest path
	    	Iterator<routerLink> ebIter = elist.iterator();
		    vlist = new ArrayList<routerNode>(elist.size() + 1);
    	    vlist.add(0, se.getEventNode());
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
		}// end of else if
		else{  //////////// EFR routing
		    // get the shortest path
        	DijkstraShortestPath<routerNode, routerLink> DSPath = 
	    	    new DijkstraShortestPath<routerNode, routerLink>(gGraphDirected, wtTransformer);
        	// get the shortest path in the form of edge list 
        	List<routerLink> elist = DSPath.getPath(
        			getrouterNode(se.getEventNode().getid(), gGraphDirected),
        			getrouterNode(se.getResourceNode().getid(),gGraphDirected));
		
    		// get the node list form the shortest path
	    	Iterator<routerLink> ebIter = elist.iterator();
	    	List<routerNode> vlistdirected = new ArrayList<routerNode>(elist.size() + 1);
	    	vlistdirected.add(0, getrouterNode(se.getEventNode().getid(),gGraphDirected));
	        for(int i=0; i<elist.size(); i++){
		       	routerLink aLink = ebIter.next();
	    	   	// get the nodes corresponding to the edge
    		   	Pair<routerNode> endpoints = gGraphDirected.getEndpoints(aLink);
	        	routerNode V1 = endpoints.getFirst();
	        	routerNode V2 = endpoints.getSecond();
	    	   	if(vlistdirected.get(i) == V1)
	    	   		vlistdirected.add(i+1, V2);
	        	else
	        		vlistdirected.add(i+1, V1);
	    	}
	        
	        // get original path
		    vlist = new ArrayList<routerNode>(vlistdirected.size());
		    for(int i=0;i<vlistdirected.size();i++){
		    	vlist.add(i, getrouterNode(vlistdirected.get(i).getid(), gGraph));
		    }
		}
		
		// routing
		int j;
		for(j=0;j<vlist.size();j++){ 
			routerNode tempNode = vlist.get(j);
			// if the resource is cached
			if(rMap.get(tempNode).routing(se)){
				break;
			}
		}
		if(1 == vlist.size()) return;      // no routing

		if(j == vlist.size()){   // no cache. fix the number
            j--;
            noCachecounts++;
		}

		if(j != vlist.size()){
		pinoCache = pinoCache.add(BigDecimal.valueOf((vlist.size() - 1))
       );
       // cost under cache condition
      piCache = piCache.add(BigDecimal.valueOf(j
                  
                  )
         );
   		
		}
		// compute the saved traffic cost
		// cost under no-cache condition
		inoCache = inoCache.add(BigDecimal.valueOf((vlist.size() - 1)
				                       *(se.getrouterResource().getSize())
				                       )
				    );
		// cost under cache condition
		iCache = iCache.add(BigDecimal.valueOf(j
				                       *(se.getrouterResource().getSize())
		                               )
            		);

		
		
/////////////////////////////////////////////////////////////////////////////////////////
		////////////////////testing ...................///////////////////////////////
    //	System.out.print("Shortest" + se.getEventNode().getid()+ "-" + se.getResourceNode().getid() +":");
    	   
   // 	for(routerNode v:vlist){
//    		System.out.print("," + v.getid());
    		
//    	}

 //   	System.out.print("---" + se.getrouterResource().getID() + "----");
    	
		////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////
		
		
		
	
		outputcontrol++;
		if(se.getEventTime() % 10000 == 0){
		    rateStatistic = iCache.divide(inoCache, 10, BigDecimal.ROUND_HALF_UP); // 10-scale, bits after dot
		    prateStatistic = piCache.divide(pinoCache, 10, BigDecimal.ROUND_HALF_UP); // 10-scale, bits after dot
			System.out.println(rateStatistic.doubleValue() + "-" + se.getEventTime() 
					              + "-" + outputcontrol + "-" + noCachecounts + "-" + prateStatistic.doubleValue());
		   graphout.println(rateStatistic.doubleValue() + "-" + outputcontrol);
		}
		
	}
	
	/**
	 * return the transformed graph
	 */
	public Graph<routerNode, routerLink> getTransfromedgraph(){
		return gGraphDirected;
	}
	
    private Map<routerNode, routerCache> rMap;
	private Graph<routerNode, routerLink> gGraph;
	private Graph<routerNode, routerLink> gGraphDirected;
	private DistributionResource DResource;
	Transformer<routerLink, Integer> wtTransformer;      // transformer for getting edge weight
	private BigDecimal inoCache = BigDecimal.valueOf(0); // traffic in no cache condition
	private BigDecimal iCache = BigDecimal.valueOf(0);   // traffic in cache condition
	private BigDecimal rateStatistic;                    // iCache/inoCache
	private long outputcontrol = 0;
    private PrintWriter graphout;
    private long noCachecounts = 0;
	private BigDecimal pinoCache = BigDecimal.valueOf(0); // traffic in no cache condition
	private BigDecimal piCache = BigDecimal.valueOf(0);   // traffic in cache condition
	private BigDecimal prateStatistic;                    // iCache/inoCache
}

/**
 * routing packets via shortest path
 */
package ndn.router.cache;

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

		
		// routing
		List<routerNode> vlist;
		// request and resource are in the same node
		if(se.getEventNode()==se.getResourceNode()){
		 	vlist = new ArrayList<routerNode>();
		   	vlist.add(0, se.getEventNode());
		}
		else{
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
	    
		// routing
		int j;
		for(j=0;j<vlist.size();j++){ 
			routerNode tempNode = vlist.get(j);
			// if the resource is cached
			if(rMap.get(tempNode).routing(se)){
				break;
			}
		}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////write remaining resource to file "C:/Results1.txt"//////////////////////////////////////////
		if(se.getEventTime() % 200 == 0){
        	for (routerNode v : Collections.unmodifiableCollection(rMap.keySet())) {
        		graphout.print("V" + v.toString() + "," + rMap.get(v).getRemainingcachesize() + ",D" + 
        				gGraph.degree(v) + "  ");
        	}
    	    graphout.println();
    	    graphout.println();
    	    graphout.println();
        	graphout.flush();
		}
//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		if(1 == vlist.size()) return;      // no routing
		
		// cache in the intermediate path
		if(j < vlist.size() - 1){
			pinoCache = pinoCache.add(BigDecimal.valueOf((vlist.size() - 1)));
	       // cost under cache condition
	      piCache = piCache.add(BigDecimal.valueOf(j
	                  
	                  )
	         );
	   		
		}

		// caching in the destination node
		if(j == vlist.size() - 1){   // no cache. fix the number
            noCachecounts++;
		}
		
		if(j == vlist.size()){   // no cache. fix the number
            j--;
            noCachecounts++;
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
		
		
		//System.out.println(iCache.doubleValue());
		//System.out.println(inoCache.doubleValue());

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
		if(se.getEventTime() % 10 == 0){
	    	rateStatistic = iCache.divide(inoCache, 10, BigDecimal.ROUND_HALF_UP); // 10-scale, bits after dot
		    prateStatistic = piCache.divide(pinoCache, 10, BigDecimal.ROUND_HALF_UP); // 10-scale, bits after dot
			   System.out.println(rateStatistic.doubleValue() + "-" + se.getEventTime() 
			              + "-routingtimes:" + outputcontrol + "-cach miss:" + noCachecounts + "-rateofpathlen:" + prateStatistic.doubleValue());
		   //graphout.println(rateStatistic.doubleValue() + "-" + outputcontrol);
		}
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

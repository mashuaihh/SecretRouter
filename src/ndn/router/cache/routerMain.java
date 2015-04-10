package ndn.router.cache;

import javax.swing.JFrame;
import edu.uci.ics.jung.graph.Graph;

public class routerMain {
    public static final int iswitch = 0; // 0-generate new graph; 1-load from file
    public static final int iswitch3 = 0; // 0-generate new resource; 1-load from file
    public static final int powerlawGenerator = 0;  // generating powerlaw sequence 0-new 1-load
    
    public static final int iswitch2 = 0; // for generating resource request// 0-create request 1-load request
    public static final int vertexNum = 100;
  
    
    // some constants
    public static final int resourceNum = 1000;  // default 4 6 8 1000
    public static  double EFRThreshold = 0.00157964;  // this is actually set in class DistributionRequestSequence
    public static final double CacheThreshold = 1.0;    // 0.5 0.1
    public static final int routerCacheSizedenominator = 100; // default= resourceNum/10


	/**
	 * main method
	 */
	public static void main(String[] args) {
		//Graph<routerNode, routerLink> g = new GraphTopologyRandom().getGraph();
		
		//System.out.println(g.toString());
	
		// generate a random graph
		Graph<routerNode, routerLink> graphRandom = new GraphTopologyRandom().getGraph();
        System.out.println("random graph is created");
		
		// generate a BA graph
		//Graph<routerNode, routerLink> graphRandom = new GraphTopologyBA().getGraph();
        //System.out.println("BA graph is created");

        // visualize the graph
/*		JFrame jf = new routerVisulization(graphRandom);
		jf.pack();
		jf.setVisible(true);
       System.out.println("graph is shown");
*/		
		// generate router cache
		routerCacheManager rCacheManager = new routerCacheManager(graphRandom);
        System.out.println("node cache is assigned");
		
		
		// generate resource distribution
		DistributionResource resDistribution = new DistributionResource(rCacheManager);
        System.out.println("resource is assigned");
	
		
		
		// generate event simulation 
		//simulationQueue sQueue = new simulationQueue();
        //System.out.println("simulation queue is created");
		
		// create request distribution generator
        DistributionRequestSequence drs = new DistributionRequestSequence(resDistribution);
        
        // generate powerlaw accees frequency
        drs.powlawGenerate();

        // EFR routing
      //  routingEFRPath rEFRP = new routingEFRPath(resDistribution);
//		JFrame jf2 = new routerVisulization(rEFRP.getTransfromedgraph());
//		jf2.pack();
//		jf2.setVisible(true);
 //       System.out.println("graph2 is shown"); 
        
       // rEFRP.TestshortestPath();
   /*     
       // test the routing path
        TestroutingEFRPath rEFRP = new TestroutingEFRPath(resDistribution);
		JFrame jf2 = new routerVisulization(rEFRP.getTransfromedgraph());
		jf2.pack();
		jf2.setVisible(true);
        System.out.println("graph2 is shown");  
        rEFRP.TestEFRshortestPath();
      */   
        
        //routingEFRPath rDSP = new routingEFRPath(resDistribution);
        
        // generate router
       routingDijkstraShortestPath rDSP = new routingDijkstraShortestPath(resDistribution);
		// start simulation

	 	//simulationEvent se = sQueue.getEvent();
		int etype;
		simulationEvent se;
		long sTimes = 2000000;
		while(sTimes > 0){
			sTimes--;
			se = drs.eventgenerate();
			// if the resource is in the node that generate the request,just return
			//if(se.getEventNode() == se.getResourceNode())
				//continue;
			etype = se.getEventType();
			// 0 - user request 
			// 1 - 
			switch(etype){
			case 0:
				
				rDSP.routing(se);      	
				
				break;
				
		
			
			case 1:
				
				
				break;
				
			case 2:
				
				break;
				
			default:
				break;	
			}
			
			
			//se = sQueue.getEvent();
		}
		
        // close file		
		drs.closefile();
		System.out.println("************************The End.****************************");
	}

}

/**
 *  generate resource distribution
 */
package ndn.router.cache;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import edu.uci.ics.jung.graph.Graph;

/**
 * @author Administrator
 *
 */
public class DistributionResource {

	/**
	 * 
	 */
	public DistributionResource(routerCacheManager rCM) {
		this.rCM = rCM;
		// get the Cache Manager table. this is not a good access pattern
		this.rMap = rCM.getrouterTable();
		this.rsize = routerMain.resourceNum;
		this.gGraph = rCM.getgraph();
		Random mRandom = new Random();
		
		//////////////////////////////////////
		////////////////////////////////////// fix later
		rCM.setresourceNum(rsize);
		

		
		// distribute the resources to router node
		
		// create resource
    	if(routerMain.iswitch3 == 0){
            try{
                PrintWriter graphout = new PrintWriter(new FileWriter(filename));
                graphout.println("" + rsize + " resources");
	         	rQueue = new routerResource[rsize];
	        	for(int i=0;i<rsize; i++){
	            	// create resources
	            	rQueue[i] = new routerResource(i, mRandom.nextInt(9) + 1); // uniformly distributed value 1-9
                    // output resource info.
                    graphout.println("" + rQueue[i].getID() + "," + rQueue[i].getSize());
	        	}

	        	// distribute the resource to the nodes. one resource can only stay on one node now.
	    		Random mRandom2 = new Random();
                List<routerNode> vlist = new ArrayList<routerNode>(Collections.unmodifiableCollection(rMap.keySet()));
	        	for(int i=0;i<rsize; i++){
	        		// distribute rQueue[i] to nodes
	        		routerCache cache = rMap.get((routerNode)vlist.get(mRandom2.nextInt(rMap.size())));
	        		cache.putResource(rQueue[i]);
	        	}
	        	/**
	        	 * set the server to node 0
	        	 * @author Mashuai
	        	 */
	        	for (routerNode e : vlist) {
	        		if (e.getid() == 0) {
	        			routerCache cache = rMap.get(e);
	        			cache.setServer();
	        		}
	        	}
	        	
	        	/**
	        	 * put all resources into server node 0
	        	 * @author Mashuai
	        	 */
	        	for (routerNode e : vlist) {
	        		routerCache cache = rMap.get(e);
	        		if (cache.isServer()) {
	        			for (int i = 0; i < rsize; i++) {
	        				cache.putResource(rQueue[i]);
	        			}
	        		}
	        	}
	        	
                // output vertex-resource info
                graphout.println("" + rMap.size() + " vertice-resource info.");
                int ionode = 0;
    	    	for (routerNode v : Collections.unmodifiableCollection(rMap.keySet())) {
    	    		graphout.print(v.toString() + ",");
    	    		// get the resource list in node v
    	    		Iterator<routerResource> bIter = rMap.get(v).getResource().iterator();
    	    		
    	    		// record node number that own resource
    	    		if(bIter.hasNext()) ionode++;
    	    		
                    while(bIter.hasNext()){
                    	graphout.print(bIter.next().getID() + ",");
                    }
                    graphout.println();
		        }
                graphout.println(ionode + " nodes have resources...");
        		graphout.close();
            }catch(Exception e){
            	e.printStackTrace();
            }
    	}
    	else if(routerMain.iswitch3 == 1){// load resource info. from file
            try{
            	BufferedReader graphreader = new BufferedReader(new FileReader(filename));

            	// load resources
            	String line = graphreader.readLine();  
            	StringTokenizer tokenizer;
          		tokenizer = new StringTokenizer(line, " ");
          		rsize = Integer.parseInt(tokenizer.nextToken());
	         	rQueue = new routerResource[rsize];
          		for(int i = 0;i < rsize; i++){
            		line = graphreader.readLine();
              		tokenizer = new StringTokenizer(line, ",");
	            	rQueue[i] = new routerResource(Integer.parseInt(tokenizer.nextToken()),
	            			Integer.parseInt(tokenizer.nextToken()));
            	}
 
            	// load cache info.
        		line = graphreader.readLine();
          		tokenizer = new StringTokenizer(line, " ");
          		int VerNum = Integer.parseInt(tokenizer.nextToken());
                List<routerNode> vlist = new ArrayList<routerNode>(Collections.unmodifiableCollection(rMap.keySet()));
          		for(int i = 0;i < VerNum; i++){
            		line = graphreader.readLine();
              		tokenizer = new StringTokenizer(line, ",");
                    // get vertex No.
                    int v1 = Integer.parseInt(tokenizer.nextToken());
                    routerNode rN1 = null;
                    // search vertex in the list
                    for(int j=0; j< VerNum; j++){
                    	if(((routerNode)vlist.get(j)).getid() == v1){
                    		rN1 = vlist.get(j);
                    		break;
                    	}
                    }
                    // search resource in the queue 
                    while(tokenizer.hasMoreTokens()){
                        int v2 = Integer.parseInt(tokenizer.nextToken());
                        int N2 = 0;
                    
                        for(int j=0; j< rsize; j++){
                        	if(rQueue[j].getID() == v2){
                        		N2 = j;
                        		break;
                        	}
                        }
                        // add to the node resource list
                        rMap.get(rN1).putResource(rQueue[N2]);
                    }
          		}
          		graphreader.close();
            }catch(Exception e){
            	e.printStackTrace();
            }
            
/*********************************** testing begin ******************************************************/            
/*********************************** testing begin ******************************************************/            
            try{
                PrintWriter graphout = new PrintWriter(new FileWriter("C:/VertexResourceInfotest.txt"));
                graphout.println("" + rsize + " resources");
	        	for(int i=0;i<rsize; i++){
                    // output resource info.
                    graphout.println("" + rQueue[i].getID() + "," + rQueue[i].getSize());
	        	}

                // output vertex-resource info
                graphout.println("" + rMap.size() + " vertice-resource info.");
                int ionode = 0;
    	    	for (routerNode v : Collections.unmodifiableCollection(rMap.keySet())) {
    	    		graphout.print(v.toString() + ",");
    	    		// get the resource list in node v
    	    		Iterator<routerResource> bIter = rMap.get(v).getResource().iterator();
    	    		// record node number that own resource
    	    		if(bIter.hasNext()) ionode++;

    	    		while(bIter.hasNext()){
                    	graphout.print(bIter.next().getID() + ",");
                    }
                    graphout.println();
		        }
                graphout.println(ionode + " nodes have resources...");
        		graphout.close();
            }catch(Exception e){
            	e.printStackTrace();
            }
/*********************************** testing end ******************************************************/            
/*********************************** testing end ******************************************************/            
            
    	}
    	
    	// Calculate cache size
    	long totalResourceSize = 0;
  		for(int i = 0;i < rsize; i++){
  			totalResourceSize += rQueue[i].getSize();
    	}
  		totalResourceSize = totalResourceSize/routerMain.routerCacheSizedenominator; // 5% of all the total size
  		
  		// set cache size
    	for (routerNode v : Collections.unmodifiableCollection(rMap.keySet())) {
    		rMap.get(v).resetCacheSize((int)totalResourceSize);   
    	}

    	System.out.println("Node cache size:" + totalResourceSize);
	}
	

	
	/**
	 * return routerCacheManager's routerTable
	 */
	public Map<routerNode, routerCache> getrouterTable(){
		return rMap;
	}
	
	/**
	 * return routerCacheManager
	 */
	public routerCacheManager getrouterCacheManager(){
		return rCM;
	}

	/**
	 * return resource size
	 */
	public int getresourcesize(){
		return rsize;
	}

	/**
	 * return resource queue
	 */
	public routerResource[] getrQueue(){
		return rQueue;
	}
	
	/**
	 * return graph
	 */
    public Graph<routerNode, routerLink> getgraph(){
    	return gGraph;
    }	
	
	private Graph<routerNode, routerLink> gGraph;
    private routerResource[] rQueue;  // queue for all resources
    private int rsize;                // resource queue size
    //private Random mRandom;           // generate random resource size
    private String filename = "C:/VertexResourceInfo.txt";  // info. file             
    private Map<routerNode, routerCache> rMap;
    private routerCacheManager rCM;
}

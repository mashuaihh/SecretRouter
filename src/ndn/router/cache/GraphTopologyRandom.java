/**
 * @(#)GraphTopologyGenerator.java	V1 24/01/11
 *
 * @author LinSen
 * 
 */
package ndn.router.cache;
import java.util.*;
import java.io.*;

import org.apache.commons.collections15.Factory;


import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * @author Administrator
 *
 */
public class GraphTopologyRandom {
	
	

	/**
	 * produce a random graph 
	 */
    public GraphTopologyRandom(){
    	if(routerMain.iswitch == 0){
    	    rgraph = new ErdosRenyiGenerator<routerNode, routerLink>(
    		    	new GraphFactory(), new VertexFactory(), new EdgeFactory(),routerMain.vertexNum, 6.0/(routerMain.vertexNum -1)).create();// average degree 6.
    	    
    	    // delete vertices whose degrees are zero
    		Set<routerNode> removeMe = new HashSet<routerNode>();
    		for (routerNode v : rgraph.getVertices()) {
                if (rgraph.degree(v) == 0 ) {
                    removeMe.add(v);
                }
            }
    		for(routerNode v : removeMe) {
    			rgraph.removeVertex(v);
    		}
          
    		// check whether the graph is connected
    		if(DistanceStatistics.<routerNode, routerLink>diameter(rgraph) == Double.POSITIVE_INFINITY){
                System.out.println("The graph is not connected!Try again...");      			
    		}

    		// write the graph topology into text file in the form of (V1, V2)
            List<routerNode> vlist = new ArrayList<routerNode>(rgraph.getVertices());
            List<routerLink> elist = new ArrayList<routerLink>(rgraph.getEdges());
            try{
                PrintWriter graphout = new PrintWriter(new FileWriter("C:/GraphTopology.txt"));
                // output vertices
                graphout.println("" + vlist.size() + " vertices");
                for(int i=0; i<vlist.size(); i++){
                	graphout.println(vlist.get(i).toString() 
                			               + "," + "*");
                }
                
                // output edges
                graphout.println("" + elist.size() + " edges");
                for(int i=0; i<elist.size(); i++){
            	    Pair<routerNode> pEndPoints = rgraph.getEndpoints(elist.get(i));
                	graphout.println(pEndPoints.getFirst().toString() 
                			               + "," + pEndPoints.getSecond().toString());
                }
                System.out.println("ER average degree:" + elist.size()*2.0/vlist.size());
                graphout.close();
            }catch(Exception e){
            	e.printStackTrace();
            }
        }
    	// read from the file
    	else{
            try{
            	rgraph = new GraphFactory().create();
            	BufferedReader graphreader = new BufferedReader(new FileReader("C:/GraphTopology.txt"));
            	String line = graphreader.readLine();  
            	StringTokenizer tokenizer;
            	
            	// add vertices
            	if(line == null) return;
          		tokenizer = new StringTokenizer(line, " ");
                int verNumber = Integer.parseInt(tokenizer.nextToken());
          		for(int i = 0;i < verNumber; i++){
            		line = graphreader.readLine();
              		tokenizer = new StringTokenizer(line, ",");
              		rgraph.addVertex(new routerNode(Integer.parseInt(tokenizer.nextToken())));
            	}
            	
          		// add edges
                List<routerNode> vlist = new ArrayList<routerNode>(rgraph.getVertices());
        		line = graphreader.readLine();
          		tokenizer = new StringTokenizer(line, " ");
                int edgeNumber = Integer.parseInt(tokenizer.nextToken());
          		for(int i = 0;i < edgeNumber; i++){
            		line = graphreader.readLine();
              		tokenizer = new StringTokenizer(line, ",");
                    // get vertex No.
                    int v1 = Integer.parseInt(tokenizer.nextToken());
                    int v2 = Integer.parseInt(tokenizer.nextToken());
                    routerNode rN1 = null;
                    routerNode rN2 = null;
                    // search vertex in the list
                    for(int j=0; j< verNumber; j++){
                    	if(((routerNode)vlist.get(j)).getid() == v1){
                    		rN1 = vlist.get(j);
                    	}
                    	if(((routerNode)vlist.get(j)).getid() == v2){
                    		rN2 = vlist.get(j);
                    	}
                    }
                    rgraph.addEdge(new routerLink(v1, v2, 1, 1),
                    		rN1, rN2);
            	}
                System.out.println("ER average degree:" + edgeNumber*2.0/verNumber);
          		// close the file
                graphreader.close();
            }catch(Exception e){
            	e.printStackTrace();
            }
    	}
    }
    
    
	/**
	 * get the random graph
	 */
    public Graph<routerNode, routerLink> getGraph(){
    	return rgraph;    	    	
    }
    
	/**
	 * get the random graph
	 */

    
    
	/**
	 * create UndirectedSparseGraph via factory method
	 */
	class GraphFactory implements Factory<UndirectedGraph<routerNode,routerLink>> {
		public UndirectedGraph<routerNode,routerLink> create() {
			return new UndirectedSparseGraph<routerNode,routerLink>();
		}
	}
	
	/**
	 * create nodes via factory method
	 * 
	 */
	class VertexFactory implements Factory<routerNode> {
		public routerNode create() {
			Vcount++;
			return new routerNode(Vcount);
		}
		
	}
	
	/**
	 * create links via factory method
	 */
	class EdgeFactory implements Factory<routerLink> {
		public routerLink create() {
			Ecount++;
			return new routerLink(0 ,Ecount, 1, 1);
		}
		
	}
	
	private Graph<routerNode, routerLink> rgraph;
	static int Vcount = 0;  //
	static int Ecount = 0;  //
}

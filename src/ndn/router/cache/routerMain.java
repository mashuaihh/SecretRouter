package ndn.router.cache;

/**
 * questions:
 * 1. request node finds cache in its own cache, then how to calculate the hit rate?
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFrame;

import edu.uci.ics.jung.graph.Graph;

public class routerMain {
    public static final int iswitch = 1; // 0-generate new graph; 1-load from file
    public static final int iswitch3 = 0; // 0-generate new resource; 1-load from file
    public static final int powerlawGenerator = 0;  // generating powerlaw sequence 0-new 1-load
    
    public static final int iswitch2 = 0; // for generating resource request// 0-create request 1-load request
    public static final int vertexNum = 96;
    
//    public static final int routerHop = 5; //5 for flat topology
    public static final int routerHop = 4; //10 for hierarchical topology
  
    
    // some constants
    public static final int resourceNum = 1000;  // default 4 6 8 1000
    public static  double EFRThreshold = 0.00157964;  // this is actually set in class DistributionRequestSequence
    public static final double CacheThreshold = 1.0;    // 0.5 0.1
    //public static final double routerCacheSizedenominator = 10; // default= resourceNum/10
    //public static final double routerCacheSizedenominator = 5; // default= resourceNum/10
    //public static final double routerCacheSizedenominator = 3.3; // default= resourceNum/10
    //public static final double routerCacheSizedenominator = 2.5; // default= resourceNum/10
    private double routerCacheSizedenominator = 2; // default= resourceNum/10
    private double aa = -0.3;
    private String algoType = "lcd";
    public double HitRate = 0.0;
    public double PathStr = 0.0;
    public int HitNumber = 0;
    public int extraHop = 0;
    public int extraLoad = 0;
    public static int requestTime = 10000;
//    public static int requestTime = 1;

    public routerMain() {
    	
    }
    
    public void setAlgoType(String str) {
    	this.algoType = str;
    }
    
    public void setCacheSizeDenominator(double num) {
    	this.routerCacheSizedenominator = num;
    }
    
    public void setAA(double aa) {
    	this.aa = aa;
    }

    public void mainPiece() {
		Graph<routerNode, routerLink> graphRandom = new GraphTopologyRandom().getGraph();
        System.out.println("random graph is created");

		routerCacheManager rCacheManager = new routerCacheManager(graphRandom);
        System.out.println("node cache is assigned");

		DistributionResource resDistribution = new DistributionResource(rCacheManager, this.routerCacheSizedenominator);
        System.out.println("resource is assigned");
        
//        JFrame jf = new routerVisulization(graphRandom, resDistribution);
//		jf.pack();
//		jf.setVisible(true);

        DistributionRequestSequence drs = new DistributionRequestSequence(resDistribution);
        // generate powerlaw accees frequency
        drs.powlawGenerate(this.aa);
        // generate router

       routingDijkstraShortestPath rDSP = new routingDijkstraShortestPath(resDistribution, this.algoType);
		// start simulation
	 	//simulationEvent se = sQueue.getEvent();
		int etype;
		simulationEvent se;
		long sTimes = routerMain.requestTime;
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
		this.HitRate = rDSP.getHitRate();
		this.PathStr = rDSP.getPathStr();
		this.HitNumber = rDSP.HitNumber;
		this.extraHop = rDSP.getExtraHop();
		this.extraLoad = rDSP.getExtraLoad();
		routingDijkstraShortestPath.clearStat();
		
        // close file		
		drs.closefile();
    }
    
    public void printResult() {
    	File file = new File("d:\\HitResult.txt");
    	PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fileOut.println("aa : " + this.aa + " denomin: " + this.routerCacheSizedenominator
				+ " Algorithm: " + this.algoType);
		fileOut.println("Hit rate: " + this.HitRate);
		fileOut.println("Hit number: " + this.HitNumber);
		fileOut.println("Request number: " + routerMain.requestTime);
		fileOut.println("Path Stretch: " + this.PathStr);
		fileOut.println("Extra Hop: " + this.extraHop);
		fileOut.println("Extra Load: " + this.extraLoad);
		fileOut.println();
		fileOut.println("----------------------------------------------------");
		fileOut.close();
    }

	/**
	 * 
	 * main method
	 */
	public static void main(String[] args) {
		double[] denomin = {0.1, 0.2, 0.3, 0.4, 0.5};
//		double[] denomin = {0.9};
		double[] aa = {-0.3, -0.9};
//		double[] aa = {-0.9};
		String[] algo = {"lcd", "ccn", "cls", "cls+"};
//		String[] algo = {"cls+"};
//		String[] algo = {"cls", "cls+"};
		
		for (double each_aa : aa) {
			for (double de : denomin) {
				for (String al : algo) {
					routerMain main = new routerMain();
					main.setAA(each_aa);
					main.setCacheSizeDenominator(de);
					main.setAlgoType(al);
					main.mainPiece();
					main.printResult();
				}
			}
		}
	}

}

/**
 * generate request sequence
 */
package ndn.router.cache;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Arrays;

/**
 * @author Administrator
 *
 */
public class DistributionRequestSequence {

	/**
	 * 
	 */
	public DistributionRequestSequence(DistributionResource DR) {
		this.DR = DR;
        // get the node table
        this.rTable = new ArrayList<routerNode>(Collections.unmodifiableCollection(DR.getrouterTable().keySet()));
        this.rTablesize = rTable.size();
        this.resourceNum = DR.getresourcesize();
        this.rQueue = DR.getrQueue();
        this.rMap = DR.getrouterTable();
        mRandom = new Random();
        routerRandom = new Random();

/**
 * 
 * find eligible requestNodes and put them into requestNodeList
	    	 */
        for (routerNode n : this.rTable) {
        	if (n.getid() != 0 && n.getHop() >= routerMain.routerHop) {
        		this.requestNodeList.add(n);
        	}
        }

	mapResource(); 
        
        reRandom = new Random();
        
        try{
    	    if(routerMain.iswitch2 == 0){
                graphout = new PrintWriter(new FileWriter(filename));
    	    }
    	    else if (routerMain.iswitch2 == 1){
            	graphreader = new BufferedReader(new FileReader(filename));
                graphout2 = new PrintWriter(new FileWriter("C:/RequestSequenceTest.txt"));
    	    }
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	/**
	 * resources are mapped to nodes
	 */
	public void mapResource() {

		//copy rQueue
		List<routerResource> sampleList = new ArrayList<routerResource>();
		for (int i = 0; i < rQueue.length; i++) {
			routerResource res = rQueue[i];
			sampleList.add(res);
		}
		
		//calculate resources number in each node
		int eachNodeHasResourceNum = routerMain.resourceNum / this.requestNodeList.size();
		
		//distribute resources in sampleList to resourceNodeMap
		for (routerNode node : this.requestNodeList) {

			for (int j = 0; j < eachNodeHasResourceNum; j++) {
				Random ran = new Random();
				int ranInt = ran.nextInt(sampleList.size());
				routerResource resourceRan = sampleList.get(ranInt);
				//put into hashmap
				this.resourceNodeMap.put(resourceRan, node);
				//delete the already allocated resource in sampleList
				sampleList.remove(resourceRan);
			}
		//end of for (routerNode node : requestNodeList)
		}
		
		while (this.resourceNodeMap.size() < routerMain.resourceNum) {
			Random rand = new Random();
			int randInt = rand.nextInt(this.requestNodeList.size());
			routerNode node = this.rTable.get(randInt);
			for (routerResource res : sampleList) {
				this.resourceNodeMap.put(res, node);
			}
		}
	}
	
	

	/**
	 * generate power law
	 */
	public void powlawGenerate(double aa){
		//a = -0.3 -0.9
		int Kmin = 5;
		int Kmax = 100;
		double[] df = new double[Kmax - Kmin + 2];
		int acceessfrequence[] = new int[routerMain.resourceNum]; 
		
		
		// calculate access frequence
		mRandom = new Random();
		long accesssum = 0;
		if(routerMain.powerlawGenerator == 0){
    		// calculate coefficient 
    		double sum = 0.0;
    		for(int i=Kmin; i<=Kmax; i++){
    			sum += Math.pow(i, aa);
    		}
		
    		df[0] = 0;
    		for(int i=1; i<Kmax - Kmin + 2; i++){
	    		df[i] = df[i-1] +  Math.pow(i + Kmin - 1, aa)/sum;
    		}
	
    		PrintWriter accessout = null;
	        try{
	        	  accessout = new PrintWriter(new FileWriter("C:/accessout.txt"));
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
			
	    	for(int i=0; i<routerMain.resourceNum; i++){
    			double p = mRandom.nextDouble();
    			// search p
    			int index = 1;
    			while(p > df[index]) index++;
    			acceessfrequence[i] = Kmin + index - 1;
	    		accesssum+=acceessfrequence[i];
	    		accessout.println(acceessfrequence[i]);   /// output for later loading
	    	}
	    	// close the file
	        try{
	        	  accessout.close();
	        }catch(Exception e){
	        	e.printStackTrace();
	        }

		}
		else{
			BufferedReader accessreader = null;
			try{
			    accessreader = new BufferedReader(new FileReader("C:/accessout.txt"));
			}catch(Exception e){
				e.printStackTrace();
			}	
			
			// load from file
			String line	= null;
			StringTokenizer tokenizer = null;
    		for(int i=0; i<routerMain.resourceNum; i++){
    			try{
    			    line = accessreader.readLine();
    			}catch(Exception e){
    				e.printStackTrace();
    			}
	    		tokenizer = new StringTokenizer(line);
	            // get vertex No.
	    		acceessfrequence[i] = Integer.parseInt(tokenizer.nextToken());
	    		accesssum+=acceessfrequence[i];
			}
	    	// close the file
	        try{
	        	accessreader.close();
	        }catch(Exception e){
	        	e.printStackTrace();
	        }

		}
		
        acceessprobability = new double[routerMain.resourceNum]; 		
		// calculate access probability
		for(int i=0; i< routerMain.resourceNum;i++)
			acceessprobability[i] = acceessfrequence[i]/(double)accesssum;
		
/////////////////////////////////////////////////////////////////////////////////////
////////////////////get the sorted array/////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

		double[] tempArray = new double[routerMain.resourceNum];
		System.arraycopy(acceessprobability, 0, tempArray, 0, routerMain.resourceNum);
		Arrays.sort(tempArray);
		try{
		PrintWriter sortedout = new PrintWriter(new FileWriter("C:/acceessprobability.txt"));
		for(int i=0; i<routerMain.resourceNum; i++){
			sortedout.println(tempArray[i] + "-" + i);
		 }
		sortedout.close();
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		// set EFR routerMain.EFRThreshold
		routerMain.EFRThreshold = tempArray[routerMain.resourceNum * 95/100];
		System.out.println("routerMain.EFRThreshold" + routerMain.EFRThreshold);
		
		
		/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
		
		// generate access frequency
		requestFrequency = new double[routerMain.resourceNum];
		requestFrequency[0] = acceessprobability[0];
		for(int i=1; i< routerMain.resourceNum; i++){
			requestFrequency[i] = requestFrequency[i-1] + acceessprobability[i];
		}
		// fix the float number error
		requestFrequency[routerMain.resourceNum -1] = 1.0;
		
		// compute coefficient of cache probability
		Qcache = 0.0;
		QcacheSquareroot = 0.0;
		for(int i=0;i<routerMain.resourceNum;i++){
			Qcache += Math.pow(acceessprobability[i], 2); 
			QcacheSquareroot += Math.pow(acceessprobability[i], 1.5);
		}
		Qcache = routerMain.CacheThreshold/Qcache;
		QcacheSquareroot = routerMain.CacheThreshold/QcacheSquareroot;
		
		/////////////////////////////////////////////////////////////////////////////////////
		//////////////// cache probability are set here   ///////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////
		for(int i=0;i<routerMain.resourceNum;i++){
			//rQueue[i].setcacheprobability(Qcache*acceessprobability[i]);   // proportional to accessprobability
			rQueue[i].setcacheprobability(routerMain.CacheThreshold);  //
			rQueue[i].setaccessprobability(acceessprobability[i]);
		}
		
		
		
	}
	
	
	/**
	 * generate an event
	 */
	public simulationEvent eventgenerate(){
		etime++;
		simulationEvent e = null;
	    if(routerMain.iswitch2 == 0){
	    	// produce a resource request
//	    	double pp = reRandom.nextDouble();
	    	
	    	//generate 10 double numbers and pick out the largest
	    	//to choose a high popularity resource
	    	List<Double> doubleList = new ArrayList<Double>();

	    	if (DistributionRequestSequence.times % 5 == 0 || DistributionRequestSequence.times % 5 == 1 || DistributionRequestSequence.times % 5 == 2 ) {
	    		double m = reRandom.nextDouble();
	    		doubleList.add(m);
	    	} else {
	    		for (int i = 0; i < (routerMain.resourceNum / 500); i++) {
	    		double m = reRandom.nextDouble();
	    		doubleList.add(m);
	    		}
	    	}

	    	double largest = 0.0;
	    	for (Double d : doubleList) {
	    		if (d > largest) {
	    			largest = d;
	    		}
	    	}
	    	
			int index = 0;  
			while(largest > requestFrequency[index]) index++; // so index is a picked random number
			//choose a random resource
	    	routerResource rR = rQueue[index];
//			int ran = new Random().nextInt(10);
//	    	routerResource rR = rQueue[ran];

	    	/**
	    	 * choose requestNode according to resource
	    	 */
	    	routerNode requestNode = null;
	    	if (DistributionRequestSequence.times % 3 == 0 || DistributionRequestSequence.times % 3 == 1 ) {
	    		Random dRan = new Random();
	    		int ran = dRan.nextInt(this.requestNodeList.size());
	    		requestNode = this.requestNodeList.get(ran);
	    	} else {
	    		requestNode = this.resourceNodeMap.get(rR);
	    	}
	    	DistributionRequestSequence.times++;

	    	if (DistributionRequestSequence.times == 6) {
	    		DistributionRequestSequence.times = 0;
	    	}
	    	
	    	/**
	    	 * set destination to server node 0
	    	 */
	    	routerNode destinationNode = null;
	    	for (routerNode v : rTable) {
	    		routerCache cache = rMap.get(v);
	    		if (cache.isServer()) {
	    			destinationNode = v;
	    		}
	    	}
	    	
	    	// (object, request)
    		e = new simulationEvent(etime, 0, requestNode, rR, destinationNode);
    		
/*    	    		try{
    	    		    graphout.println("" + e.getEventTime()
    	    			    	        + "," + e.getEventNode().getid()
    	                               + "," + e.getrouterResource().getID()
    	                               + "," + vtemp.getid());
    	 		}catch(Exception exc){
    	 			exc.printStackTrace();
    	        }
*/
	    }
	    else if (routerMain.iswitch2 == 1){
	    	String line = null;
    		try{
    	   		line = graphreader.readLine();
    		}catch(Exception exc){
    			exc.printStackTrace();
            }
    		StringTokenizer tokenizer = new StringTokenizer(line, ",");
            // get vertex No.
            int etime = Integer.parseInt(tokenizer.nextToken());
            int v1 = Integer.parseInt(tokenizer.nextToken());
            int v2 = Integer.parseInt(tokenizer.nextToken());
            routerNode rN1 = null;
            int N2 = 0;
            // search vertex in the list
            for(int j=0; j< rTablesize; j++){
            	if(rTable.get(j).getid() == v1){
            		rN1 = rTable.get(j);
            		break;
            	}
            }
            // search resource in the queue 
            for(int j=0; j< resourceNum; j++){
            	if(rQueue[j].getID() == v2){
            		N2 = j;
            		break;
            	}
            }

            // get the nodes containing a resource
	    	routerResource rR = rQueue[N2];
	    	routerNode vtemp = null; 
	    	for(routerNode v : rTable){
 	    		if(rMap.get(v).getResource().contains(rR)){
	    			vtemp = v;
	    			break;
        		}
	    	}

	    	e = new simulationEvent(etime, 0, 
    				rN1, 
		    		rQueue[N2], vtemp);
    		
/////////////////////////////////testing begin//////////////////////////////////////////////////////////////////
    		//////////////////////////////////////////////////////////////////////////////
	    	/*
    		try{
    		    graphout2.println("" + e.getEventTime()
    			    	        + "," + e.getEventNode().getid()
                                + "," + e.getrouterResource().getID()
                                + "," + vtemp.getid());
    		}catch(Exception exc){
    			exc.printStackTrace();
            }
    		*/
////////////////////////////////testing end//////////////////////////////////////////////////////////////////
    		///////////////////////////////////////////////////////////////////////////////

	    }	    
       	return e;	
	}
	
	
	
	/**
	 * close the file
	 */
	public void closefile(){
		try{
    	    if(routerMain.iswitch2 == 0){
	        	graphout.close();
	        }
	        else if (routerMain.iswitch2 == 1){
	    	    graphreader.close();
	    	    graphout2.close();
	        }
		}catch(Exception exc){
			exc.printStackTrace();
        }
	}
	
	/**
	 * close the file
	 */
    public long getTotalRequestTimes(){
    	return etime;
    }
    
	/**
	 * get accessprobability
	 */
    public double getaccessprobability(int i){
    	return acceessprobability[i];
    }
    
	/**
	 * get coefficient of cache probability
	 */
    public double getcachecoefficient(){
    	return Qcache;
    }
    
	/**
	 * get coefficient of square root cache probability 
	 */
    public double getcachesquarecoefficient(){
    	return QcacheSquareroot;
    }
    
    private List<routerNode> rTable;
	private int resourceNum;
	Random mRandom;
	Random reRandom;  // source request
	Random routerRandom; // router request
	private long etime = 0;
	private DistributionResource DR;
    private routerResource[] rQueue;  // queue for all resources
    private int rTablesize;  // queue for all resources
    private PrintWriter graphout;
    private PrintWriter graphout2;
    private BufferedReader graphreader;
    private String filename = "C:/RequestSequence.txt";
    private Map<routerNode, routerCache> rMap;
    private double[] requestFrequency;    // for generating access sequence
    private double acceessprobability[];  // store access frequency
    private double Qcache;                // coefficient of cache probability
    private double QcacheSquareroot;          // coefficient of square root cache probability 
    private List<routerNode> requestNodeList = new ArrayList<routerNode>();
    private Map<routerResource, routerNode> resourceNodeMap = new HashMap<routerResource, routerNode>();
    public static long times = 0;
    
    public static void main(String[] args) {
    	int j = 0;
    	for (int i = 100000; i > 0; i--) {
    		if (i % 1000 == 0 ) {
    			j++;
    		}
    	}
    	System.out.println(j);
    }
}

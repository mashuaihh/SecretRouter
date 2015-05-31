package ndn.router.newalgo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ndn.router.cache.routerCache;
import ndn.router.cache.routerNode;
import ndn.router.cache.routerResource;
import ndn.router.cache.simulationEvent;

/**
 * 
 * @author user
 * 		 0
 *       1
 *    2     3
 *          4 
 */

public class Testing {
	private static int requestTime = 100;
	private static int cacheSize = 10;
	private static int nodeNumber = 5;
	private List<routerNode> nodeList = new ArrayList<routerNode>();
	private Map<routerNode, routerCache> routerCacheTable = new HashMap<routerNode, routerCache>();		
	private routerNode serverNode;
	private int[] resourceSizeList = {1, 2, 3, 4, 5, 6, 7, 8, 9};
	private List<routerResource> resourceList = new ArrayList<routerResource>();
	
	public Testing() {
		init();
	}
	
	private void init() {
		for (int i = 0; i < Testing.nodeNumber; i++) {
			routerNode node = new routerNode(i);
			this.nodeList.add(node);
			node.setHop(1);
			routerCache cache = new routerCache(Testing.cacheSize, 0);
			if (i == 0) {
				serverNode = node;
			}
			this.routerCacheTable.put(node, cache);
		}
		this.initResource();
		this.setServer();
		this.initTuple();
	}
	
	private void initTuple() {
		routerResource[] rQueue = new routerResource[this.resourceSizeList.length];
		for (int i = 0; i < rQueue.length; i++) {
			routerResource each_resource = this.resourceList.get(i);
			rQueue[i] = each_resource;
			System.out.println("init resource " + each_resource.getID());
		}
	        	/**
	        	 * Initial the tuple of every resource in every node.
                 * Initial the ResourceCountList of every node.
	        	 * @author Mashuai
	        	 */
	        	for (routerNode e : nodeList) {
	        		e.addInTupleList(rQueue);
	        		routerCache each_cache = this.getCache(e);
	        		each_cache.addInResourceCountList(rQueue);
	        	}
	}
	
	private void initResource() {
		int i = 0;
		for (int num : this.resourceSizeList) {
			routerResource resource = new routerResource(i, num);
			this.resourceList.add(resource);
			i++;
		}
	}
	
	private void setServer() {
		routerCache serverCache = this.getCache(serverNode);
		serverCache.setServer();
	}
	
	private routerCache getCache(routerNode node) {
		routerCache cache = this.routerCacheTable.get(node);
		return cache;
	}
	
	private routerNode getNodeById(int i) {
		for (routerNode e : this.nodeList) {
			if (e.getid() == i) {
				return e;
			}
		}
		return null;
	}
	
	private List<routerNode> getVList(int nodeId) {
		List<routerNode> vlist = new ArrayList<routerNode>();
		String path;

		switch (nodeId) {
		case 0: path = "";break;
		case 1: path = "10";break;
		case 2: path = "210";break;
		case 3: path = "310";break;
		case 4: path = "4310";break;
		default : path = "";
		}

		char[] numbers = path.toCharArray();
		
		for (char ch : numbers) {
			int intNumber = Character.getNumericValue(ch);
			routerNode node = getNodeById(intNumber);
			vlist.add(node);
		}
		
		return vlist;
	}
	
	public void generateEvent() {
		Random rand = new Random();
		int idx = rand.nextInt(Testing.nodeNumber - 1) + 1;
		routerNode requestNode = this.getNodeById(idx);
		
		int resIdx = rand.nextInt(this.resourceList.size());
		routerResource resource = this.resourceList.get(resIdx);
		
		simulationEvent se = new simulationEvent(0, 0, requestNode, resource, this.serverNode);
		List<routerNode> vlist = this.getVList(idx);
		
		Cls algo = new Cls(se, vlist, this.routerCacheTable);
		algo.routing();
		algo.showPath();

	}
	
	public void testByHand() {
		List<String> testList = new ArrayList<String>();
		//"1,2" 1 stands for requestNode id, 2 stands for resource size.
/**
 * 
 * @author user
 * 		 0
 *       1
 *    2     3
 *          4 
 */
		testList.add("4,3");
		testList.add("4,3");
		testList.add("4,3");
		testList.add("4,3");
		testList.add("4,9");
		testList.add("4,9");
		testList.add("4,9");
		testList.add("4,3");
		testList.add("4,9");
		
		this.processTestList(testList);
	}
	
	private void processTestList(List<String> list) {
		for (String str : list) {
			int idxDot = str.indexOf(",");
			String requestIdStr = str.substring(0, idxDot);
			int requestNodeId = Integer.parseInt(requestIdStr);
			
			String resourceIdStr = str.substring(idxDot + 1);
			int resourceSize = Integer.parseInt(resourceIdStr);
			
			routerNode requestNode = this.getNodeById(requestNodeId);
			routerResource resource = this.getResourceBySize(resourceSize);
			
			simulationEvent se = new simulationEvent(0, 0, requestNode, resource, this.serverNode);
			List<routerNode> vlist = this.getVList(requestNodeId);
			
			Cls algo = new Cls(se, vlist, this.routerCacheTable);
			algo.showPathBeforeRouting();
			algo.routing();
			algo.showPathAfterRouting();
			
		}
	}
	
	private routerResource getResourceBySize(int size) {
		for (routerResource e : this.resourceList) {
			if (e.getSize() == size) {
				return e;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		Testing ts = new Testing();
		ts.testByHand();
//		int i = 0;
//		while (i < Testing.requestTime) {
//			ts.generateEvent();
//			i++;
//		}
	}

}

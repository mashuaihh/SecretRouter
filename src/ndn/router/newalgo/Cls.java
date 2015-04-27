package ndn.router.newalgo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;
import ndn.router.cache.routerCache;
import ndn.router.cache.routerNode;
import ndn.router.cache.routerResource;
import ndn.router.cache.routerTuple;
import ndn.router.cache.simulationEvent;

public class Cls extends NewAlgo {
	
	public Cls(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		super(se, vlist, rMap);
	}
	
	public void routing() {
		routerNode firstTupleNode = getTupleNode();

		if (firstTupleNode == null) {
			//no tuple appears during the trail to server
			//Need to cache? Yes
			routerNode serverNode = super.getServer();
			routerNode downNode = super.getLowerNode(serverNode);
			realList = vlist;
			printPath();
			routerTuple downTuple = downNode.getTuple(rResource);
			downTuple.isValid();
			downTuple.setInNode(null);
			//caching

		} else {
			//Tuple is found on the way to server
			routerNode firstNode = vlist.get(0);

			if (firstNode == firstTupleNode) {
				//The fisrt firstTupleNode contains the tuple
				//Need to cache? No
				routerNode destiNode = findEndOfTrail(firstNode);
				if (!(realList.size() > vlist.size())) {
					realList = vlist;
				}
				printPath();

			} else {
				//Not the first firstTupleNode in vlist contains the tuple.
				//Tuple is found on half way to server.
				//Need to cache? Yes
				
				//for showing path, ie making the realList
				for (int i = 0; i < vlist.size() - 1; i++) {
					//changing the realList
					routerNode thisNode = vlist.get(i);
					routerNode nextNode = vlist.get(i + 1);
					realList.add(thisNode);
					if (nextNode == firstTupleNode) {
						break;
					} 
				}
				//findEndOfTrail changes the realList
				routerNode destiNode = findEndOfTrail(firstTupleNode);
				routerNode beforeTuple = super.getLowerNode(firstTupleNode);
				if (!(realList.size() > vlist.size())) {
					realList = vlist;
				}
				printPath();
				
				//for caching

			}
		}

	}
	
	/**
	 * Highly dangerous function, only resort to the first item
	 * of the outList.
	 * The realList is changed in this function.
	 * @param node
	 * @return
	 */
	private routerNode findEndOfTrail(routerNode node) {
		//add to the realList
		realList.add(node);
		
		routerTuple tuple = node.getTuple(rResource);
		List<routerNode> outList = tuple.getOutNodes();
		if (outList.size() == 0) {
			return node;
		} else {
			routerNode outNode = outList.get(0);
			return findEndOfTrail(outNode);
		}
	}
	
	private void caching(routerNode firstTupleNode, routerNode downNode) {
		routerCache cache = super.getCache(firstTupleNode);
		routerCache downCache = super.getCache(downNode);
		
		routerTuple firstTuple = firstTupleNode.getTuple(rResource);
		
		if (!cache.isServer()) {
			// if size == 0, then the firstTupleNode contains the cache.
			if (firstTuple.getOutNodes().size() == 0) {
				cache.removeResource(rResource);
				downCache.putResource(rResource);
			} else {
				//firstTuple has branch out. And the cache is found there.
				downCache.putResource(rResource);
			}
			cache.removeResource(rResource);
			downCache.putResource(rResource);
		}
		processOutResources(downNode);
	}
	
	private routerNode processOutResources(routerNode node) {
		routerCache cache = super.getCache(node);
		if (cache.isServer() || cache.isOutResourceListEmpty()) {
			return null;
		} else {
			routerNode upNode = super.getUpperNode(node);
			routerTuple tuple = node.getTuple(rResource);
			routerCache upCache = super.getCache(upNode);
			List<routerResource> resList = cache.getOutResourceList();
			for (routerResource e : resList) {
				tuple.deleteTuple();
				upCache.scheduleLRU(e, upNode);
				cache.removeOutResource(e);
			}
			return processOutResources(upNode);
		}
	}
	
	private void printPath() {
		File file = new File("c:\\clsResult.txt");
		PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fileOut.println("Resourse is " + super.rResource.getID()+ " size is " + super.rResource.getSize());
		fileOut.print("Orig Path: ");
		for (int i = 0; i < vlist.size(); i++) {
			routerNode node = vlist.get(i);
			if (i == (vlist.size() - 1)) {
				fileOut.println(node);
			} else {
				fileOut.print(node + " -> ");
			}
		}
		
		for (int i = 0; i < vlist.size(); i++) {
			routerNode node = vlist.get(i);
			String tupleStr = makeString(node);
			if (i == (vlist.size() - 1)) {
				fileOut.println(tupleStr);
			} else {
				fileOut.print(tupleStr + " -> ");
			}
		}
		
		fileOut.print("Real Path: ");
		for (int i = 0; i < super.realList.size(); i++) {
			routerNode node = super.realList.get(i);
			if (i == (super.realList.size() - 1)) {
				fileOut.println(node);
			} else {
			fileOut.print(node + " -> ");
			}
		}

		for (int i = 0; i < super.realList.size(); i++) {
			routerNode node = super.realList.get(i);
			String tupleStr = makeString(node);
			if (i == (super.realList.size() - 1)) {
				fileOut.println(tupleStr);
			} else {
				fileOut.print(tupleStr + " -> ");
			}
		}

		fileOut.println();
		fileOut.close();
	}
	
	/**
	 * Search only non-server nodes
	 * @return null if no tuple can be found, then the server is end node.
	 * return a node containing the tuple.
	 */
	private routerNode getTupleNode() {
		for (int i = 0; i < vlist.size() - 1; i++) {
			routerNode eachNode = vlist.get(i);
			routerTuple eachTuple = eachNode.getTuple(rResource);
			if (eachTuple.isValid()) {
				return eachNode;
			}
		}
		return null;
	}
	
	public String makeString(routerNode node) {
		routerTuple tuple = node.getTuple(rResource);
		if (!tuple.isValid()) {
			return "INVALID";
		}
		List<routerNode> li = tuple.getOutNodes();
		routerNode InNode = tuple.getInNode();
		int hop = node.getHop();
		String list = "[";
		if (li.size() == 0) {
			list = "null";
		} else {
		for (int i = 0; i < li.size(); i++) {
			routerNode each = li.get(i);
			if (i == li.size() - 1) {
				list = list + each + "]";
			} else {
				list = list + each + ",";
			}
		}
		}
		String end = "(" + list + ", " + InNode + ", " + hop + ")";
		return end;
	}
	
	public static void main(String[] args) {
		List<Integer> lili = new ArrayList<Integer>();
		for (int i = 0; i < 3; i++) {
			lili.add(i);
		}
		int a2 = 2;
		int hop = 3;
	}
	

}

package ndn.router.newalgo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;
import ndn.router.cache.routerCache;
import ndn.router.cache.routerNode;
import ndn.router.cache.routerResource;
import ndn.router.cache.routerTuple;
import ndn.router.cache.simulationEvent;

public class Cls extends NewAlgo {
	
	private routerNode firstTupleNode = getTupleNode();

	public Cls(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		super(se, vlist, rMap);
//		super.addRequestNum();
	}
	
	public void routing() {

		if (firstTupleNode == null) {
			//no tuple appears during the trail to server
			//Need to cache? Yes
			routerNode serverNode = super.getServer();
			routerNode downNode = super.getLowerNode(serverNode);
			realList = vlist;
			printPath();
//			caching(serverNode, downNode);
			this.serverPullDown(serverNode);

		} else {
			//Tuple is found on the way to server
			//First node in vlist is the firstTupleNode
			routerNode firstNode = vlist.get(0);

			if (firstNode == firstTupleNode) {
				//The fisrt firstTupleNode contains the tuple
				//Need to cache? No
				findEndOfTrail(firstNode);
				if (realList.size() > vlist.size()) {
					realList = vlist;
				}
				printPath();
					super.addHitNum();

			} else {
				//Not the first firstTupleNode in vlist contains the tuple.
				//Tuple is found on half way to server.
				//Need to cache? Yes
				
				routerNode downNode = super.getLowerNode(firstTupleNode);
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
				findEndOfTrail(firstTupleNode);
				if (realList.size() > vlist.size()) {
					realList = vlist;
					return;
				}
				printPath();
				
				super.addHitNum();
				//for caching
//				caching(firstTupleNode, downNode);
				this.tupleNodePullDown();
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
	public routerNode findEndOfTrail(routerNode node) {
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

	private void serverPullDown(routerNode server) {
			routerNode lowerNode = super.getLowerNode(server);
			routerCache lowerCache = super.getCache(lowerNode);
			routerTuple lowerTuple = lowerNode.getTuple(rResource);

			//has enough space to cache this resource.
			//if not, do nothing.
			if (lowerCache.getSize() < rResource.getSize()) {
				return;
			}

			//see if there's enough remaining space to cache
			//not enough
			if (!lowerCache.hasEnoughRemainingCacheSize(rResource)) {

				List<routerResource> replacedResourceList =
						lowerCache.saveThisResource(rResource, true);

				List<routerResource> replacedRealResourceList = new ArrayList<routerResource>();
				
				if (replacedResourceList != null) {
					//cache this resource, this resource is hotter than 
					//all to be ousted resources
					for (routerResource e : replacedResourceList) {
						routerResource resource = lowerCache.getResourceById(e.getID());
						lowerCache.removeResource(resource);

						//update tuple after removing
//						routerTuple removedTuple = lowerNode.getTuple(e);
//						removedTuple.deleteTuple();

						replacedRealResourceList.add(resource);
					}

					//cache the resource
					if (lowerCache.scheduleLRU(rResource, lowerNode)) {
						lowerTuple.setValid();
						lowerTuple.setInNode(server);
					}

					//oust all the replaced resources
					for (routerResource e : replacedRealResourceList) {
						oustedResourceToSource(lowerNode, e);
					}
				}
				
			} else {
				//has enough remaining space for this resource
					lowerCache.scheduleLRU(rResource, lowerNode);
					lowerTuple.setValid();
					lowerTuple.setInNode(server);
			}
	}
	
	public void tupleNodePullDown() {
		routerNode lowerNode = super.getLowerNode(firstTupleNode);
		routerCache lowerCache = super.getCache(lowerNode);
		routerTuple lowerTuple = lowerNode.getTuple(rResource);

		if (lowerCache.getSize() < rResource.getSize()) {
				return;
		}
		
		if (!lowerCache.hasEnoughRemainingCacheSize(rResource)) {
			
				List<routerResource> replacedResourceList =
						lowerCache.saveThisResource(rResource, true);
				//to store the real routerResource objects.
				List<routerResource> replacedRealResourceList = new ArrayList<routerResource>();

				if (replacedResourceList != null) {

					for (routerResource e : replacedResourceList) {
						routerResource resource = lowerCache.getResourceById(e.getID());
						lowerCache.removeResource(resource);

						replacedRealResourceList.add(resource); 
					}

					//cache the resource
					lowerCache.scheduleLRU(rResource, lowerNode);

					//update tuple
					//delete rResource in firstTupleNode
					updateTupleInfo(this.firstTupleNode);

					//oust all the replaced resources
					for (routerResource e : replacedRealResourceList ) {
//						oustedResourceToSource(firstTupleNode, e);
						oustedResourceToSource(lowerNode, e);
					}

				}

		} else {
			lowerCache.scheduleLRU(rResource, lowerNode);
			updateTupleInfo(firstTupleNode);
		}
	}
	
	/**
	 * Oust this resource in this node, route the resource to its source.
	 * @param node
	 * @param resource
	 */
	public void oustedResourceToSource(routerNode node, routerResource resource) {
		routerCache cache = super.getCache(node);
		routerTuple tuple = node.getTuple(resource);
		
		//1.find inNode from tuple 
		routerNode inNode = tuple.getInNode();
		routerCache inCache = super.getCache(inNode);
		routerTuple inTuple = inNode.getTuple(resource);
		
		//2.delete tuple for this node
		tuple.deleteTuple();

		//3.if inNode is server, end
		if (inCache.isServer()) {
			return;
		}

		//4.update inNode tuple
		//if has multiple outs, delete the out in outList
		//if has not multi, clear out 
			List<routerNode> outList = inTuple.getOutNodes();
			if (outList.size() > 1) {
				outList.remove(node);
				return;
			} else {
				outList.clear();
			}

		//5.has space for resource?
		if (!inCache.hasEnoughRemainingCacheSize(resource)) {
			//No enough space, need to evict other resources.

			List<routerResource> replacedResourceList =
					inCache.saveThisResource(resource, true);
			
				//remove all to be replaced resources in this node.
				for (routerResource e : replacedResourceList) {
//					routerResource resourceEach = cache.getResourceById(e.getID());
					routerResource resourceEach = inCache.getResourceById(e.getID(), true);
					inCache.removeResource(resourceEach);
				}

				//cache the resource
				inCache.scheduleLRU(resource, inNode);
				
				//ousted the replaced resources
				for (routerResource e : replacedResourceList) {
					routerResource resourceEach = inCache.getResourceById(e.getID(), true);
					oustedResourceToSource(inNode, resourceEach);
				}


			//else, ie the inNode has enough space to cache the resource
		} else {
			//inCache.hasEnoughRemainingCacheSize(rResource)
			inCache.scheduleLRU(resource, inNode);
			
			//update tuple in inNode
			inTuple.getOutNodes().clear();
			
			return;
		}

	}
	
	private void updateTupleInfo(routerNode node) {
		routerCache cache = super.getCache(node);
		routerTuple tuple = node.getTuple(rResource);
		
		cache.removeResource(rResource);

		routerNode lowerNode = super.getLowerNode(node);
		routerCache lowerCache = super.getCache(lowerNode);
		routerTuple lowerTuple = lowerNode.getTuple(rResource);
		
		tuple.addOutNodes(lowerNode);

		lowerTuple.setValid();
		lowerTuple.setInNode(node);
		
	}
	

	private void printOut(List<routerResource> eList, routerNode node) {
		File file = new File("d:\\clsResult.txt");
		PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileOut.print(node.getid() + " is removing ");
		for (routerResource ss : eList) {
			fileOut.print(ss.getID() + ", ");
		}

		fileOut.println();
		fileOut.println();
		fileOut.println("---------------------------");
		fileOut.close();
	}
	
	protected void printPath() {
		File file = new File("d:\\clsResult.txt");
		PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fileOut.println("Resource is " + super.rResource.getID()+ " size is " + super.rResource.getSize());
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
	
	public void showPathBeforeRouting() {
		out.println("Requesting resource " + this.rResource.getID() + " size is " + this.rResource.getSize());
		out.print("Path to Server: ");
		//print path
		for (int i = 0; i < vlist.size(); i++) {
			routerNode each = vlist.get(i);
			if (i == vlist.size() - 1)
				out.println(each.getid());
			else 
				out.print(each.getid() + " -> ");
		}
		//print tuple
		for (int i = 0; i < vlist.size(); i++) {
			routerNode each = vlist.get(i);
			if (i == vlist.size() - 1)
				out.println(this.makeString(each));
			else 
				out.print(this.makeString(each) + " -> ");
		}
	}

	public void showPathAfterRouting() {
		out.print("Real path:      ");
		//print path
		for (int i = 0; i < realList.size(); i++) {
			routerNode each = realList.get(i);
			if (i == realList.size() - 1) 
				out.println(each.getid());
			else 
				out.print(each.getid() + " -> ");
		}
		//print tuple
		for (int i = 0; i < realList.size(); i++) {
			routerNode each = realList.get(i);
			if (i == realList.size() - 1) {
				out.println(this.makeString(each));
				out.println();
			}
			else 
				out.print(this.makeString(each) + " -> ");
		}
	}
	
	public String makeString(routerNode node) {
		routerTuple tuple = node.getTuple(rResource);
		routerCache cache = super.getCache(node);
		if (cache.isServer()) {
			return "Server";
		}
		if (!tuple.isValid()) {
			return "INVALID";
		}  
		List<routerNode> li = tuple.getOutNodes();
		routerNode InNode = tuple.getInNode();
		int hop = node.getHop();
		String list = "[";
		if (li.size() == 0) {
			list = "[]";
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
		String end = "(" + InNode + ", " + list + ", " + hop + ")" 
		+ " " + cache.hasResource(rResource);
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

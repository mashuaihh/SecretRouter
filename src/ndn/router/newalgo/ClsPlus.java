package ndn.router.newalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ndn.router.cache.routerCache;
import ndn.router.cache.routerNode;
import ndn.router.cache.routerResource;
import ndn.router.cache.routerTuple;
import ndn.router.cache.simulationEvent;

public class ClsPlus extends Cls {
//public class ClsPlus extends NewAlgo {
	private List<routerNode> firstTupleToEnd = new ArrayList<routerNode>();
	private routerNode serverNode = super.getServer();
	private routerNode firstTupleNode = getTupleNode();
			
	public ClsPlus(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
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
				super.findEndOfTrail(firstNode);
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
				super.findEndOfTrail(firstTupleNode);
				if (realList.size() > vlist.size()) {
					realList = vlist;
					addResourceCountInRealList();
					return;
				}
				printPath();
				
				super.addHitNum();
				//for caching
				this.tupleNodePullDown();
			}
		}
		
		addResourceCountInRealList();
	}
	
	private void addResourceCountInRealList() {
		for (routerNode node : this.realList) {
			routerCache cache = super.getCache(node);
			cache.addResourceCount(rResource);
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
						lowerCache.saveThisResource(rResource);

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
				} else {
					//list == null, no need to cache this resource
					return;
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
						lowerCache.saveThisResource(rResource);
				//to store the real routerResource objects.
				List<routerResource> replacedRealResourceList = new ArrayList<routerResource>();

				if (replacedResourceList != null) {

					//cache this resource, this resource is hotter than 
					//all to be ousted resources
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
						oustedResourceToSource(lowerNode, e);
					}

				//No need to cache this resource in lowerNode.
				} else {
					return;
				}

		//has enough space to cache
		} else {
			lowerCache.scheduleLRU(rResource, lowerNode);
			updateTupleInfo(firstTupleNode);
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
					inCache.saveThisResource(resource);
			
			if (replacedResourceList != null) {
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
			
			//No need to cache in this node.
			} else {
				//skip this node, keep oust to the upper node
				oustedResourceToSource(inNode, resource);
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


	public static void main(String[] args) {
		List<routerNode> vlist = new ArrayList<routerNode>();
		for (int i = 0; i < 4; i++) {
		routerNode node = new routerNode(i);
		vlist.add(node);
		}
		routerNode node1 = vlist.get(0);
		routerNode node2 = vlist.get(3);
		routerResource res = new routerResource(3, 3);
		simulationEvent sm = new simulationEvent(1, 1, node1, res, node2);

	}

}

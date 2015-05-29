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
	private routerNode firstTupleNode = getFirstTupleNode();
	private List<routerNode> firstTupleToEnd = new ArrayList<routerNode>();
	private routerNode server;
	private int routingTimes = 0;
			
	public ClsPlus(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		super(se, vlist, rMap);
//		super.addRequestNum();

			int idx = vlist.size();
			//return the serverNode
			this.server = vlist.get(idx - 1);
	}

	public void routing() {
		routerNode firstNode = vlist.get(0);
		//endNode may be tupleNode or Server
		routerNode endNode = routeToSource(firstNode);
//		routerNode endNode = routeToSource2();
		responseBack(endNode);
		super.printPath();
	}
	
	public void responseBack(routerNode node) {
		//the node contains the cache itself 
		if (node == null) {
			super.addHitNum();
			return;
		}

		routerCache cache = super.getCache(node);
		//either case, server or tuple, pulling down must happen
		//there is no tuple on the way
		if (cache.isServer()) {
			serverPullDown(node);
		} else {
			//there is tuple 
			super.addHitNum();

			//for routeToSource2
			//get the realPath
//			int idx = vlist.indexOf(firstTupleNode);
//			for (int i = 0; i < idx; i++) {
//				routerNode eachNode = vlist.get(i);
//				this.realList.add(eachNode);
//			}
//			for (routerNode e : this.firstTupleToEnd) {
//				this.realList.add(e);
//			}
			//for routeToSource2

			//if the request node itself is the firstTupleNode
			tupleNodePullDown();
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
		
		if (lowerTuple.isValid()) 
			return;

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
					//update the trail

					updateTupleInfo(this.firstTupleNode);

					//oust all the replaced resources
					for (routerResource e : replacedRealResourceList ) {
						oustedResourceToSource(firstTupleNode, e);
					}

				}

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
		
		if (tuple == null)
			return;
		if (tuple.getInNode() == null)
			return;
		//1.find inNode from tuple 
		routerNode inNode = tuple.getInNode();
		if (inNode == null) {
			return;
		}
		routerCache inCache = super.getCache(inNode);
		routerTuple inTuple = inNode.getTuple(rResource);
		
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
		if (!inCache.hasEnoughRemainingCacheSize(rResource)) {

			List<routerResource> replacedResourceList =
					inCache.saveThisResource(rResource);
			
			if (replacedResourceList != null) {
				//save this resource

				//remove all to be replaced resources in this node.
				for (routerResource e : replacedResourceList) {
					routerResource resourceEach = cache.getResourceById(e.getID());
					inCache.removeResource(resourceEach);
				}

				//cache the resource
				inCache.scheduleLRU(rResource, inNode);
				
				//ousted the replaced resources
				for (routerResource e : replacedResourceList) {
					routerResource resourceEach = cache.getResourceById(e.getID());
					oustedResourceToSource(inNode, resourceEach);
				}
				
				return;

			} else {
				//not save the resource in this node
				oustedResourceToSource(inNode, rResource);
			}
			
		} else {
			inCache.scheduleLRU(rResource, inNode);
			return;
		}

	}

	public routerNode routeToSource2() {
		//no trail
		if (this.firstTupleNode == null) {
			int idx = vlist.size();
			//return the serverNode
			return vlist.get(idx - 1);
		} else {
			// has trail
			routerTuple firstTuple = firstTupleNode.getTuple(rResource);
			if (firstTuple.getOutNodes().size() == 0) {
				for (routerNode e : this.vlist) {
					this.realList.add(e);
				}
				return firstTupleNode;
			} else {
				return findEnd(firstTupleNode);
			}
		}
	}
	
	public routerNode findEnd(routerNode node) {
		this.firstTupleToEnd.add(node);
		routerTuple tuple = node.getTuple(rResource);
		if (!tuple.isValid()) {
			return this.server;
		}
		if (tuple.getOutNodes().size() == 0) {
			return node;
		}
		routerNode outNode = tuple.getOutNodes().get(0);
		return findEnd(outNode);
	}

	public routerNode routeToSource(routerNode node) {
		this.routingTimes++;
		routerCache cache = super.getCache(node);
		super.realList.add(node);
		
		if (this.routingTimes > 10) {
			realList = vlist;
			return server;
		}

		if (cache.isServer()) {
			int idx = vlist.size();
			//return the serverNode
			return vlist.get(idx - 1);
		} else {
			cache.addResourceCount(rResource);
		}
		
		if (node.hasValidTuple(rResource)) {
			routerTuple tuple = node.getTuple(rResource);

			//if contains the cache itself, return null
			if (tuple.getOutNodes().size() == 0 && node.getid() == this.vlist.get(0).getid())
				return null;

			if (tuple.getOutNodes().size() == 0) {
				return node;
			}

			routerNode nextNode = getTupleOutNode(node);
			if (nextNode.getid() == node.getid()) {
				return node;
			} else {
				return routeToSource(nextNode);
			}

			//has no valid tuple
		} else {
			routerNode nextNode = getEnrouteNextNode(node);
			return routeToSource(nextNode);
		}

	}
	
	private routerNode getTupleOutNode(routerNode node) {
		routerTuple tuple = node.getTuple(rResource);
		List<routerNode> outs = tuple.getOutNodes();
		if (outs.size() == 0) {
			return node;
		} else {
			return outs.get(0);
		}
	}
	
	private routerNode getEnrouteNextNode(routerNode node) {
		int idx = vlist.indexOf(node);
		return vlist.get(idx + 1);
	}
	
	private routerNode getFirstTupleNode() {
		for (int i = 0; i < super.vlist.size() - 1; i++) {
			routerNode node = vlist.get(i);
			if (node.hasValidTuple(rResource)) {
				return node;
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

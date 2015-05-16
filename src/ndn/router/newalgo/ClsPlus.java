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
	private routerNode firstTupleNode = getFirstTupleNode();
			
	public ClsPlus(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		super(se, vlist, rMap);
		super.addRequestNum();
	}

	public void routing() {
		routerNode firstNode = vlist.get(0);
		//endNode may be tupleNode or Server
		routerNode endNode = routeToSource(firstNode);
		super.printPath();
		responseBack(endNode);
	}
	
	public void responseBack(routerNode node) {
		routerCache cache = super.getCache(node);
		//either case, server or tuple, pulling down must happen
		//there is no tuple on the way
		if (cache.isServer()) {
			serverPullDown(node);
		} else {
			//there is tuple 
			super.addHitNum();
			tupleNodePullDown();
		}
	}
	
	private void serverPullDown(routerNode server) {
			routerNode lowerNode = super.getLowerNode(server);
			routerCache lowerCache = super.getCache(lowerNode);
			routerTuple lowerTuple = lowerNode.getTuple(rResource);

			if (!lowerCache.hasEnoughRemainingCacheSize(rResource)) {
				List<routerResource> replacedResourceList =
						lowerCache.saveThisResource(rResource);

				if (replacedResourceList != null) {
					//cache this resource, this resource is hotter than 
					//all to be ousted resources
					for (routerResource e : replacedResourceList) {
						lowerCache.removeResource(e);
					}
					//cache the resource
					lowerCache.scheduleLRU(rResource, lowerNode);
					lowerTuple.setValid();
					lowerTuple.setInNode(server);
				}

			} else {
				//has space for this resource
					lowerCache.scheduleLRU(rResource, lowerNode);
					lowerTuple.setValid();
					lowerTuple.setInNode(server);
			}
	}
	
	public void tupleNodePullDown() {
		routerNode lowerNode = super.getLowerNode(firstTupleNode);
		routerCache lowerCache = super.getCache(lowerNode);
		routerTuple lowerTuple = lowerNode.getTuple(rResource);

		if (!lowerCache.hasEnoughRemainingCacheSize(rResource)) {
				List<routerResource> replacedResourceList =
						lowerCache.saveThisResource(rResource);
				if (replacedResourceList != null) {

					//cache this resource, this resource is hotter than 
					//all to be ousted resources
					for (routerResource e : replacedResourceList) {
						lowerCache.removeResource(e);
					}

					//cache the resource
					lowerCache.scheduleLRU(rResource, lowerNode);
					//update the trail
					updateTupleInfo(this.firstTupleNode);

					//oust all the replaced resources
					for (routerResource e : replacedResourceList) {
						lowerCache.removeResource(e);
//						oustedResourceToSource(e);
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

		routerNode lowerNode = super.getLowerNode(node);
		routerCache lowerCache = super.getCache(lowerNode);
		routerTuple lowerTuple = lowerNode.getTuple(rResource);
		
		tuple.addOutNodes(lowerNode);

		lowerTuple.setValid();
		lowerTuple.setInNode(node);
		
	}
	
	public void oustedResourceToSource(routerNode node, routerResource resource) {
		routerCache cache = super.getCache(node);
		routerTuple tuple = node.getTuple(resource);
		
		//1.find inNode from tuple 
		routerNode inNode = tuple.getInNode();
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

				//remove all to be replaced resources
				for (routerResource e : replacedResourceList) {
					inCache.removeResource(e);
				}

				//cache the resource
				inCache.scheduleLRU(rResource, inNode);
				
				//ousted the replaced resources
				for (routerResource e : replacedResourceList) {
					oustedResourceToSource(inNode, e);
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


	public routerNode routeToSource(routerNode node) {
		routerCache cache = super.getCache(node);
		super.realList.add(node);

		if (cache.isServer()) {
			int idx = vlist.size();
			return vlist.get(idx - 1);
		} else {
			cache.addResourceCount(rResource);
		}
		
		if (node.hasValidTuple(rResource)) {
			routerNode nextNode = getTupleOutNode(node);
			if (nextNode.getid() == node.getid()) {
				return node;
			} else {
				return routeToSource(nextNode);
			}
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
	
	private routerNode getTupleInNode(routerNode node) {
		routerTuple tuple = node.getTuple(rResource);
		routerNode inNode = tuple.getInNode();
		return inNode;
	}
	
	private routerNode getEnrouteNextNode(routerNode node) {
		int idx = vlist.indexOf(node);
		return vlist.get(idx + 1);
	}
	
	private routerNode getFirstTupleNode() {
		for (int i = 0; i < super.vlist.size(); i++) {
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

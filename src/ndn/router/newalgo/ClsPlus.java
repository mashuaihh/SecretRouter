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
		responseBack(endNode);
		super.printPath();
	}
	
	public void responseBack(routerNode node) {
		routerCache cache = super.getCache(node);

		if (cache.isServer()) {
			routerNode lowerNode = super.getLowerNode(node);
			routerCache lowerCache = super.getCache(lowerNode);
			routerTuple lowerTuple = lowerNode.getTuple(rResource);
			lowerTuple.setValid();
			lowerTuple.setInNode(node);
			lowerCache.scheduleLRU(rResource, lowerNode);
		} else {
			pullDown();
		}
	}
	
	public routerNode routeToSource(routerNode node) {
		routerCache cache = super.getCache(node);
		super.realList.add(node);

		if (cache.isServer()) {
			return null;
		} else {
			cache.addResourceCount(rResource);
		}
		
		if (node.hasValidTuple(rResource)) {
			routerNode nextNode = getTupleOutNode(node);
			if (nextNode.getid() == node.getid()) {
				return null;
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
		for (int i = 0; i < super.realList.size(); i++) {
			routerNode node = realList.get(i);
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

package ndn.router.newalgo;

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
		routerNode upNode = getEndNode();
		routerNode downNode = super.getLowerNode(upNode);
		addTuple(upNode, downNode);

	}
	
	private void addTuple(routerNode upNode, routerNode downNode) {
		routerCache upCache = super.getCache(upNode);
		routerTuple upTuple = upNode.getTuple(rResource);
		routerTuple downTuple = downNode.getTuple(rResource);
		downTuple.setValid();
		if (!upCache.isServer()) {
			upTuple.addOutNodes(downNode);
			downTuple.setInNode(upNode);
		}
	}
	
	private boolean hasTrail() {
		for (int i = 0; i < vlist.size(); i++) {
			routerNode eachNode = vlist.get(i);
			routerTuple eachTuple = eachNode.getTuple(rResource);
			routerCache eachCache = super.getCache(eachNode);
			if (eachTuple.isValid() && !eachCache.isServer())
				return true;
		}
		return false;
	}
	
	private routerNode getEndNode() {
		for (int i = 0; i < vlist.size(); i++) {
			routerNode eachNode = vlist.get(i);
			routerTuple eachTuple = eachNode.getTuple(rResource);
			routerCache eachCache = super.getCache(eachNode);
			if (eachTuple.isValid() || eachCache.isServer())
				return eachNode;
		}
		return null;
	}
	

}

package ndn.router.newalgo;

import java.util.List;
import java.util.Map;

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
		List<routerNode> vlist = super.getVList();
		routerResource resource = super.rResource;
		
		for (int i = 0; i < vlist.size(); i++) {
			routerNode eachNode = vlist.get(i);
			routerTuple eachTuple = eachNode.getTuple(resource);
		}
	}

}

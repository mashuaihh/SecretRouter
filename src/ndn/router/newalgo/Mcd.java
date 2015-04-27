package ndn.router.newalgo;

import java.util.List;
import java.util.Map;

import ndn.router.cache.routerCache;
import ndn.router.cache.routerNode;
import ndn.router.cache.routerResource;
import ndn.router.cache.simulationEvent;

public class Mcd extends NewAlgo {
	public Mcd(simulationEvent se, List<routerNode> vlist, 
			Map<routerNode, routerCache> rMap) {
		super(se, vlist, rMap);
		super.addRequestNum();
	}
	
	public void routing() {
		List<routerNode> vlist = super.getVList();
		routerResource resource = super.getResource();
		for (int i = 0; i < vlist.size(); i++) {
			routerNode each = vlist.get(i);
			
			super.realList.add(each);
			
			routerCache cache = super.getCache(each);
			if (cache.hasResource(resource)) {
				if ((i != (vlist.size() - 1)) && (i != 0))
					super.addHitNum();
				//move cache down one level
				routerNode lowerNode = super.getLowerNode(each);
				routerCache lowerCache = super.getCache(lowerNode);
				//remove the cache in this node
				if (!cache.isServer())
					cache.removeResource(resource);
				lowerCache.scheduleLRU(resource, lowerNode);
				break;
			}
		}
	}
}

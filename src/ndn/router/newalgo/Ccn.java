package ndn.router.newalgo;

import java.util.List;
import java.util.Map;

import ndn.router.cache.routerCache;
import ndn.router.cache.routerNode;
import ndn.router.cache.routerResource;
import ndn.router.cache.simulationEvent;

public class Ccn extends NewAlgo {

	public Ccn(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		super(se, vlist, rMap);
//		super.addRequestNum();
	}
	
	public void routing() {
		List<routerNode> vlist = super.getVList();
		routerResource resource = super.getResource();

		for (int i = 0; i < vlist.size(); i++) {
			routerNode each = vlist.get(i);
			
			super.realList.add(each);
			
			routerCache cache = super.getCache(each);
			if (cache.hasResource(resource)) {
				if ((i != (vlist.size() - 1)))
					super.addHitNum();
				break;
			}
		}
		
		for (int i = realList.size() - 2; i >= 0; i--) {
			routerNode node = realList.get(i);
			routerCache cache = super.getCache(node);
			
			cache.scheduleLRU(resource, node);
		}
	}
}

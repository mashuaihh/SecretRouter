package ndn.router.newalgo;

import java.util.List;
import java.util.Map;

import ndn.router.cache.*;

public class Lcd extends NewAlgo {

	public Lcd(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
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
				if (i != (vlist.size() - 1))
					super.addHitNum();
				routerNode lowerNode = super.getLowerNode(each);
				routerCache lowerCache = super.getCache(lowerNode);
				lowerCache.scheduleLRU(resource);
				break;
			}
		}
	}

}

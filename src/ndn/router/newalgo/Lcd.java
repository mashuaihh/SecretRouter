package ndn.router.newalgo;

import java.util.List;
import java.util.Map;

import ndn.router.cache.*;

public class Lcd extends NewAlgo {

	public Lcd(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		super(se, vlist, rMap);
	}
	
	public void routing() {
		List<routerNode> vlist = super.getVList();
		for (int i = 0; i < vlist.size(); i++) {
			routerNode each = vlist.get(i);
			super.realList.add(each);
			routerCache cache = super.getCache(each);
			if (cache.routing(super.getEvent())) {
				break;
			}
		}
	}

}

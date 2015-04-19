package ndn.router.newalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

import ndn.router.cache.*;

public class NewAlgo {
	private routerResource rResource;
	private List<routerNode> vlist;
	private simulationEvent se;
	private Map<routerNode, routerCache> rMap;
	public List<routerNode> realList = new ArrayList<routerNode>();
	
	public NewAlgo(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		this.rMap = rMap;
		this.se = se;
		this.rResource = se.getrouterResource();
		this.vlist = vlist;
	}
	
	public void showPath() {
		out.print("Path to Server: ");
		for (int i = 0; i < vlist.size(); i++) {
			routerNode each = vlist.get(i);
			if (i == vlist.size() - 1)
				out.println(each.getid());
			else 
				out.print(each.getid() + " -> ");
		}
	}
	
	public routerCache getCache(routerNode node) {
		routerCache cache = this.rMap.get(node);
		return cache;
	}
	
	public List<routerNode> getVList() {
		return this.vlist;
	}
	
	public routerResource getResource() {
		return this.rResource;
	}
	
	public simulationEvent getEvent() {
		return this.se;
	}

}

package ndn.router.newalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

import ndn.router.cache.*;

public class NewAlgo {
	public routerResource rResource;
	private List<routerNode> vlist;
	private simulationEvent se;
	private Map<routerNode, routerCache> rMap;
	public List<routerNode> realList = new ArrayList<routerNode>();
	
	private static int requestNum = 0;
	private static int hitNum = 0;
	
	public NewAlgo(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		this.rMap = rMap;
		this.se = se;
		this.rResource = se.getrouterResource();
		this.vlist = vlist;
	}
	
	public void showPath() {
		out.println();
		out.println("Requesting resource " + this.rResource.getID());
		out.print("Path to Server: ");
		for (int i = 0; i < vlist.size(); i++) {
			routerNode each = vlist.get(i);
			if (i == vlist.size() - 1)
				out.println(each.getid());
			else 
				out.print(each.getid() + " -> ");
		}
		out.print("Real path:      ");
		for (int i = 0; i < realList.size(); i++) {
			routerNode each = realList.get(i);
			if (i == realList.size() - 1)
				out.println(each.getid());
			else 
				out.print(each.getid() + " -> ");
		}

	}
	
	public void stat() {
		out.println("Hit rate: " + this.getHitRate());
		out.println("Path stretch: " + this.getPathStretch());
	}
	
	public double getHitRate() {
		double rate = this.hitNum * 1.0 / this.requestNum;
		return rate;
	}
	
	public double getPathStretch() {
		double ps = this.realList.size() * 1.0 / this.vlist.size();
		return ps;
	}
	
	public routerNode getLowerNode(routerNode node) {
		if (node.getid() == vlist.get(0).getid()) {
			out.println("lower node: " + node.getid());
			return node;
		}
		else {
			int idx = vlist.indexOf(node);
			out.println("lower node: " + vlist.get(idx - 1));
			return vlist.get(idx - 1);
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
	
	public int getHitNum() {
		return this.hitNum;
	}
	
	public void addHitNum() {
		this.hitNum++;
	}
	
	public void addRequestNum() {
		this.requestNum++;
	}
	
	public int getRequestNum() {
		return this.requestNum;
	}

}

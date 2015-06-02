package ndn.router.newalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

import ndn.router.cache.*;

public class NewAlgo {
	public routerResource rResource;
	public List<routerNode> vlist;
	private simulationEvent se;
	private Map<routerNode, routerCache> rMap;
	public List<routerNode> realList = new ArrayList<routerNode>();
	
	private static int requestNum = 0;
	private static int hitNum = 0;
	
	private static int realPathNum = 0;
	private static int oriPathNum = 0;
	
	private static int extraHop = 0;
	private static int extraLoad = 0;
	
	public NewAlgo(simulationEvent se, List<routerNode> vlist, Map<routerNode, routerCache> rMap) {
		this.rMap = rMap;
		this.se = se;
		this.rResource = se.getrouterResource();
		this.vlist = vlist;
		this.addRequestNum();
	}
	
	public void addExtraHop() {
		NewAlgo.extraHop++;
	}
	
	public void addExtraLoad(int i) {
		NewAlgo.extraLoad += i;
	}
	
	public int getExtraHop() {
		return NewAlgo.extraHop;
	}

	public int getExtraLoad() {
		return NewAlgo.extraLoad;
	}
	
	public void showPath() { out.println();
		
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
		out.println();
		out.println("Hit rate: " + this.getHitRate());
		out.println("Path stretch: " + this.getPathStretch());
	}
	
	public double getHitRate() {
		double rate = NewAlgo.hitNum * 1.0 / NewAlgo.requestNum;
		return rate;
	}
	
	public double getPathStretch() {
		double ps = this.realList.size() * 1.0 / this.vlist.size();
		return ps;
	}
	
	public double getPathStr() {
		double ps = NewAlgo.realPathNum * 1.0 / NewAlgo.oriPathNum;
		return ps;
	}
	
	public routerNode getLowerNode(routerNode node) {
		if (node.getid() == vlist.get(0).getid()) {
			return node;
		}
		else {
			int idx = vlist.indexOf(node);
			return vlist.get(idx - 1);
		}
	}
	
	public routerNode getUpperNode(routerNode node) {
		if (node.getid() == 0) {
			return node;
		} else {
			int idx = vlist.indexOf(node);
			return vlist.get(idx + 1);
		}
	}
	
	public routerNode getServer() {
		routerNode node = null;
		for (routerNode e : vlist) {
			if (e.getid() == 0)
				node = e;
		}
		return node;
	}
	
	public routerCache getCache(routerNode node) {
		routerCache cache = this.rMap.get(node);
		return cache;
	}
	
	public static void clearStat() {
		NewAlgo.hitNum = 0;
		NewAlgo.requestNum = 0;
		NewAlgo.oriPathNum = 0;
		NewAlgo.realPathNum = 0;
		NewAlgo.extraHop = 0;
		NewAlgo.extraLoad = 0;
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
		return NewAlgo.hitNum;
	}
	
	public void addHitNum() {
		NewAlgo.hitNum++;
	}
	
	public void addRequestNum() {
		NewAlgo.requestNum++;
	}
	
	public int getRequestNum() {
		return NewAlgo.requestNum;
	}
	
	public void addPathNum() {
		NewAlgo.oriPathNum += this.vlist.size();
		NewAlgo.realPathNum += this.realList.size();
	}
	
	public static void main(String[] args) {
		String a = "Extra Hop: d";
		int idx = a.indexOf(":");
		System.out.println(idx);
	}
}

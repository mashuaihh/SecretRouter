package ndn.router.cache;

import java.util.ArrayList;
import java.util.List;

public class routerTuple {
	private routerResource resource;
	private routerNode InNode;
	private List<routerNode> OutNodes = new ArrayList<routerNode>();
	private boolean valid = false;
	
	public routerTuple(routerResource se) {
		this.resource = se;
	}
	
	public void setValid() {
		this.valid = true;
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	public void setInNode(routerNode node) {
		this.InNode = node;
	}

	public void addOutNodes(routerNode node) {
		this.OutNodes.add(node);
	}
	
	public void removeOutNode(routerNode node) {
		this.OutNodes.remove(node);
	}
}

package ndn.router.cache;

import java.util.ArrayList;

public class Node {

	private int id;
	private int layer;
	private static int vertexNum = 0;
	private static ArrayList<Node> Nlist = new ArrayList<Node>();
	private boolean init = false;
	
	public Node() {
		this.id = Node.vertexNum;
		Node.Nlist.add(Node.vertexNum, this);
		Node.vertexNum++;
	}
	
	public void setId(int i) {
		this.id = i;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setLayer(int i ){
		this.layer = i;
	}
	
	public int getLayer() {
		return this.layer;
	}
	
	public int getVertexNum() {
		return Node.vertexNum;
	}
	
	public boolean isInit() {
		return this.init;
	}
	
	public void setInit() {
		this.init = true;
	}
	
	public ArrayList<Node> getList() {
		return Node.Nlist;
	}
}
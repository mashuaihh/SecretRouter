/**
 * To generate the textual representation of topology of a graph.
 * 
 */
package ndn.router.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author mashuai
 *
 */
public class TopologyMake {
	public static ArrayList<String> eList = new ArrayList<String>();
	public static int current_layer = 0;
	public static int max_layer = 3;
	
	public static int getBranch(Node node) {
		int layer = node.getLayer();
		Random ran = new Random();
		int branch = 0;
		if (layer == 1)
			branch = 2;
		else 
			branch = ran.nextInt(1) + layer ;
		return branch;
	}
	
	public static void growBranch(Node node) {
		int currLayer = node.getLayer();
		int branch = 5;
		if (node.getId() != 0)
			branch = getBranch(node);
		
		node.setInit();
		
		for (int i = 0; i < branch; i++) {
			Node eachNode = new Node();
			eachNode.setLayer(currLayer + 1);
			String eachStr = node.getId() + "," + eachNode.getId();
			eList.add(eachStr);
		}
	}
	
	public static void main(String[] args) {
		Node server = new Node();
		
		
		while (current_layer <= max_layer) {
			ArrayList<Node> nlist = server.getList();
			
			for (int i = 0; i < nlist.size(); i++) {
				Node v = nlist.get(i);
				if (v.getLayer() == current_layer && v.isInit() == false) {
					growBranch(v);
				}
			}
			current_layer++;
		}
		
		for (int i = 0; i < server.getVertexNum(); i++) {
			System.out.println(i + ",*");
		}
		
		try {
			File file = new File("C:\\GraphTopology.txt");
			if (file.exists())
				file.delete();
			PrintWriter out = new PrintWriter(file);
			out.println(server.getVertexNum()+ " " + "vertices");
			for (int i = 0; i < server.getVertexNum(); i++) {
				out.println(i + ",*");
			}
			int listLen = eList.size();
			out.println(listLen + " edges");
			for (String s : eList) {
				out.println(s);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

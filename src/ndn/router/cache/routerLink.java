/**
 *  router edge
 */
package ndn.router.cache;

public class routerLink {

	public routerLink(int V1, int V2, double capacity, int weight) {
		this.V1 = V1;
		this.V2 = V2;
		this.capacity = capacity;
		this.weight = weight;
	}

	
	/**
	 * for debugging
	 */
	public String toString(){
		return "E" + V1 + "-" + V2;
	}

	/**
	 * for debugging
	 */
	public int getWeight(){
		return weight;
	}
	

	
	private int V1;                 // edge adjacency vertex
	private int V2;                 // edge adjacency vertex2
	private double capacity;        // capacity of edge
	private int weight;          // weight of edge
	
}

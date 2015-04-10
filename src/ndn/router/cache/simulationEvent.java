/**
 * events for simulation
 */
package ndn.router.cache;

/**
 * @author Administrator
 *
 */
public class simulationEvent {

	/**
	 * 
	 */
	public simulationEvent(long etime, int eventType, routerNode rNode, routerResource rResource, routerNode rN) {
		this.etime = etime;
		this.eventType = eventType;
		this.rNode = rNode;
		this.rResource = rResource;
		this.rN = rN;
	}
	
	
	/**
	 * return event time
	 */
	public long getEventTime() {
		return etime;
	}
	
	/**
	 * return event type
	 */
	public int getEventType() {
		return eventType;
	}

	/**
	 * return event node
	 */
	public routerNode getEventNode() {
		return rNode;
	}
	
	/**
	 * return resource node
	 */
	public routerNode getResourceNode() {
		return rN;
	}
	/**
	 * return router resource
	 */
	public routerResource getrouterResource() {
		return rResource;
	}

	private long etime;    // total request times
	private int eventType; // event type: 0- 1- 2-
	private routerNode rNode; // corresponding router node
	private routerResource rResource; 
	private routerNode rN;    // nodes containing the resource
	
	

}

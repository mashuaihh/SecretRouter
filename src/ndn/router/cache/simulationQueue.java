/**
 * event queue
 */
package ndn.router.cache;

import java.lang.*;
/**
 * @author Administrator
 *
 */
public class simulationQueue {

	/**
	 * 
	 */
	public simulationQueue() {
		events = new simulationEvent[DEFAULT_QUEUE_SIZE];
		eventSize = 0;
	}
	

	/**
	 * add events to the queue
	 */
	public void addEvent(simulationEvent se){
		// check whether the queue is full
		if(eventSize >= ACTUAL_QUEUE_SIZE){
			ACTUAL_QUEUE_SIZE = ACTUAL_QUEUE_SIZE + QUEUE_INCREMENT;
			// expand the queue
			simulationEvent[] newEvents = new simulationEvent[ACTUAL_QUEUE_SIZE];
			System.arraycopy(events, 0, newEvents, 0, eventSize);
			events = newEvents;
		}
		events[eventSize] = se;
		eventSize++;
	}
	
	/**
	 * delete events from the queue
	 */
	public void delEvent(int index){
		events[index] = events[eventSize-1];
		eventSize--;
	}
	
	/**
	 * get latest events from the queue
	 */
	public simulationEvent getEvent(){
		if(eventSize <= 0) return null;
		int minindex = 0;
		long min = events[0].getEventTime();
		// search for the latest event
		for(int i = 1; i< eventSize; i++){
			if(events[i].getEventTime()<min){
				min = events[i].getEventTime();
				minindex = i;
			}
		}
		simulationEvent e = events[minindex];
		delEvent(minindex);
		return e;
	}
	
	
	
	
	
	private static final int DEFAULT_QUEUE_SIZE = 1000; // default queue size
	private static final int QUEUE_INCREMENT = 500;     // default queue size
	private int ACTUAL_QUEUE_SIZE = 1000;                      // allocated queue size
	private int eventSize = 0;                          // event length
	private simulationEvent[] events;                   // event queue

}

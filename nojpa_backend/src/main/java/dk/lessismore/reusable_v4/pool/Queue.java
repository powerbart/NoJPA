package dk.lessismore.reusable_v4.pool;



public class Queue {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Queue.class);

    private QueueElement firstElement = null;
    private int countOfElements = 0;

    class QueueElement {
	QueueElement after = null;
	Object myObject = null;
    }

    public void push(Object o){
	synchronized(this){
		QueueElement q = new QueueElement();
		q.myObject = o;
		q.after = firstElement;
		firstElement = q;
		countOfElements++;
		//log.debug("Adding element too queue " + countOfElements);
	}
    }

    public Object pop(){
	synchronized(this){
	    Object objectToReturn = firstElement.myObject;
	    firstElement = firstElement.after;
	    countOfElements--;
	    //log.debug("POP: " + countOfElements);
	    return objectToReturn;
	}
    }
    
    public int size(){ 
	    return countOfElements;
    }
    
    public boolean isEmpty(){
	    return countOfElements == 0;
    }
}

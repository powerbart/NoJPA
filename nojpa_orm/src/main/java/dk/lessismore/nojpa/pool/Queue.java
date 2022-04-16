package dk.lessismore.nojpa.pool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Queue<T> {

	private static final Logger log = LoggerFactory.getLogger(Queue.class);

    private QueueElement firstElement = null;
    private int countOfElements = 0;

    class QueueElement {
	QueueElement after = null;
	T myObject = null;
    }

    public void push(T o){
	synchronized(this){
		QueueElement q = new QueueElement();
		q.myObject = o;
		q.after = firstElement;
		firstElement = q;
		countOfElements++;
		//log.debug("Adding element too queue " + countOfElements);
	}
    }

    public T pop(){

	synchronized(this){
		if (firstElement == null) return null;
	    T objectToReturn = firstElement.myObject;
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

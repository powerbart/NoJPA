package dk.lessismore.nojpa.pool;

import dk.lessismore.nojpa.pool.factories.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class implements a resource pool. Basicly its an object container,
 * which contains a defined number of objects. Its primary targets is to contain
 * objects which take a long time to instantiate and create. This could
 * be forinstance database-, socket connections and threads etc.<br>
 * The objects in the pool/container can be taken out of the container, and
 * used, but must allways be placed back into the pool when the work with the
 * object has finished. This class can holde any kind of object. It uses a
 * so called object factory (like abstract factory design pattern) which can
 * create and instantiate a object of a desired kind. This could be database
 * factories forinstance. As the abstract factory design pattern describes
 * there is a common interface called 'ResourceFactory' which all factories must
 * implement, if you want this class to use the factory.   ..
 *
 * @author LESS-IS-MORE
 * @version 1.0 14-01-02
 */
public class ResourcePool {

    private static final Logger log = LoggerFactory.getLogger(ResourcePool.class);
    /**
     * The default size of the resource pool (20)
     */
    public static final int DEFAULT_POOL_SIZE = 20;

    /**
     * The pool size.
     */
    private int _poolSize = DEFAULT_POOL_SIZE;

    /**
     * A list containing the object which currently is available in the
     * pool
     */
    private final Queue _pool = new Queue();


    /**
     * The Factory which can produce the object which this pool consist of.
     */
    private ResourceFactory _resourceFactory = null;

    /**
     * Constructor which instantiate the pool size with the default amount
     * and with the given resource factory.
     * @param resourceFactory The resource factory that can produce the objects
     * which this pool should consist of.
     */
    public ResourcePool(ResourceFactory resourceFactory) {
        this(resourceFactory, DEFAULT_POOL_SIZE);
    }

    /**
     * Constructor
     * @param poolSize The initial nr of objects in the pool.
     * @param resourceFactory The resource factory that can produce the objects
     * which this pool should consist of.
     */
    public ResourcePool(ResourceFactory resourceFactory, int poolSize) {
        _resourceFactory = resourceFactory;
        createPool(poolSize);
    }

    public void createPool(int poolSize) {
        log.debug("Creating pool x-start : " + _resourceFactory.debugName());
        _poolSize = poolSize;


        //Fill the pool!
        for(int i = 0; i < _poolSize; i++) {
            log.debug("createPool() making("+ i +" out of "+ poolSize +") ["+  _resourceFactory.debugName() +"]");
            Object resource = _resourceFactory.makeResource();
            _pool.push(resource);
        }
	log.debug("Creating pool -done : " + _resourceFactory.debugName());
    }
    /*
    public void recreatePool() {
        recreatePool(_poolSize);
    }
    public void recreatePool(int poolSize) {
	log.debug("recreatePool:: calling closePool()");
	for(int i = _pool.size(); i < _poolSize; i++){
	   Object resource = _resourceFactory.makeResource();
	   _pool.push(resource); 
	}
	log.debug("recreatePool:: done");
    }
    */
    public void addNew(){
        log.info("START: addNew() ----- of " + _resourceFactory.debugName());
        Object resource = _resourceFactory.makeResource();
        _pool.push(resource);
        log.info("END: addNew() ----- of " + _resourceFactory.debugName());
    }

    /**
     * This function will close the pool, which will result in that
     * all the objects which belongs to the pool are closed. The method
     * will block until all pool resources has been returned. Afterwards
     * all will be closed.
     */
    public void closePool() {
        log.debug("closePool::1");
        synchronized(log){
            try{
                while(!_pool.isEmpty()){
                    Object o = _pool.pop();
                    _resourceFactory.closeResource(o);
                }

            }catch(Exception e) {
                log.error("Some error in getFromPool(): " + e, e);
            }
        }
    }


    /**
     * @return The pool size..
     */
    public int getPoolSize() {
        return _poolSize;
    }

    /**
     * @return The list containing the objects currently in the pool.
     */
    public Queue getPool() {
        return _pool;
    }

    /**
     * @return The nr of object currently in the pool. This is not
     * the same as the pool size.
     */
    public int getNrOfResources() {
        return _pool.size();
    }

    /**
     * Gets an object from the pool. If there currently is no objects left in the
     * pool, the calling thread will wait until an object has been put back into the
     * pool, or the pool has been closed.
     *
     * @return A pool object.
     * @exception RuntimeException If the pool has been closed or if the thread has been
     * interrupted.
     */
    static long waiting_global = 0;
    public Object getFromPool() {
        synchronized (log) {
//            log.debug("getFromPool " + _resourceFactory.debugName());
            try {
                //log.debug("getFromPool:2");
                int countOfWait = 0;
                while (_pool.isEmpty()) {
                    log.warn("Waiting for "+ _resourceFactory.debugName() +"..... size(" + _pool.size() + ") #"+ (waiting_global++));
                    try {
                        //log.error("getFromPool:2.1 - wait - start");
                        try {
                            log.wait(200);
                        } catch (Exception e) {
                            log.error("waiting-X ... pool("+ _pool.size() +") : " + e);
                        }


                        //log.error("getFromPool:2.1 - wait - ends");
                        if (countOfWait++ > 5 * _resourceFactory.maxWaitSecBeforeCreatingNewResource()) { //Waiting to 5 mins ...
                            //log.error("getFromPool:2.1 - addNew - starts");
                            addNew();
                            countOfWait = 0;
                            //log.error("getFromPool:2.1 - addNew - ends");
                        }
                        //log.debug("getFromPool:2.2");
                    } catch (Exception ee) {
                        //log.debug("getFromPool():: Got a InterruptedException");
                    }
                }
                //log.debug("getFromPool:3");
                waiting_global = 0;
                return _pool.pop();
            } catch (Exception e) {
                log.error("Some error in getFromPool(): " + e.toString());
                return null;
            }
        }
    }

    /**
     * Puts a pool object back into the pool.
     * @param poolObj Pool object.
     */
    public void putBackInPool(Object poolObj) {
	//notifyAll();
//	log.debug("putBackInPool " + _resourceFactory.debugName());
        _pool.push(poolObj);
        try{
            synchronized (log) {
                log.notify();
            }
        } catch (Exception e){
            log.error("putBackInPool: " + e);
        }
	//log.debug("putBackInPool:: -end ");
    }


    /*
    public synchronized  void replaceWithNewPoolObject() {
        _pool.add(_resourceFactory.makeResource());
        notifyAll();
    }
    */
}

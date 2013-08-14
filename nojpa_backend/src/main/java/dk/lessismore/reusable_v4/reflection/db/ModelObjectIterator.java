package dk.lessismore.reusable_v4.reflection.db;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObject;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import org.apache.log4j.Logger;
import dk.lessismore.reusable_v4.utils.MaxSizeMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: bart
 * Date: 2007-04-06
 * Time: 01:59:45
 * To change this template use File | Settings | File Templates.
 */
public class ModelObjectIterator<M extends ModelObject> implements Iterable, DbObjectVisitor {

    private static MaxSizeMap mapOfIterator = new MaxSizeMap(1000);

    public static Object getModelObjectIterator(String key){
        return mapOfIterator.get(key);
    }

    public static void putModelObjectIterator(String key, ModelObjectIterator m){
        mapOfIterator.put(key, m);
    }



    private static Logger log = Logger.getLogger(ModelObjectIterator.class);


    private Calendar lastModified = Calendar.getInstance();
    private boolean done = false;
    private ArrayList<String> totalList = new ArrayList<String>(50);
    private Class modelClass = null;
    private int posibleTotalSize = -1;

    public ModelObjectIterator(Class modelClass){
        this.modelClass = modelClass;
    }

    public void visit(ModelObjectInterface m) {
        add((M) m);
    }

    public synchronized void add(M m){
        lastModified = Calendar.getInstance();
        totalList.add(m.getPrimaryKeyValue());
        log.debug("add(): current size = " + totalList.size() + " in "+ this);
    }

    public synchronized void add(String objectID){
        lastModified = Calendar.getInstance();
        totalList.add(objectID);
        log.debug("add(): current size = " + totalList.size() + " in "+ this);
    }

    public synchronized boolean getDone() {
        Calendar min2 = Calendar.getInstance();
        min2.add(Calendar.MINUTE, -1);
        return done || lastModified.before(min2) || posibleTotalSize == 0;
    }

    public synchronized int currentSize(){
        return totalList.size();
    }


    public synchronized void setDone(boolean done) {
        this.done = done;
    }


    public int getPosibleTotalSize() {
        return posibleTotalSize;
    }

    public void setPosibleTotalSize(int posibleTotalSize) {
        this.posibleTotalSize = posibleTotalSize;
    }

    public synchronized Iterator<M> iterator() {
        log.debug("iterator(): current size = " + totalList.size() + " in "+ this);
        return new MIterator(totalList.size());
    }


    protected class MIterator implements Iterator<M> {

        int size = 0;
        int current = 0;

        public MIterator(int size){
            this.size = size;
        }

        public boolean hasNext() {
            return current < size;
        }

        public M next() {
            return (M) DbObjectReader.readObjectFromDb(totalList.get(current++), modelClass);
        }

        public void remove() {

        }
    }

}

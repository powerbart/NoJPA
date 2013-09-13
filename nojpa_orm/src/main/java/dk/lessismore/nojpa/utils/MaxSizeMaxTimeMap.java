package dk.lessismore.nojpa.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Created : by IntelliJ IDEA.
 * User: bart
 * Date: 12-11-2008
 * Time: 11:01:42
 * To change this template use File | Settings | File Templates.
 */
public class MaxSizeMaxTimeMap<E> {

    private final Map<String, TimeMapEntry<E>> cachedObjects[] = new Map[] {new HashMap<String, TimeMapEntry<E>>(), new HashMap<String, TimeMapEntry<E>>()};
    private int nrOfOldBucket = 0;
    private int maxCacheSize;
    private long maxSeconds;

    protected static class TimeMapEntry<E> {
        public long lastAccessed = System.currentTimeMillis();
        public E value;


        public TimeMapEntry(E value){
            this.value = value;
        }

        public TimeMapEntry(TimeMapEntry<E> t){
            this.value = t.value;
        }

    }

    public MaxSizeMaxTimeMap(int maxCacheSize, long maxSeconds) {
        this.maxCacheSize = maxCacheSize;
        this.maxSeconds = maxSeconds;
    }


    public synchronized int getNrOfObjectsInCache() {
        return getNewBucket().size()+getOldBucket().size();
    }

    public Map<String, TimeMapEntry<E>>[] getCachedObjects() {
      return cachedObjects;
    }

    public Map<String, TimeMapEntry<E>> getNewBucket() {
      return getCachedObjects()[((nrOfOldBucket+1)%2)];
    }

    public Map<String, TimeMapEntry<E>> getOldBucket() {
      return getCachedObjects()[(nrOfOldBucket%2)];
    }

    public boolean isFull(Map bucket) {
      return bucket.size() == maxCacheSize;
    }


    public synchronized void put(String key, E object) {
        try {
          if (isFull(getNewBucket())) {
            shiftBuckets();
          }

          Map<String, TimeMapEntry<E>> newBucket = getNewBucket();
          Map<String, TimeMapEntry<E>> oldBucket = getOldBucket();
          if (newBucket.containsKey(key)) {
              newBucket.put(key, new TimeMapEntry<E>(object));
              return;
          }
          if (oldBucket.containsKey(key)) {
            TimeMapEntry<E> o = oldBucket.get(key);
            oldBucket.remove(o);
            newBucket.put(key, new TimeMapEntry<E>(o));
          } else {
              newBucket.put(key, new TimeMapEntry<E>(object));
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
    }


    /** This method gets an object with the given primary key from the cache. */
    public synchronized E get(String primaryKey) {
        try {
          Map<String, TimeMapEntry<E>> newBucket = getNewBucket();
          TimeMapEntry<E> entry = newBucket.get(primaryKey);
          if (entry == null) {
            Map<String, TimeMapEntry<E>> oldBucket = getOldBucket();
            entry = oldBucket.get(primaryKey);
            if (entry != null && validTime( entry )) {
              oldBucket.remove(primaryKey);
              put(primaryKey, entry.value);
              return entry.value;
            } else {
              return null;
            }
          } else {
            return validTime(entry) ? entry.value : null;
          }
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
    }


    private  boolean validTime(TimeMapEntry<E> entry){
        long now = System.currentTimeMillis();
        return now < maxSeconds * 1000 + entry.lastAccessed;
    }



    private synchronized void shiftBuckets() {
        try {
          Map<String, TimeMapEntry<E>> oldBucket = getOldBucket();
          oldBucket.clear();
          nrOfOldBucket++;
          nrOfOldBucket %= 2;
        } catch (Exception e) {
          e.printStackTrace();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        MaxSizeMaxTimeMap map = new MaxSizeMaxTimeMap(5, 1);
        for(int i = 0; i < 20; i++){
            map.put("" + i, "" + i);
            //if(i % 3 == 0) {
            System.out.println("map.get(\"3\") = " + map.get("3"));
            System.out.println("NewMap: " + Arrays.toString(map.getNewBucket().values().toArray()));
            System.out.println("OldMap: " + Arrays.toString(map.getOldBucket().values().toArray()));
        }

        System.out.println("map.get(\"3\") = " + map.get("3"));
        System.out.println("Sleeping ... ");
        Thread.sleep(2 * 1000);
        System.out.println("map.get(\"3\") = " + map.get("3"));




    }











}

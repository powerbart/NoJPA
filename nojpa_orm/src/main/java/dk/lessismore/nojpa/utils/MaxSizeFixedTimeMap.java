package dk.lessismore.nojpa.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created : by IntelliJ IDEA.
 * User: bart
 * Date: 12-11-2008
 * Time: 11:01:42
 * To change this template use File | Settings | File Templates.
 */
public class MaxSizeFixedTimeMap<E> {

    public static class FixedTimeCounterMap {
        MaxSizeFixedTimeMap<Integer> counterMap;

        public FixedTimeCounterMap(int maxCacheSize, long maxSeconds) {
            counterMap = new MaxSizeFixedTimeMap<Integer>(maxCacheSize, maxSeconds);
        }

        public int count(String key) {
            TimeMapEntry<Integer> timeMapEntry = counterMap.getEntry(key);
            if(timeMapEntry == null) {
                timeMapEntry = new TimeMapEntry(0);
            }
            timeMapEntry.value = timeMapEntry.value + 1;
            counterMap.put(key, timeMapEntry);
            return timeMapEntry.value;
        }

        public int getWithoutCounting(String key) {
            TimeMapEntry<Integer> timeMapEntry = counterMap.getEntry(key);
            if(timeMapEntry == null) {
                timeMapEntry = new TimeMapEntry(0);
            }
            return timeMapEntry.value;
        }
    }


    private final Map<String, TimeMapEntry<E>> cachedObjects[] = new Map[] {new HashMap<String, TimeMapEntry<E>>(), new HashMap<String, TimeMapEntry<E>>()};
    private int nrOfOldBucket = 0;
    private int maxCacheSize;
    private long maxSeconds;

    protected static class TimeMapEntry<E> {
        public long firstAccessed = System.currentTimeMillis();
        public E value;


        public TimeMapEntry(E value){
            this.value = value;
        }

        public TimeMapEntry(TimeMapEntry<E> t){
            this.value = t.value;
        }

    }

    public MaxSizeFixedTimeMap(int maxCacheSize, long maxSeconds) {
        this.maxCacheSize = maxCacheSize;
        this.maxSeconds = maxSeconds;
    }


    public synchronized int getNrOfObjectsInCache() {
        return getNewBucket().size() + getOldBucket().size();
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
        put(key, new TimeMapEntry<>(object));
    }

    protected synchronized void put(String key, TimeMapEntry<E> timeMapEntry) {
        try {
          if (isFull(getNewBucket())) {
            shiftBuckets();
          }

          Map<String, TimeMapEntry<E>> newBucket = getNewBucket();
          Map<String, TimeMapEntry<E>> oldBucket = getOldBucket();
          if (newBucket.containsKey(key)) {
              newBucket.put(key, timeMapEntry);
              return;
          }
          if (oldBucket.containsKey(key)) {
            TimeMapEntry<E> o = oldBucket.get(key);
            oldBucket.remove(o);
            newBucket.put(key, o);
          } else {
              newBucket.put(key, timeMapEntry);
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
              put(primaryKey, entry);
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

    protected synchronized TimeMapEntry<E> getEntry(String primaryKey) {
        try {
          Map<String, TimeMapEntry<E>> newBucket = getNewBucket();
          TimeMapEntry<E> entry = newBucket.get(primaryKey);
          if (entry == null) {
            Map<String, TimeMapEntry<E>> oldBucket = getOldBucket();
            entry = oldBucket.get(primaryKey);
            if (entry != null && validTime( entry )) {
              oldBucket.remove(primaryKey);
              put(primaryKey, entry);
              return entry;
            } else {
              return null;
            }
          } else {
            return validTime(entry) ? entry : null;
          }
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
    }

    public synchronized void remove(String key) {
        try {
            getNewBucket().remove(key);
            getOldBucket().remove(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private  boolean validTime(TimeMapEntry<E> entry){
        long now = System.currentTimeMillis();
        return now < maxSeconds * 1000 + entry.firstAccessed;
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
        FixedTimeCounterMap counterMap = new FixedTimeCounterMap(20, 1);
        for(int i = 0; i < 20; i++) {
            System.out.println("0:" + counterMap.count("0"));
            System.out.println( i+ ":--" + counterMap.count("" + i));
            Thread.sleep(200);
        }
    }



//    public static void main(String[] args) throws InterruptedException {
//        MaxSizeFixedTimeMap map = new MaxSizeFixedTimeMap(5, 1);
//        for(int i = 0; i < 20; i++){
//            map.put("" + i, "" + i);
//            //if(i % 3 == 0) {
//            System.out.println("map.get(\"3\") = " + map.get("3"));
//            System.out.println("NewMap: " + Arrays.toString(map.getNewBucket().values().toArray()));
//            System.out.println("OldMap: " + Arrays.toString(map.getOldBucket().values().toArray()));
//            Thread.sleep(200);
//        }
//
//        System.out.println("map.get(\"3\") = " + map.get("3"));
//        System.out.println("Sleeping ... ");
//        Thread.sleep(2 * 1000);
//        System.out.println("map.get(\"3\") = " + map.get("3"));
//
//    }
}

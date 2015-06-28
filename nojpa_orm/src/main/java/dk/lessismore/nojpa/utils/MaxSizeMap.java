package dk.lessismore.nojpa.utils;

import java.util.*;

public class MaxSizeMap<K, V> {


    private final Map<K, V>  cachedObjects[] = new Map[] {new HashMap<K, V> (), new HashMap<K, V>()};
    private int nrOfOldBucket = 0;
    private int maxCacheSize = 2000;

    public MaxSizeMap(int maxCacheSize) {
      this.maxCacheSize = maxCacheSize;
    }


    public synchronized int getNrOfObjectsInCache() {
        return getNewBucket().size()+getOldBucket().size();
    }

    public Map[] getCachedObjects() {
      return cachedObjects;
    }

    public Map<K, V>  getNewBucket() {
      return getCachedObjects()[((nrOfOldBucket+1)%2)];
    }

    public Map<K, V>  getOldBucket() {
      return getCachedObjects()[(nrOfOldBucket%2)];
    }

    public boolean isFull(Map bucket) {
      return bucket.size() == maxCacheSize;
    }


    public synchronized void put(K key, V object) {
        try {
          if (isFull(getNewBucket())) {
            shiftBuckets();
          }

          Map newBucket = getNewBucket();
          Map oldBucket = getOldBucket();
          if (newBucket.containsKey(key)) {
              newBucket.put(key, object);
              return;
          }
          if (oldBucket.containsKey(key)) {
            Object o = oldBucket.get(key);
            oldBucket.remove(o);
            newBucket.put(key, o);
          } else {
              newBucket.put(key, object);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
    }


    /** This method gets an object with the given primary key from the cache. */
    public synchronized V get(K primaryKey) {
        try {
          Map<K, V>  newBucket = getNewBucket();
          V entry = newBucket.get(primaryKey);
          if (entry == null) {
            Map<K, V>  oldBucket = getOldBucket();
            entry = oldBucket.get(primaryKey);
            if (entry != null) {
              oldBucket.remove(primaryKey);
              put(primaryKey, entry);
              return entry;
            } else {
              return null;
            }
          } else {
            return entry;
          }
        // TODO: Is it intended catch all exceptions (including RuntimeException) here?
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
    }

    private synchronized void shiftBuckets() {
        try {
          Map oldBucket = getOldBucket();
          oldBucket.clear();
          nrOfOldBucket++;
          nrOfOldBucket %= 2;
        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    public synchronized void clear(){
        shiftBuckets();
        shiftBuckets();
    }


    public static void main(String[] args) {
        MaxSizeMap map = new MaxSizeMap(5);
        for(int i = 0; i < 20; i++){
            map.put("" + i, "" + i);
            //if(i % 3 == 0) {
            System.out.println("map.get(\"3\") = " + map.get("3"));
            System.out.println("NewMap: " + Arrays.toString(map.getNewBucket().values().toArray()));
            System.out.println("OldMap: " + Arrays.toString(map.getOldBucket().values().toArray()));
        }

        System.out.println("NewMap: " + Arrays.toString(map.getNewBucket().values().toArray()));
        System.out.println("OldMap: " + Arrays.toString(map.getOldBucket().values().toArray()));
        System.out.println("map.get(\"3\") = " + map.get("3"));
        for(int i = 0; i < 3; i++){
            map.put("" + i, "" + i);
        }
        System.out.println("map.get(\"3\") = " + map.get("3"));




    }











}

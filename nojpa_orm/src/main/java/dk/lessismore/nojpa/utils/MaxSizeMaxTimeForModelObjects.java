package dk.lessismore.nojpa.utils;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class MaxSizeMaxTimeForModelObjects<E extends ModelObjectInterface> {

    private final Class<? extends ModelObjectInterface> clazz;
    private final MaxSizeMaxTimeMap<List<String>> map;
    private int nrOfOldBucket = 0;
    private int maxCacheSize;
    private long maxSeconds;


    public MaxSizeMaxTimeForModelObjects(Class<? extends ModelObjectInterface> clazz, int maxCacheSize, long maxSeconds) {
        this.clazz = clazz;
        this.maxCacheSize = maxCacheSize;
        this.maxSeconds = maxSeconds;
        map = new MaxSizeMaxTimeMap<List<String>>(maxCacheSize, maxSeconds);
    }


    public void put(String key, List<E> in){
        List<String> strings = toStrings(in);
        map.put(key, strings);
    }


    public List<E> get(String key){
        List<String> strings = map.get(key);
        if(strings != null && !strings.isEmpty()){
            return toModelObjects(strings);
        }
        return null;
    }



    private List<String> toStrings(List<E> in){
        List<String> toReturn = new ArrayList<String>();
        for(int i = 0; i < in.size(); i++){
            toReturn.add(""+ in.get(i));
        }
        return toReturn;
    }

    private List<E> toModelObjects(List<String> in){
        return (List<E>) MQL.selectByIDs(clazz, in);
    }


}

package dk.lessismore.nojpa.reflection.db.model.nosql;

import java.util.Calendar;
import java.util.List;

public interface NoSQLInputDocument {
    void addShard(String shard);
    String getShard();
    void addField(String varName, String objectID);
    void addField(String varName, Long value);
    void addField(String varName, Integer value);
    void addField(String varName, Boolean value);
    void addField(String varName, Float value);
    void addField(String varName, Double value);
    void addField(String varName, Calendar value);
    void addField(String varName, List values);
//    void addField(String varName, Object values);
}

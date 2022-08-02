package dk.lessismore.nojpa.reflection.db.model.nosql;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

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

    void addPostfixShardName(String postfixShardName);
    String getPostfixShardName();

    void addTranslatedFieldName(String solrAttributeName);

    Set<String> getTranslateFields();

    Set<String> getAllFields();
    Object getValue(String f);
    void setField(String varName, Object values);
}

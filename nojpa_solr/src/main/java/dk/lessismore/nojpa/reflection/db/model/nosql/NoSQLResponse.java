package dk.lessismore.nojpa.reflection.db.model.nosql;

import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.db.methodquery.NStats;
import dk.lessismore.nojpa.utils.Pair;

import java.util.List;

public interface NoSQLResponse {
    long getNumFound();

    int size();

    String getID(int i);

    Object getRaw(int i);

    <N extends Number> NStats<N> getStats(String attributeIdentifier);


    List<Pair<String, Long>> getFacet(String attributeIdentifier);
}

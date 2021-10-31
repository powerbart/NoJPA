package dk.lessismore.nojpa.reflection.db.model.nosql;

import dk.lessismore.nojpa.db.methodquery.NStats;
import dk.lessismore.nojpa.utils.Pair;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.elasticsearch.search.aggregations.Aggregations;

import java.util.List;

public interface NoSQLResponse {

    Aggregations getAggregations();

    long getNumFound();

    long getQTime();

    long getQTimeIncludingNetwork();

    int size();

    String getID(int i);

    Object getRaw(int i);

    <N extends Number> NStats<N> getStats(String attributeIdentifier);

    List<Pair<String, Long>> getFacet(String attributeIdentifier);
    List<BucketJsonFacet> getDateRangeFacet();
}

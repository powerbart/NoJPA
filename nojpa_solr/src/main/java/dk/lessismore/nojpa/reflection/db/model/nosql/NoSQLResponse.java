package dk.lessismore.nojpa.reflection.db.model.nosql;

import dk.lessismore.nojpa.db.methodquery.NStats;
import dk.lessismore.nojpa.utils.Pair;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.elasticsearch.search.aggregations.Aggregations;

import java.util.List;
import java.util.Map;

public interface NoSQLResponse {

    Aggregations getAggregations();

    long getNumFound();

    long getQTime();

    long getQTimeIncludingNetwork();

    Map<String, Map<String, List<String>>> getHighlighting();

    int size();

    String getID(int i);

    Object getRaw(int i);

    <N extends Number> NStats<N> getStats(String attributeIdentifier);

    List<Pair<String, Long>> getFacet(String attributeIdentifier);
    List<BucketJsonFacet> getDateRangeFacet();
}

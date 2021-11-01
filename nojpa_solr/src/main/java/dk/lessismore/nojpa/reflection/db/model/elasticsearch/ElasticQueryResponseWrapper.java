package dk.lessismore.nojpa.reflection.db.model.elasticsearch;

import dk.lessismore.nojpa.db.methodquery.NStats;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import dk.lessismore.nojpa.utils.Pair;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregations;

import java.util.List;
import java.util.Map;

public class ElasticQueryResponseWrapper implements NoSQLResponse {


    private final SearchResponse response;

    private final long qtimeIncludingNetwork;

    public ElasticQueryResponseWrapper(SearchResponse response, long ms){
        this.response = response;
        this.qtimeIncludingNetwork = ms;
    }


    @Override
    public Aggregations getAggregations() {
        return response.getAggregations();
    }

    @Override
    public long getNumFound() {
        return response.getHits().getTotalHits();
    }

    @Override
    public long getQTime() {
        return response.getTook().millis();
    }

    @Override
    public long getQTimeIncludingNetwork() {
        return qtimeIncludingNetwork;
    }

    @Override
    public Map<String, Map<String, List<String>>> getHighlighting() {
        return null; // not implemented for es
    }

    @Override
    public int size() {
        return (response.getHits().getHits() != null ? response.getHits().getHits().length : -1);
    }

    @Override
    public String getID(int i) {
//        Map<String, DocumentField> fields = response.getHits().getHits()[i].getId();
//        return fields.get("objectID").getValue();
        return response.getHits().getHits()[i].getId();
    }

    @Override
    public Object getRaw(int i) {
        return response.getHits().getHits()[i].getFields();
    }

    @Override
    public <N extends Number> NStats<N> getStats(String attributeIdentifier) {
        throw new RuntimeException("Not implemented yet ... ");

    }

    @Override
    public List<Pair<String, Long>> getFacet(String attributeIdentifier) {
        throw new RuntimeException("Not implemented yet ... ");
    }

    @Override
    public List<BucketJsonFacet> getDateRangeFacet() {
        throw new RuntimeException("Not implemented yet ... ");
    }
}

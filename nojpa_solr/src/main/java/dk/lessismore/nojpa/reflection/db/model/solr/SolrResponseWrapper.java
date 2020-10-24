package dk.lessismore.nojpa.reflection.db.model.solr;

import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.db.methodquery.NStats;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import dk.lessismore.nojpa.utils.Pair;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.elasticsearch.search.aggregations.Aggregations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolrResponseWrapper implements NoSQLResponse {

    private static final Logger log = LoggerFactory.getLogger(SolrResponseWrapper.class);

    private final QueryResponse query;

    public SolrResponseWrapper(QueryResponse query) {
        this.query = query;
    }

    @Override
    public Aggregations getAggregations() {
        throw new RuntimeException("Not implemented getAggregations() on Solr");
    }

    @Override
    public long getNumFound() {
        return query.getResults().getNumFound();
    }

    @Override
    public int size() {
        return query.getResults().size();
    }

    @Override
    public String getID(int i) {
        return (String) query.getResults().get(i).get("objectID");
    }

    @Override
    public Object getRaw(int i) {
        return query.getResults().get(i);
    }

    @Override
    public <N extends Number> NStats<N> getStats(String attributeIdentifier) {
        NStats<N> nStats = new NStats<N>();
        Map<String, FieldStatsInfo> fieldStatsInfo = this.query.getFieldStatsInfo();

        FieldStatsInfo sInfo = fieldStatsInfo.get(attributeIdentifier);
        if(sInfo == null){
            return nStats;
        }
        nStats.setMin((Double) sInfo.getMin());
        nStats.setMax((Double) sInfo.getMax());
        nStats.setSum((Double) sInfo.getSum());
        nStats.setCount(sInfo.getCount());
        nStats.setMean((Double) sInfo.getMean());
        nStats.setStddev((Double) sInfo.getStddev());

        return nStats;

    }

    @Override
    public List<Pair<String, Long>> getFacet(String attributeIdentifier) {

        List<Pair<String, Long>> toReturn = new ArrayList<Pair<String, Long>>();

        List<FacetField> facetFields = this.query.getFacetFields();
        if(facetFields == null){
            log.warn("Are you calling getFacet, when you should have been calling getDateRangeFacet? ... This will end bad ... ");
        }
        for(int i = 0; i < facetFields.size(); i++){
            FacetField facetField = facetFields.get(i);
            String name = facetField.getName();
            if(name.equals(attributeIdentifier)){
                for (FacetField.Count facet : facetField.getValues()) {
                    toReturn.add(new Pair<>(facet.getName(), facet.getCount()));
                }

            }
        }

        return toReturn;
    }

    public List<BucketJsonFacet> getDateRangeFacet() {
        return this.query.getJsonFacetingResponse().getBucketBasedFacets("nqlFacet").getBuckets();
    }


}

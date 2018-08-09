package dk.lessismore.nojpa.reflection.db.model.elasticsearch;

import dk.lessismore.nojpa.db.methodquery.NQL;
import org.apache.solr.client.solrj.SolrQuery;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchQuery extends NQL.SearchQuery{

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchQuery.class);


    SolrQuery solrQuery;
    private String indices;
    private String type;
    private SearchSourceBuilder queryBuilder;

    public ElasticSearchQuery(Class selectClass) {
        super(selectClass);
        solrQuery = new SolrQuery();
    }

    public SolrQuery getSolrQuery() {
        return solrQuery;
    }

    protected void buildQuery(){
        StringBuilder builder = new StringBuilder();
        solrQuery.setQuery("*:*");
        for (int i = 0; i < rootConstraints.size(); i++) {
            NQL.Constraint constraint = (NQL.Constraint) rootConstraints.get(i);
            String subQuery = constraint.getExpression().updateQuery(solrQuery);
            if (subQuery != null) {
                builder.append(subQuery);
            }
        }
        String query = builder.toString();
        if (query == null || query.length() < 2) {
            query = "*:*";
        } else {
            if (preBoost != null) {
                query = preBoost + " " + query;
            }
        }
        log.debug("We will query = " + query);
        solrQuery.setQuery(query);
        if (startLimit != -1) {
            solrQuery.setStart(startLimit);
            solrQuery.setRows(endLimit - startLimit);
        }
        if (this.orderByAttribute != null) {
            log.debug("Will sort by " + orderByAttribute + " with " + this.orderByORDER);
            solrQuery.addSort(this.orderByAttribute, this.orderByORDER == NQL.Order.ASC ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        }
    }

    @Override
    protected String toStringDebugQuery() {
        return solrQuery.toString().replace('+', ' ');
    }


    public String getIndices() {
        return indices;
    }

    public String getType() {
        return type;
    }

    public SearchSourceBuilder getQueryBuilder() {
        return queryBuilder;
    }
}

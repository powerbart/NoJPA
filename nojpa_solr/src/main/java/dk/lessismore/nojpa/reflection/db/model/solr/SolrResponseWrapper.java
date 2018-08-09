package dk.lessismore.nojpa.reflection.db.model.solr;

import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import org.apache.solr.client.solrj.response.QueryResponse;

public class SolrResponseWrapper implements NoSQLResponse {

    private final QueryResponse query;

    public SolrResponseWrapper(QueryResponse query) {
        this.query = query;
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
}

package dk.lessismore.nojpa.reflection.db.model.elasticsearch;

import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import org.elasticsearch.action.search.SearchResponse;

public class ElasticQueryResponseWrapper implements NoSQLResponse {


    private final SearchResponse response;

    public ElasticQueryResponseWrapper(SearchResponse response){
        this.response = response;
    }


    @Override
    public long getNumFound() {
        return 0L;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String getID(int i) {
        return null;
    }

    @Override
    public Object getRaw(int i) {
        return null;
    }
}

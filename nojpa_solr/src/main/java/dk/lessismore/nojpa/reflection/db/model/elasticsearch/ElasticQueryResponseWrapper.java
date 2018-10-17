package dk.lessismore.nojpa.reflection.db.model.elasticsearch;

import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;

import java.util.Map;

public class ElasticQueryResponseWrapper implements NoSQLResponse {


    private final SearchResponse response;

    public ElasticQueryResponseWrapper(SearchResponse response){
        this.response = response;
    }


    @Override
    public long getNumFound() {
        return response.getHits().getTotalHits();
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
}
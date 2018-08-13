package dk.lessismore.nojpa.reflection.db.model.nosql;

import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.io.IOException;

/**
 * Created by seb on 7/23/14.
 */
public interface NoSQLService {


    String getName();

    void index(NoSQLInputDocument solrInputDocument);
    NoSQLResponse query(NQL.SearchQuery query);

    void commit();
    void optimize();
    void delete(String id);
    void empty();
    void destroy() throws IOException;
    void deleteAll();

    NoSQLInputDocument createInputDocument(Class<? extends ModelObjectInterface> clazz, ModelObjectInterface mm);

    <T extends ModelObjectInterface> NQL.SearchQuery createSearchQuery(Class<T> clazz);
}

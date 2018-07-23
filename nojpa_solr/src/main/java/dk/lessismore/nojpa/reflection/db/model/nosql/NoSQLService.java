package dk.lessismore.nojpa.reflection.db.model.nosql;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;

/**
 * Created by seb on 7/23/14.
 */
public interface NoSQLService {


    NoSQLClient getServer();

    String getName();

    void index(NoSQLInputDocument solrInputDocument);
    QueryResponse query(SolrQuery query);

    void commit();
    void optimize();
    void delete(String id);
    void empty();
    void destroy() throws IOException;
    void deleteAll();

}

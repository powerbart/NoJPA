package dk.lessismore.nojpa.reflection.db.model.elasticsearch;

import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLService;
import org.apache.solr.client.solrj.SolrClient;

/**
 * Created by seb on 7/23/14.
 */
public interface ElasticService extends NoSQLService {


    void setCleanOnStartup(boolean cleanOnStartup);

}

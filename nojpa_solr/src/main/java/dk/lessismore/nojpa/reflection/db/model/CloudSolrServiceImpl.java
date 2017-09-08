package dk.lessismore.nojpa.reflection.db.model;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Created on 9/8/17.
 */
public class CloudSolrServiceImpl extends SolrServiceImpl {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String zkHost;
    private String collectionName;

    public CloudSolrServiceImpl(String zkHost, String collectionName) {
        super();
        log.info("[ : constructor]:HIT:zkHost({}) collectionName({})", zkHost, collectionName);
        this.zkHost = zkHost;
        this.collectionName = collectionName;
    }

    protected void startup() {
        log.debug("[void : zkHost (" + zkHost + ")startup]:HIT: " + this);
        try {
            server = new CloudSolrClient.Builder().withZkHost(zkHost).build(); // TODO add collectionName here somehow
        } catch (Exception e) {
            log.error("[ void: (" + zkHost + ")startup ]: exception constructing embedded server: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        log.debug("closing down solr [{}]", getName());
        server.close();
    }


    @Override
    public void commit() {
        // ignore
    }

    @Override
    public String getName() {
        return String.format("%s|%s", zkHost, collectionName);
    }

    @Override
    public QueryResponse query(SolrQuery query) {
        try {
            if(server != null){
                return server.query(collectionName, query);
            } else {
                log.error("query() ... server is null ... This is okay doing startup ...");
                return null;
            }
        } catch (Exception e) {
            log.error("[QueryResponse : (" + collectionName + ")query]: SolrException: " + e.getMessage(), e);
        }
        return null;

    }

    @Override
    public NamedList<Object> request(SolrRequest req) {
        try {
            return server.request(req, collectionName);
        } catch (SolrServerException e) {
            log.error("[] void: (" + collectionName + ")request ]: SolrServerException request: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[ void: (" + collectionName + ")request ]: IOException request: " + e.getMessage(), e);
        }
        return null;
    }
}

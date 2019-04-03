package dk.lessismore.nojpa.reflection.db.model.solr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Created on 9/8/17.
 */
public class CloudSolrServiceImpl extends SolrServiceImpl {

    public static int AUTO_COMMIT_MS = 300;


    private final Logger log = LoggerFactory.getLogger(getClass());

    private String zkHost;
    private String collectionName;

    public CloudSolrServiceImpl(String zkHost, String collectionName) {
        super();
        log.info("[ : constructor]:HIT:zkHost({}) collectionName({})", zkHost, collectionName);
        this.zkHost = zkHost;
        this.collectionName = collectionName;
        startup();
    }

    @Override
    public void startup() {
        log.debug("[void : zkHost (" + zkHost + ")startup]:HIT: " + this);
        try {
            server = new CloudSolrClient.Builder(ImmutableList.of(zkHost), java.util.Optional.empty()).build();
            ((CloudSolrClient) server).setDefaultCollection(collectionName);
        } catch (Exception e) {
            log.error("[ void: (" + zkHost + ")startup ]: exception constructing embedded server: " + e.getMessage(), e);
        }
    }

    @Override
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
    public NoSQLResponse query(NQL.SearchQuery query) {
        try {
            if (server != null) {
                return new SolrResponseWrapper(server.query(collectionName, ((SolrSearchQuery)query).getSolrQuery()));
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

    @Override
    public void index(SolrInputDocument solrInputDocument) {
        if (solrInputDocument == null) {
            log.error("index()::solrInputDocument is null");
            return;
        }
        try {
            server.add(collectionName, solrInputDocument, AUTO_COMMIT_MS);
        } catch (SolrServerException e) {
            log.error("[void : (" + collectionName + ")index]: SolrServerException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[void : (" + collectionName + ")index]: IOException: " + e.getMessage(), e);
        }
    }


    @Override
    public void delete(String id) {
        try {
            server.deleteById(collectionName, id, AUTO_COMMIT_MS);
        } catch (SolrServerException e) {
            log.error("[ void: (" + collectionName + ")deleteByID(" + id + ") ]: SolrServerException deleteByID index: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[ void: (" + collectionName + ")deleteByID(" + id + ") ]: IOException deleteByID index: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            server.deleteByQuery(collectionName, "*:*", AUTO_COMMIT_MS);
        } catch (Exception e) {
            log.error("[ void: (" + collectionName + ")deleteAll index: " + e.getMessage(), e);
        }
    }

    @Override
    public void optimize() {
        try {
            server.optimize(collectionName, false, false, 4);
        } catch (SolrServerException e) {
            log.error("[void : (" + collectionName + ")optimize]: SolrServerException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[void : (" + collectionName + ")optimize]: IOException: " + e.getMessage(), e);
        }
    }
}

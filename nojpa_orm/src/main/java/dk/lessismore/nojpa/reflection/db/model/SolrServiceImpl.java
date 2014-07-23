package dk.lessismore.nojpa.reflection.db.model;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by niakoi on 7/23/14.
 */
public class SolrServiceImpl implements SolrService {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SolrServiceImpl.class);

    private static CoreContainer coreContainer;

    private SolrServer server;
    private String coreName = "";
    private boolean cleanOnStartup;

    public SolrServiceImpl() {
        log.debug("[ : constructor]:HIT:");
    }
    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }

    public void setCleanOnStartup(boolean cleanOnStartup) {
        this.cleanOnStartup = cleanOnStartup;
    }

    public SolrServer getServer() {
        synchronized (this){
            if(server == null){
                startup();
            }
        }
        return server;
    }

    @Override
    public void startup() {
        log.debug("[void : (" + coreName + ")startup]:HIT: " + this);
        try {
            if (coreContainer == null) {
                File f = getSolrXml();
                log.debug("[ : config ]:: " + f);
                coreContainer = new CoreContainer(f.getParentFile().getAbsolutePath());  //
                coreContainer.load();
//                coreContainer.load(f.getParentFile().getAbsolutePath(), f);
            }
            if (server == null) {
                log.debug("[ : cores]:getting " + coreName + " core: " + coreContainer.getCoreNames());
                server = new EmbeddedSolrServer(coreContainer, coreName);
            }

            if (cleanOnStartup) {
                empty();
            }

        } catch (Exception e) {
            log.error("[ void: (" + coreName + ")startup ]: exception constructing embedded server: " + e.getMessage(), e);
        }
    }

    public void index(SolrInputDocument solrInputDocument){
        if (solrInputDocument == null) {
            log.error("index()::solrInputDocument is null");
            return;
        }
        try {
            server.add(solrInputDocument);
        } catch (SolrServerException e) {
            log.error("[void : (" + coreName + ")index]: SolrServerException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[void : (" + coreName + ")index]: IOException: " + e.getMessage(), e);
        }
    }


    @Override
    public QueryResponse query(SolrQuery query) {
        try {
            if(server != null){
                return server.query(query);
            } else {
                log.error("query() ... server is null ... This is okay doing startup ...");
                return null;
            }
        } catch (Exception e) {
            log.error("[QueryResponse : (" + coreName + ")query]: SolrException: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void commit() {
        try {
            log.debug("[void : (" + coreName + ")commit]:COMMIT");
            server.commit();
        } catch (SolrServerException e) {
            log.error("[void : (" + coreName + ")commit]: SolrServerException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[void : (" + coreName + ")commit]: IOException: " + e.getMessage(), e);
        }
    }

    @Override
    public void optimize() {
        try {
            server.optimize(false, false, 4);
        } catch (SolrServerException e) {
            log.error("[void : (" + coreName + ")optimize]: SolrServerException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[void : (" + coreName + ")optimize]: IOException: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            server.deleteById(id);
        } catch (SolrServerException e) {
            log.error("[ void: (" + coreName + ")deleteByID(" + id + ") ]: SolrServerException deleteByID index: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[ void: (" + coreName + ")deleteByID(" + id + ") ]: IOException deleteByID index: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            server.deleteByQuery("*:*");
        } catch (SolrServerException e) {
            log.error("[ void: (" + coreName + ")deleteAll index: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[ void: (" + coreName + ")deleteAll index: " + e.getMessage(), e);
        }
    }

    @Override
    public void empty() {
        try {
            log.debug("[void : (" + coreName + ")empty]:EMPTY : ");
            server.deleteByQuery("*:*");
            commit();
        } catch (SolrServerException e) {
            log.error("[ void: (" + coreName + ")empty ]: SolrServerException empty index: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[ void: (" + coreName + ")empty ]: IOException empty index: " + e.getMessage(), e);
        }
    }

    @Override
    public NamedList<Object> request(SolrRequest req) {
        try {
            return server.request(req);
        } catch (SolrServerException e) {
            log.error("[] void: (" + coreName + ")request ]: SolrServerException request: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[ void: (" + coreName + ")request ]: IOException request: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void destroy() {
        coreContainer.shutdown();
    }

    @Override
    public <T> T getByID(String id, Class<T> type) {
        SolrQuery query = new SolrQuery(id).setRows(1);
        QueryResponse response = query(query);
        List<T> beans = response.getBeans(type);
        if (beans != null && beans.size() > 0) {
            return beans.get(0);
        }
        return null;
    }


    public File getSolrXml() {
        try {
            return new File(getClass().getResource("/solr/solr.xml").getFile());
        } catch (Exception e) {
            log.error("[File : getSolrXml]: can't get solr xml resource: " + e.getMessage(), e);
        }
        return null;
    }
}
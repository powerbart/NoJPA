package dk.lessismore.nojpa.reflection.db.model;

import dk.lessismore.nojpa.reflection.translate.TranslateService;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by niakoi on 16/3/16.
 */
public class SolrEmbeddedServiceImpl implements SolrService {

    private static TranslateService translateService = null;
    private static Locale[] locales = null;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static CoreContainer coreContainer;

    protected SolrClient client;
    private String coreName = "";
    private boolean cleanOnStartup;

    public SolrEmbeddedServiceImpl() {
    }

    public SolrEmbeddedServiceImpl(String coreName) {
        log.warn("[ : constructor]:HIT:" + coreName);
        this.coreName = coreName;
    }

    public void setCleanOnStartup(boolean cleanOnStartup) {
        this.cleanOnStartup = cleanOnStartup;
    }

    public SolrClient getServer() {
        synchronized (this) {
            if (client == null) {
                startup();
            }
        }
        return client;
    }

    @Override
    public String getName() {
        return coreName;
    }

    @Override
    public void addTranslateService(TranslateService translateService, Locale... locales) {
        SolrEmbeddedServiceImpl.translateService = translateService;
        SolrEmbeddedServiceImpl.locales = locales;
    }

    protected void startup() {
        log.debug("[void : (" + coreName + ")startup]:HIT: " + this);
        try {
            if (coreContainer == null) {
                File f = getSolrXml();
                log.debug("[ : config ]:: " + f);
                coreContainer = new CoreContainer(f.getParentFile().getAbsolutePath());  //
                log.debug("[ : config ]:: loading container on " + coreContainer.getCoreRootDirectory());
                coreContainer.load();
            }
            if (client == null) {
                log.debug("[ : cores]:getting " + coreName + " core: " + coreContainer.getCoreNames());
                client = new EmbeddedSolrServer(coreContainer, coreName);
            }

            if (cleanOnStartup) {
                empty();
            }

        } catch (Exception e) {
            log.error("[ void: (" + coreName + ")startup ]: exception constructing embedded server: " + e.getMessage(), e);
        }
    }

    public void index(SolrInputDocument solrInputDocument) {
        if (solrInputDocument == null) {
            log.error("index()::solrInputDocument is null");
            return;
        }
        try {
            client.add(solrInputDocument);
        } catch (SolrServerException e) {
            log.error("[void : (" + coreName + ")index]: SolrServerException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[void : (" + coreName + ")index]: IOException: " + e.getMessage(), e);
        }
    }


    @Override
    public QueryResponse query(SolrQuery query) {
        try {
            if (client != null) {
                return client.query(query);
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
            client.commit();
        } catch (SolrServerException e) {
            log.error("[void : (" + coreName + ")commit]: SolrServerException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[void : (" + coreName + ")commit]: IOException: " + e.getMessage(), e);
        }
    }

    @Override
    public void optimize() {
        try {
            client.optimize(false, false, 4);
        } catch (SolrServerException e) {
            log.error("[void : (" + coreName + ")optimize]: SolrServerException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[void : (" + coreName + ")optimize]: IOException: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            client.deleteById(id);
        } catch (SolrServerException e) {
            log.error("[ void: (" + coreName + ")deleteByID(" + id + ") ]: SolrServerException deleteByID index: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[ void: (" + coreName + ")deleteByID(" + id + ") ]: IOException deleteByID index: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            client.deleteByQuery("*:*");
        } catch (Exception e) {
            log.error("[ void: (" + coreName + ")deleteAll index: " + e.getMessage(), e);
        }
    }

    @Override
    public void empty() {
        try {
            log.debug("[void : (" + coreName + ")empty]:EMPTY : ");
            client.deleteByQuery("*:*");
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
            return client.request(req);
        } catch (SolrServerException e) {
            log.error("[] void: (" + coreName + ")request ]: SolrServerException request: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("[ void: (" + coreName + ")request ]: IOException request: " + e.getMessage(), e);
        }
        return null;
    }

    @PreDestroy
    public void destroy() throws IOException {
        log.debug("closing down solr [{}]", getName());
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

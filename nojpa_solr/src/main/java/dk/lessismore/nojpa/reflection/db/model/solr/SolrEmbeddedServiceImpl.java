package dk.lessismore.nojpa.reflection.db.model.solr;

import dk.lessismore.nojpa.reflection.translate.TranslateService;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by niakoi on 16/3/16.
 */
public class SolrEmbeddedServiceImpl extends SolrServiceImpl {

    private static TranslateService translateService = null;
    private static Locale[] locales = null;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static CoreContainer coreContainer;

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
            if (server == null) {
                startup();
            }
        }
        return server;
    }

    @Override
    public String getName() {
        return coreName;
    }


    protected void startup() {
        log.debug("[void : (" + coreName + ")startup]:HIT: " + this);
        try {
            if (coreContainer == null) {
                String solrHome = getSolrHome();
                coreContainer = new CoreContainer(solrHome);
                log.debug("[ : config ]:: loading container on " + coreContainer.getCoreRootDirectory());
                coreContainer.load();
            }
            if (server == null) {
                log.debug("[ : cores]:getting " + coreName + " core: " + coreContainer.getAllCoreNames());
                server = new EmbeddedSolrServer(coreContainer, coreName);
            }

            if (cleanOnStartup) {
                empty();
            }

        } catch (Exception e) {
            log.error("[ void: (" + coreName + ")startup ]: exception constructing embedded server: " + e.getMessage(), e);
        }
    }


    @PreDestroy
    public void destroy() throws IOException {
        log.debug("closing down solr [{}]", getName());
        coreContainer.shutdown();
    }

    public String getSolrHome() {
        String solrHome = System.getProperty("solr.home");
        if (StringUtils.isEmpty(solrHome)) {
            solrHome = new File("./solr-config").getAbsolutePath();
        }
        return solrHome;
    }

}

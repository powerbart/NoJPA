package dk.lessismore.nojpa.reflection.db.model.solr;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Created by niakoi on 16/3/16.
 */
public class SolrRemoteServiceImpl extends SolrEmbeddedServiceImpl {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String url = "";


    public SolrRemoteServiceImpl(String url) {
        log.debug("[ : constructor]:HIT:" + url);
        this.url = url;
    }

    public void startup() {
        log.debug("[void : (" + url + ")startup]:HIT: " + this);
        try {
            server = new HttpSolrClient.Builder(url).build();
        } catch (Exception e) {
            log.error("[ void: (" + url + ")startup ]: exception constructing embedded server: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        log.debug("closing down solr [{}]", getName());
        server.close();
    }

}
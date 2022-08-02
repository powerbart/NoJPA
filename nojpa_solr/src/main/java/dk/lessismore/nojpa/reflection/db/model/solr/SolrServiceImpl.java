package dk.lessismore.nojpa.reflection.db.model.solr;

import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLInputDocument;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by seb on 7/23/14.
 */
public abstract class SolrServiceImpl implements SolrService {
    private static final Logger log = LoggerFactory.getLogger(SolrServiceImpl.class);

    protected SolrClient server;
    private String coreName = "";

    public SolrServiceImpl() {
        log.debug("[ : constructor]:HIT:");
    }
    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }


    public SolrClient getSolrClient() {
        return server;
    }

    public String getName() {
        return coreName;
    }

    @Override
    public void index(NoSQLInputDocument noSQLInputDocument) {
        String shard = noSQLInputDocument.getShard() != null ? noSQLInputDocument.getShard() + (noSQLInputDocument.getPostfixShardName() != null ? noSQLInputDocument.getPostfixShardName() : "") :  null;
        index(((NoSQLInputDocumentWrapper) noSQLInputDocument).document, shard);
    }

    public abstract void startup();

    public void index(SolrInputDocument solrInputDocument, String shard){
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
    public NoSQLResponse query(NQL.SearchQuery query, String postShardName) {
        try {
            if(server != null){
                long start = System.currentTimeMillis();
                QueryResponse queryResponse = server.query(((SolrSearchQuery) query).getSolrQuery(), SolrRequest.METHOD.POST);
                return new SolrResponseWrapper(queryResponse, System.currentTimeMillis() - start);
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
    public QueryResponse query(SolrQuery query, String postShardName) {
        try {
            if(server != null){
                return server.query(query, SolrRequest.METHOD.POST);
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
    public void delete(String id, String shard) {
        delete(id); // TODO use the shard
    }

    @Override
    public void deleteAll() {
        try {
            server.deleteByQuery("*:*");
        } catch (Exception e) {
            log.error("[ void: (" + coreName + ")deleteAll index: " + e.getMessage(), e);
        }
    }

    public static class NoSQLInputDocumentWrapper implements NoSQLInputDocument {

        SolrInputDocument document = new SolrInputDocument();

        Set<String> translatedFields = new HashSet<>();
        String shard = null;
        String postfixShardName = null;

        @Override
        public void addShard(String shard) {
            this.shard = shard;
        }

        @Override
        public String getShard() {
            return shard;
        }

        @Override
        public void addField(String objectIDVarName, String objectID) {
            document.addField(objectIDVarName, objectID);
        }

        @Override
        public void addField(String objectIDVarName, Long value) {
            document.addField(objectIDVarName, value);
        }

        @Override
        public void addField(String objectIDVarName, Integer value) {
            document.addField(objectIDVarName, value);
        }

        @Override
        public void addField(String objectIDVarName, Boolean value) {
            document.addField(objectIDVarName, value);
        }

        @Override
        public void addField(String objectIDVarName, Float value) {
            document.addField(objectIDVarName, value);
        }

        @Override
        public void addField(String objectIDVarName, Double value) {
            document.addField(objectIDVarName, value);
        }

        @Override
        public void addField(String objectIDVarName, Calendar value) {
            document.addField(objectIDVarName, value);
        }

        @Override
        public void addField(String objectIDVarName, List values) {
            document.addField(objectIDVarName, values);
        }

        @Override
        public void addPostfixShardName(String postfixShardName) {
            this.postfixShardName = postfixShardName;
        }

        @Override
        public String getPostfixShardName() {
            return postfixShardName;
        }

        @Override
        public void addTranslatedFieldName(String solrAttributeName) {
            translatedFields.add(solrAttributeName);
        }

        @Override
        public Set<String> getTranslateFields() {
            return translatedFields;
        }

        @Override
        public Set<String> getAllFields() {
            return document.keySet();
        }

        @Override
        public Object getValue(String f) {
            return document.getFieldValue(f);
        }

        @Override
        public void setField(String varName, Object values) {
            document.addField(varName, values);
        }
//
//        @Override
//        public void addField(String objectIDVarName, Object value) {
//            document.addField(objectIDVarName, value);
//        }


    }


    @Override
    public NoSQLInputDocument createInputDocument(Class<? extends ModelObjectInterface> clazz, ModelObjectInterface modelObjectInterface) {
        return new NoSQLInputDocumentWrapper();
    }



    @Override
    public <T extends ModelObjectInterface> NQL.SearchQuery createSearchQuery(Class<T> clazz) {
        return new SolrSearchQuery(clazz);
    }

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

    public abstract void destroy() throws IOException;

}

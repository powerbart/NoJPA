package dk.lessismore.nojpa.reflection.db.model.elasticsearch;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLInputDocument;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLQuery;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import dk.lessismore.nojpa.reflection.translate.TranslateService;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by seb on 7/23/14.
 */
public class ElasticServiceImpl implements ElasticService {


    private static final Logger log = LoggerFactory.getLogger(ElasticServiceImpl.class);

    private final static Header[] HEADERS = { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };

    protected String host;
    protected int port;
    protected Class<? extends ModelObjectInterface> clazz;
    protected String coreName;

    protected RestHighLevelClient client;
    private boolean cleanOnStartup;

    public ElasticServiceImpl(String host, int port, Class<? extends ModelObjectInterface> clazz) {
        log.debug("[: constructor]:HIT: host("+ host +":"+ port +") for " + clazz.getSimpleName());
        this.host = host;
        this.port = port;
        this.clazz = clazz;
        this.coreName = clazz.getSimpleName();
    }

    public void setCleanOnStartup(boolean cleanOnStartup) {
        this.cleanOnStartup = cleanOnStartup;
    }



    public String getName() {
        return clazz.getSimpleName();
    }


    @Override
    public void index(NoSQLInputDocument noSQLInputDocument) {
        index(((ElasticInputDocumentWrapper) noSQLInputDocument));
    }


    protected void startup() {
        log.debug("[void : (" + clazz.getSimpleName() + ")startup]:HIT: " + this);
        try {
            if (client == null) {
                client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
            }

            if (cleanOnStartup) {
                empty();
            }

        } catch (Exception e) {
            log.error("[ void: (" + clazz.getSimpleName() + ")startup ]: exception constructing server: " + e.getMessage(), e);
        }
    }

    public void index(ElasticInputDocumentWrapper inputDocument){
        if (inputDocument == null) {
            log.error("index()::solrInputDocument is null");
            return;
        }
        try {
            IndexRequest request = new IndexRequest("funny33", clazz.getSimpleName(), inputDocument.node.get("objectID").textValue());
            client.index(request, HEADERS);
        } catch (Exception e) {
            log.error("[void : (" + coreName + ")index]: IOException: " + e.getMessage(), e);
        }
    }


    @Override
    public NoSQLResponse query(NQL.SearchQuery query) {
        try {
            if(client != null){
                ElasticSearchQuery elasticSearchQuery = (ElasticSearchQuery) query;
                SearchRequest searchRequest = new SearchRequest(elasticSearchQuery.getIndices());
                searchRequest.types(elasticSearchQuery.getType());
                elasticSearchQuery.buildQuery();
                searchRequest.source(elasticSearchQuery.getQueryBuilder());
                final SearchResponse search = client.search(searchRequest, HEADERS);
                NoSQLResponse response = new ElasticQueryResponseWrapper(search);
                return response;
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
            //All is committed ...
        } catch (Exception e) {
            log.error("[void : (" + coreName + ")commit]: IOException: " + e.getMessage(), e);
        }
    }

    @Override
    public void optimize() {
    }

    @Override
    public void delete(String id) {
        try {
            final DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest.id(id);
            client.delete(deleteRequest, HEADERS);
        } catch (Exception e) {
            log.error("[ void: (" + coreName + ")deleteByID(" + id + ") ]: IOException deleteByID index: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            final DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest.type(coreName);
            client.delete(deleteRequest, HEADERS);
        } catch (Exception e) {
            log.error("[ void: (" + coreName + ")deleteAll index: " + e.getMessage(), e);
        }
    }

    public static class ElasticInputDocumentWrapper implements NoSQLInputDocument {

        final Class<? extends ModelObjectInterface> clazz;
        final ObjectMapper objectMapper;
        final ObjectNode node;
        
        public ElasticInputDocumentWrapper(Class<? extends ModelObjectInterface> clazz){
            this.clazz = clazz;
            objectMapper = new ObjectMapper();
            node = objectMapper.createObjectNode();

//            node.put("objectID", "my-id");
//            objectMapper.writeValueAsString(node);
//            System.out.println("id = "+idNode.asInt());

        }



        @Override
        public void addField(String varName, String objectID) {
            node.put(varName, objectID);
        }

        @Override
        public void addField(String varName, Long value) {
            node.put(varName, value);
        }

        @Override
        public void addField(String varName, Integer value) {
            node.put(varName, value);
        }

        @Override
        public void addField(String varName, Boolean value) {
            node.put(varName, value);
        }

        @Override
        public void addField(String varName, Float value) {
            node.put(varName, value);
        }

        @Override
        public void addField(String varName, Double value) {
            node.put(varName, value);
        }

        @Override
        public void addField(String varName, Calendar value) {
            SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2011-11-28T18:30:30Z
            xmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            node.put(varName, xmlDateFormat.format(((Calendar) value).getTime()));
        }

        @Override
        public void addField(String varName, List values) {
//TODO
            //            objectMapper = new ObjectMapper();
//            node = objectMapper.createObjectNode();
//            node.set(varName, values);
        }

//        @Override
//        public void addField(String varName, Enum value) {
//            node.put(varName, "" + value);
//        }


    }


    @Override
    public NoSQLInputDocument createInputDocument(Class<? extends ModelObjectInterface> clazz) {

        return null;
    }




    @Override
    public <T extends ModelObjectInterface> NQL.SearchQuery createSearchQuery(Class<T> clazz) {
        return null;
    }

    @Override
    public void empty() {
        try {
            log.debug("[void : (" + coreName + ")empty]:EMPTY : ");
            deleteAll();
        } catch (Exception e) {
            log.error("[ void: (" + coreName + ")empty ]: IOException empty index: " + e.getMessage(), e);
        }
    }

    @Override
    public void destroy() throws IOException {
        client.close();
    }




}

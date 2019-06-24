package dk.lessismore.nojpa.reflection.db.model.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.reflection.attributes.Attribute;
import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLInputDocument;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    public ElasticServiceImpl(String host, int port, Class<? extends ModelObjectInterface> clazz) {
        log.debug("[: constructor]:HIT: host("+ host +":"+ port +") for " + clazz.getSimpleName());
        this.host = host;
        this.port = port;
        this.clazz = clazz;
        this.coreName = clazz.getSimpleName();
        startup();
    }


    public String getName() {
        return clazz.getSimpleName();
    }


    @Override
    public void index(NoSQLInputDocument noSQLInputDocument) {
        index(((ElasticInputDocumentWrapper) noSQLInputDocument));
    }


    public void startup() {
        log.debug("[void : (" + clazz.getSimpleName() + ")startup]:HIT: " + this);
        try {
            if (client == null) {
                client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
            }

        } catch (Exception e) {
            log.error("[ void: (" + clazz.getSimpleName() + ")startup ]: exception constructing server: " + e.getMessage(), e);
        }
    }

    static int errors = 0;
    static long totalTime = 0;
    static long counter = 0;

    public void index(ElasticInputDocumentWrapper inputDocument){
        if (inputDocument == null) {
            log.error("index()::solrInputDocument is null");
            return;
        }
        try {
            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(clazz);
            Attribute searchRouteAttribute = dbAttributeContainer.getAttributeContainer().getSearchRouteAnnotationAttribute();
            String indexName = getIndexName(inputDocument);

            IndexRequest request = new IndexRequest(indexName, clazz.getSimpleName().toLowerCase(), inputDocument.node.get("objectID").textValue());

            if(searchRouteAttribute != null){
                String routing = "" + dbAttributeContainer.getAttributeContainer().getAttributeValue(inputDocument.getModelObjectInterface(), searchRouteAttribute.getAttributeName());
                log.debug("Setting ROUTING = " + routing);
                if(routing != null) {
                    request.routing(routing);
                }
            }
            log.debug("Adding to index["+ indexName +"]: " + inputDocument.json());
            request.source(inputDocument.json(), XContentType.JSON);
            counter++;
            long startTime = System.currentTimeMillis();
            client.index(request, HEADERS);
            long currentTime = System.currentTimeMillis() - startTime;
            totalTime = totalTime + currentTime;
            log.debug("INDEX-TIME: currentTime("+ currentTime +") AVG("+ (totalTime / counter) +") ("+ (currentTime > 1.2 * totalTime / counter ? "SLOW,SLOW,SLOW" : "OK" ) +").... ");

        } catch (Exception e) {
            log.error("[void : (" + coreName + ")index]: IOException: " + e.getMessage(), e);
        }
    }

    private String getIndexName(ElasticInputDocumentWrapper inputDocument){
        Calendar creationDate = inputDocument.getModelObjectInterface().getCreationDate();
        return ("NoJPA2-" + inputDocument.getModelObjectInterface().getInterface().getSimpleName() + "-" + creationDate.get(Calendar.YEAR) + "-" + (1+creationDate.get(Calendar.MONTH))).toLowerCase();
    }

    private String[] getIndexName(ElasticSearchQuery query){
        ArrayList<String> toReturn = new ArrayList<>();
        String prefix = "NoJPA2-" + query.getType() + "-";
        String[] indexs = query.getIndexs();
        if(false && indexs != null){ //TODO: Remove false later, when index are workings ...
            for(int i = 0; i < indexs.length; i++){
                toReturn.add((prefix + indexs[i]).toLowerCase());
            }
        } else {
            toReturn.add(prefix.toLowerCase() + "*");
        }
        //For debug...
        for(int i = 0; i < toReturn.size(); i++){
            log.debug("We will send query to index["+ i +"]: " + toReturn.get(i));
        }
//        return new String[] {"funny87"};
        return toReturn.toArray(new String[toReturn.size()]);
    }




    @Override
    public NoSQLResponse query(NQL.SearchQuery query) {
        try {
            if(client != null){
                ElasticSearchQuery elasticSearchQuery = (ElasticSearchQuery) query;
                String[] indexName = getIndexName(elasticSearchQuery);
                return query(query, indexName);
            } else {
                Exception exp = new RuntimeException("We don't have a client...!!!! ");
                log.error("Error: " , exp);
                throw exp;
            }
        } catch (Exception e) {
            try {
                if (e.toString().contains("index_not_found_exception")) {
                    ElasticSearchQuery elasticSearchQuery = (ElasticSearchQuery) query;
                    return query(query, new String[]{("nojpa2-" + elasticSearchQuery.getType()).toLowerCase() + "-*"});
                } else {
                    log.error("[QueryResponse : (" + coreName + ")query]: ElasticException: " + e.getMessage(), e);
                }
            } catch (Exception e2){
                log.error("[QueryResponse : (" + coreName + ")query]: ElasticException: " + e.getMessage(), e2);
            }


        }
        return null;
    }

    public NoSQLResponse query(NQL.SearchQuery query, String[] indexs) throws Exception {
        ElasticSearchQuery elasticSearchQuery = (ElasticSearchQuery) query;
        SearchRequest searchRequest = new SearchRequest(indexs);
        searchRequest.types(elasticSearchQuery.getType().toLowerCase());
        if(elasticSearchQuery.getRouting() != null){
            searchRequest.routing(elasticSearchQuery.getRouting());
        }
        SearchSourceBuilder queryBuilder = elasticSearchQuery.getQueryBuilder();
        if(query.getAggregationBuilder() != null){
            queryBuilder.aggregation(query.getAggregationBuilder());
        }
        searchRequest.source(queryBuilder);
        final SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        NoSQLResponse response = new ElasticQueryResponseWrapper(search);
        return response;
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
        // TODO implement
//        try {
//            BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client.)
//                    .filter(QueryBuilders.matchQuery("gender", "male"))
//                    .source("persons")
//                    .get();
//            long deleted = response.getDeleted();        } catch (Exception e) {
//            log.error("[ void: (" + coreName + ")deleteAll index: " + e.getMessage(), e);
//        }
    }

    public static class ElasticInputDocumentWrapper implements NoSQLInputDocument {

        final Class<? extends ModelObjectInterface> clazz;
        final ObjectMapper objectMapper;
        final ObjectNode node;
        final ModelObjectInterface modelObjectInterface;
        
        public ElasticInputDocumentWrapper(Class<? extends ModelObjectInterface> clazz, ModelObjectInterface modelObjectInterface){
            this.clazz = clazz;
            this.modelObjectInterface = modelObjectInterface;
            objectMapper = new ObjectMapper();
            node = objectMapper.createObjectNode();

//            node.put("objectID", "my-id");
//            objectMapper.writeValueAsString(node);
//            System.out.println("id = "+idNode.asInt());

        }


        @Override
        public void addShard(String shard) {

        }

        @Override
        public String getShard() {
            return null;
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
            if(value != null) {
                SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2011-11-28T18:30:30Z
                xmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                node.put(varName, xmlDateFormat.format(((Calendar) value).getTime()));
            }
        }

        @Override
        public void addField(String varName, List values) {
//TODO
            //            objectMapper = new ObjectMapper();
//            node = objectMapper.createObjectNode();
//            node.set(varName, values);
        }

        @Override
        public void addPostfixShardName(String postfixShardName) {

        }

        @Override
        public String getPostfixShardName() {
            return null;
        }

//        @Override
//        public void addField(String varName, Enum value) {
//            node.put(varName, "" + value);
//        }


        public String json(){
            return node.toString();
        }

        public ModelObjectInterface getModelObjectInterface() {
            return modelObjectInterface;
        }
    }


    @Override
    public NoSQLInputDocument createInputDocument(Class<? extends ModelObjectInterface> clazz, ModelObjectInterface modelObjectInterface) {

        return new ElasticInputDocumentWrapper(clazz, modelObjectInterface);
    }




    @Override
    public <T extends ModelObjectInterface> NQL.SearchQuery createSearchQuery(Class<T> clazz) {
        return new ElasticSearchQuery(clazz);
    }


    @Override
    public void destroy() throws IOException {
        client.close();
    }




}

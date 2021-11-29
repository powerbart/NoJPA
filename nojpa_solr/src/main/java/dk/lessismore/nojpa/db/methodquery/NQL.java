package dk.lessismore.nojpa.db.methodquery;

import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLResponse;
import dk.lessismore.nojpa.reflection.db.model.nosql.NoSQLService;
import dk.lessismore.nojpa.utils.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
* Created : with IntelliJ IDEA.
* User: seb
*/
public class NQL {

    private static final Logger log = LoggerFactory.getLogger(NQL.class);

    public static boolean DEBUG_EXPLAIN = false;


    public enum Comp {EQUAL, EQUAL_OR_GREATER, EQUAL_OR_LESS, NOT_EQUAL, LIKE}
    public enum Order {ASC, DESC}
    public static final int ANY = 0;
    public enum NoSQLOperator {
        OR(0), AND(1);
        final int operator;
        NoSQLOperator(int i){
            this.operator = i;
        }

        public String toDebugString(){
            return this.name();
        }

        public static String name(Integer condition) {
            if(condition == 0){
                return OR.name();
            } else {
                return AND.name();
            }
        }
    };

    private static Hashtable<Thread, LinkedList<Pair<Object, Method>>> threadMockCallSequenceMap =
            new Hashtable<Thread, LinkedList<Pair<Object, Method>>>();

    /**
     * Create a select query object.
     * @param sourceMock A mock object used to determine the source model.
     * @param <T> The type of the entities fetched.
     * @return self.
     */
    public static <T extends ModelObjectInterface> SearchQuery<T> search(T sourceMock) {
        clearMockCallSequence();
        final Class<T> sourceClass = (Class<T>) ((MockExtra) sourceMock).mockExtra_getSourceClass();
        NoSQLService noSQLService = ModelObjectSearchService.noSQLService(sourceClass);
        SearchQuery searchQuery = noSQLService.createSearchQuery(sourceClass);
        return searchQuery;
    }
    public static <T extends ModelObjectInterface> SearchQuery<T> search(Class<T> sourceClass) {
        clearMockCallSequence();
        NoSQLService noSQLService = ModelObjectSearchService.noSQLService(sourceClass);
        SearchQuery searchQuery = noSQLService.createSearchQuery(sourceClass);
        return searchQuery;
    }

//    public static String asString(Object mockValue){
//        Pair<Class, String> pair = getSourceAttributePair();
//        clearMockCallSequence();
//        String attribute = makeAttributeIdentifier(pair);
//        attribute = attribute.replaceAll("_", "");
//        return attribute;
//    }


    /**
     * Create a mock object.
     * @param modelInterface Interface class for which the mock should represent.
     * @param <I> ModelClass type
     * @return A mock object.
     */
    @SuppressWarnings("unchecked")
    public static <I extends ModelObjectInterface> I mock(Class<I> modelInterface) {
        return (I) Proxy.newProxyInstance(
                modelInterface.getClassLoader(),
                new Class[]{modelInterface, MockExtra.class},
                new MockInvocationHandler(modelInterface));
    }

    /**
     * A select query on which you can put constraints and fetch results.
     * @param <T> The model source class
     */
    public static abstract class SearchQuery<T extends ModelObjectInterface> {

        protected final Class<T> selectClass;
        protected ArrayList<Constraint> rootConstraints;
        protected ArrayList<Constraint> filterConstraints;
//        protected NoSQLQuery noSqlQuery;
        protected NoSQLService noSQLService;
        protected String orderByAttribute;
        protected Order orderByORDER = Order.ASC;
        protected int startLimit = -1;
        protected int endLimit = -1;
        protected String preBoost = null;
        protected boolean addStats = false;
        protected boolean enableHighlighting = false;
        protected boolean addFacets = false;
        protected boolean addDateFacets = false;
        protected int facetLimit = 10;
        protected List<String> statsAttributeIdentifier = new ArrayList<>();
        protected List<String> facetAttributeIdentifier = new ArrayList<>();
        protected List<DateRangeFacet> dateFacetAttributeIdentifier = new ArrayList<>();



        public SearchQuery(Class<T> selectClass) {
            this.selectClass = selectClass;
            NoSQLService noSQLService = ModelObjectSearchService.noSQLService(selectClass);
//            noSqlQuery = noSQLService.createQuery(selectClass);
            rootConstraints = new ArrayList<Constraint>();
            filterConstraints = new ArrayList<Constraint>();
        }



        public SearchQuery<T> addFunction(NoSQLFunction solrMathFunction) {
            NoSQLExpression expression = rootConstraints.get(rootConstraints.size() - 1).getExpression();
            if(expression instanceof NoSQLContainerExpression){
                NoSQLContainerExpression noSQLContainerExpression = (NoSQLContainerExpression) expression;
                noSQLContainerExpression.getExpressions().get(noSQLContainerExpression.getExpressions().size() - 1).addFunction(solrMathFunction);
            } else {
                expression.addFunction(solrMathFunction);
            }
            return this;
        }


        public SearchQuery<T> search(int mockValue, Comp comp, int value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SearchQuery<T> search(boolean mockValue, Comp comp, boolean value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }


        public SearchQuery<T> search(String mockValue, Comp comp, String value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SearchQuery<T> search(Enum mockValue, Comp comp, Enum value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SearchQuery<T> filter(Enum mockValue, Comp comp, Enum value) {
            filterConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SearchQuery<T> search(Enum mockValue, Comp comp, String value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SearchQuery<T> search(Calendar mockValue, Comp comp, Calendar value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SearchQuery<T> search(double mockValue, Comp comp, double value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }
        public SearchQuery<T> search(long mockValue, Comp comp, long value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }
        public SearchQuery<T> search(long mockValue, Comp comp, int value) {
            rootConstraints.add(has(mockValue, comp, new Long(value).longValue()));
            return this;
        }
        public SearchQuery<T> search(double mockValue, Comp comp, float value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }
        public <M extends ModelObjectInterface> SearchQuery<T> search(M mockValue, Comp comp, M model) {
            rootConstraints.add(has(mockValue, comp, model));
            return this;
        }

        public <M extends ModelObjectInterface> SearchQuery<T> searchIsNull(M mockValue) {
            rootConstraints.add(hasNull(mockValue));
            return this;
        }

        public <M extends ModelObjectInterface> SearchQuery<T> searchIsNull(String mockValue) {
            rootConstraints.add(hasNull(mockValue));
            return this;
        }

        public <M extends ModelObjectInterface> SearchQuery<T> searchNotNull(M mockValue) {
            rootConstraints.add(hasNotNull(mockValue));
            return this;
        }

        public SearchQuery<T> search(Constraint constraint) {
            rootConstraints.add(constraint);
            return this;
        }

        public SearchQuery<T> filter(Constraint constraint) {
            filterConstraints.add(constraint);
            return this;
        }

//        public <M extends ModelObjectInterface> SearchQuery<T> scoreMin(float lower) {
//            noSqlQuery.setFilterQueries("{!frange l=" + lower + "}query($q)");
//            return this;
//        }
//
//        public <M extends ModelObjectInterface> SearchQuery<T> scoreMax(float upper) {
//            noSqlQuery.setFilterQueries("{!frange u=" + upper + "}query($q)");
//            return this;
//        }
//
//        public <M extends ModelObjectInterface> SearchQuery<T> scoreWithin(float lower, float upper) {
//            noSqlQuery.setFilterQueries("{!frange l=" + lower + " " + "u=" + upper + "}query($q)");
//            return this;
//        }
//

//        /**
//         * Set order by
//         * @param mockValue this value is ignored, but actual parameter is expected to be a mock call
//         * @param order ascending, descending.
//         * @return this.
//         */
        public SearchQuery<T> orderBy(int mockValue, Order order) {
            return orderBy(order);
        }
        public SearchQuery<T> orderBy(String mockValue, Order order) {
            return orderBy(order);
        }
        public SearchQuery<T> orderBy(Calendar mockValue, Order order) {
            return orderBy(order);
        }
        public SearchQuery<T> orderBy(double mockValue, Order order) {
            return orderBy(order);
        }
        public SearchQuery<T> orderBy(T mockValue, Order order) {
            return orderBy(order);
        }
        private SearchQuery<T> orderBy(Order order) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            String solrName = makeAttributeIdentifier(pair);
            String sortByAttributeName = createFinalSolrAttributeName(joints, solrName);
            this.orderByAttribute = sortByAttributeName;
            this.orderByORDER = order;
            clearMockCallSequence();
//            String tableName = getTableName(pair.getFirst());
//            String attributeName = pair.getSecond();
//            for (Pair<Class, String> joint: joints) {
//                joinOn(joint.getFirst(), joint.getSecond());
//            }
//            statement.setOrderBy(tableName, attributeName, orderToNum(order));

            return this;
        }

        /**
         * Set limit
         * @param start start
         * @param end end
         * @return this
         */
        public SearchQuery<T> limit(int start, int end) {
            this.startLimit = start;
            this.endLimit = end;
            return this;
        }
        public SearchQuery<T> limit(int count) {
            return limit(0, count);
        }


        public SearchQuery<T> preBoost(String preBoost) {
            this.preBoost = preBoost;
            return this;
        }


        /**
         * Execute query
         * @return result list, possible empty, never null.
         */
        public NList<T> getList() {
            NList<T> list = selectObjectsFromDb();
            return list;
        }

        public NList<T> getList(float scoreAbove) {
            NList<T> list = selectObjectsFromDb(scoreAbove, null);
            return list;
        }

        public NList<T> getList(String postShardName) {
            NList<T> list = selectObjectsFromDb(-1, postShardName);
            return list;
        }

        public NList<T> getList(float scoreAbove, String postShardName) {
            NList<T> list = selectObjectsFromDb(scoreAbove, postShardName);
            return list;
        }

        public T getFirst() {
            NList<T> list = limit(1).getList();
            if (list.size() > 0) {
                return list.get(0);
            }
            return null;
        }

        public long getCount() {
            return limit(1).getList().getNumberFound();
        }





        public <N extends Number> SearchQuery<T> addStats(N variable){
            addStats = true;
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            String shortName = makeAttributeIdentifier(pair);
            String attributeIdentifier = createFinalSolrAttributeName(joints, shortName);
            statsAttributeIdentifier.add(attributeIdentifier);
            NQL.clearMockCallSequence();
            return this;
        }

        public SearchQuery<T> enableHighlighting() {
            enableHighlighting = true;
            return this;
        }

        public SearchQuery<T> addFacet(Object variable, int facetLimit){
            addFacets = true;
            this.facetLimit = facetLimit;
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            String shortName = makeAttributeIdentifier(pair);
            String attributeIdentifier = createFinalSolrAttributeName(joints, shortName);
            facetAttributeIdentifier.add(attributeIdentifier);
            NQL.clearMockCallSequence();
            return this;
        }


        public static class DateRangeFacet {

            public enum STATS { AVG, SUM, MEAN, MIN, MAX};

            String variable;
            String start;
            String end;
            String gap;
            STATS[] stats;

            public DateRangeFacet(String variable, String start, String end, String gap, STATS ... stats) {
                this.variable = variable;
                this.start = start;
                this.end = end;
                this.gap = gap;
                this.stats = stats;
            }

            public String getVariable() {
                return variable;
            }

            public String getStart() {
                return start;
            }

            public String getEnd() {
                return end;
            }

            public String getGap() {
                return gap;
            }

            public STATS[] getStats() {
                return stats;
            }
        }


        public SearchQuery<T> getDateRangeFacet(Object variable, String start, String end, String gap, DateRangeFacet.STATS... stats){
            addDateFacets = true;
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            String shortName = makeAttributeIdentifier(pair);
            String attributeIdentifier = createFinalSolrAttributeName(joints, shortName);
            dateFacetAttributeIdentifier.add(new DateRangeFacet(attributeIdentifier, start, end, gap, stats));
            NQL.clearMockCallSequence();
            return this;
        }









//        public <N> List<Pair<String, Long>> getCloud(N variable, int limit) {
//            List<Pair<String, Long>> toReturn = new ArrayList<Pair<String, Long>>();
//            try {
//
//                List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
//                Pair<Class, String> pair = getSourceAttributePair();
//                String attributeIdentifier = makeAttributeIdentifier(pair);
//                clearMockCallSequence();
//
//
//                buildQuery();
//                NoSQLService noSQLService = ModelObjectSearchService.noSQLService(selectClass);
//
//                noSqlQuery.setFacet(true);
//                noSqlQuery.setFacetLimit(limit);
//                noSqlQuery.addFacetField(attributeIdentifier);
//
//                QueryResponse response = noSQLService.query(noSqlQuery);
//                List<FacetField.Count> facets = response.getFacetFields().get(0).getValues();
//                for (FacetField.Count facet : facets) {
//                    toReturn.add(new Pair<>(facet.getName(), facet.getCount()));
//                }
//
//            } catch (Exception e){
//                log.error("Some error in getMax() : " + e, e);
//                throw new RuntimeException("getMax - error", e);
//            }
//
//            return toReturn;
//        }




//        public <N extends Number> NStats<N> getStats(N variable) {
//            NStats<N> nStats = new NStats<N>();
//            try {
//
//                List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
//                Pair<Class, String> pair = getSourceAttributePair();
//                String attributeIdentifier = makeAttributeIdentifier(pair);
//                clearMockCallSequence();
//
//
//                buildQuery();
//                SolrService solrServer = ModelObjectSearchService.noSQLService(selectClass);
//
//                noSqlQuery.setGetFieldStatistics(true);
//                noSqlQuery.setParam("stats.field", attributeIdentifier);
//                noSqlQuery.setParam("stats.facet", attributeIdentifier);
//
//                QueryResponse queryResponse = solrServer.query(noSqlQuery);
//
//                Map<String, FieldStatsInfo> fieldStatsInfo = queryResponse.getFieldStatsInfo();
//
//                FieldStatsInfo sInfo = fieldStatsInfo.get(attributeIdentifier);
//                if(sInfo == null){
//                    return nStats;
//                }
//                nStats.min = (Double) sInfo.getMin();
//                nStats.max = (Double) sInfo.getMax();
//                nStats.sum = (Double) sInfo.getSum();
//                nStats.count = sInfo.getCount();
//                nStats.mean = (Double) sInfo.getMean();
//                nStats.stddev = (Double) sInfo.getStddev();
//            } catch (Exception e){
//                log.error("Some error in getMax() : " + e, e);
//                throw new RuntimeException("getMax - error", e);
//            }
//
//            return nStats;
//        }




        private String cleanTerm(String input){
            StringTokenizer toks = new StringTokenizer(input, " <>'@\"/");
            StringBuilder toReturn = new StringBuilder();
            while(toks.hasMoreTokens()){
                String s = toks.nextToken();
                boolean ignore = s.length() < 1;
                if(!ignore){
                    toReturn.append(s); toReturn.append(' ');
                }
            }
            return toReturn.toString();

        }


//
//        public List<String> terms(String userInput) throws SolrServerException {
//            SolrQuery query = new SolrQuery();
//            query.setParam(CommonParams.QT, "/terms");
//            query.setParam(TermsParams.TERMS, true);
//            query.setParam(TermsParams.TERMS_LIMIT, "30");
//            query.setParam(TermsParams.TERMS_FIELD, "_Lot_translations__ID_Translations_title_da__TXT", "_Lot_translations__ID_Translations_description_da__TXT");  //"description_da", "title_da"
//            query.setParam(TermsParams.TERMS_PREFIX_STR, userInput.trim()); //brudekjole    jacobsen
//
//            SolrServer solrServer = ModelObjectSearchService.solrServer(selectClass);
//            QueryResponse resp = solrServer.query(query);
//
//            Map<String,List<TermsResponse.Term>> termMap = resp.getTermsResponse().getTermMap();
//
////    List<TermsResponse.Term> title_da = termMap.get("title_da");
////    for(int j = 0; title_da != null && j < title_da.size(); j++){
////        TermsResponse.Term term = title_da.get(j);
////        System.err.println("title_da: " + term.getTerm());
////        jsonArray.add(term.getTerm());
////    }
////
////    List<TermsResponse.Term> description_da = termMap.get("description_da");
////    for(int j = 0; description_da != null && j < description_da.size(); j++){
////        TermsResponse.Term term = description_da.get(j);
////        System.err.println("description_da: " + term.getTerm());
////        jsonArray.add(term.getTerm());
////    }
//
//            HashMap<String, Long> fMap = new HashMap<String, Long>();
//
//            List<TermsResponse.Term> title_shingles_da = termMap.get("title_shingles_da");
//            for(int j = 0; title_shingles_da != null && j < title_shingles_da.size(); j++){
//                TermsResponse.Term term = title_shingles_da.get(j);
//
//                String cleanTerm = cleanTerm(term.getTerm());
//                if(fMap.containsKey(cleanTerm)){
//                    fMap.put(cleanTerm, fMap.get(cleanTerm) + term.getFrequency());
//                    System.err.println("RE-add of " + cleanTerm + " / " + term.getTerm());
//                } else {
//                    System.err.println("add of " + cleanTerm + " / " + term.getTerm());
//                    fMap.put(cleanTerm, term.getFrequency());
//                }
//                //        jsonArray.add(term.getTerm() + " :: " + term.getFrequency());
//            }
//
//            List<TermsResponse.Term> description_shingles_da = termMap.get("description_shingles_da");
//            for(int j = 0; description_shingles_da != null && j < description_shingles_da.size(); j++){
//                TermsResponse.Term term = description_shingles_da.get(j);
//                String cleanTerm = cleanTerm(term.getTerm());
//                if(fMap.containsKey(cleanTerm)){
//                    fMap.put(cleanTerm, fMap.get(cleanTerm) + term.getFrequency());
//                    System.err.println("RE-add of " + cleanTerm + " / " + term.getTerm());
//                } else {
//                    System.err.println("add of " + cleanTerm + " / " + term.getTerm());
//                    fMap.put(cleanTerm, term.getFrequency());
//                }
//            }
//
//            String[] ss = fMap.keySet().toArray(new String[fMap.size()]);
//
//            Arrays.sort(ss);
//            return Arrays.asList(ss);
//        }



//        public List<String> suggest(String userInput){
//            ArrayList<String> tags = new ArrayList<String>();
//            try {
//                SolrQuery query = new SolrQuery();
//                query.setRequestHandler("/suggest");
//                query.setQuery(ClientUtils.escapeQueryChars(userInput));
//                SolrService solrServer = ModelObjectSearchService.noSQLService(selectClass);
//                QueryResponse response = solrServer.query(query);
//
////                NamedList<Object> namedList = response.getResponse();
////                for(Iterator<Map.Entry<String, Object>> iterator = namedList.iterator(); iterator.hasNext(); ){
////                    Map.Entry<String, Object> kv = iterator.next();
////                    log.debug("NamedList::: k("+ kv.getKey() +") -> " + );
////                }
//
//
//                Object solrSuggester = ((HashMap) response.getResponse().get("suggest")).get("default");
//                SimpleOrderedMap suggestionsList = ((SimpleOrderedMap<SimpleOrderedMap>)solrSuggester).getVal(0);
//                ArrayList<SimpleOrderedMap> suggestions = (ArrayList<SimpleOrderedMap>)suggestionsList.get("suggestions");
//
//                for(SimpleOrderedMap suggestion : suggestions) {
//                    tags.add((String)suggestion.get("term"));
//                }
//
//            } catch (Exception e) {
//                log.error("Some error when running suggest: " + e, e);
//            }
//            return tags;
//        }




//        public void visit(DbObjectVisitor visitor){
//            log.debug("Will run: DbObjectSelector.iterateObjectsFromDb(selectClass, statement, visitor)");
//            statement.addExpression(getExpressionAddJoins());
//            DbObjectSelector.iterateObjectsFromDb(selectClass, statement, visitor);
//        }
//


        protected abstract void buildQuery();
        protected abstract String toStringDebugQuery();


        AggregationBuilder aggregation = null;

        public void setAggregation(AggregationBuilder aggregation){
            this.aggregation = aggregation;
        }

        public AggregationBuilder getAggregationBuilder() {
            return aggregation;
        }

        @SuppressWarnings("unchecked")
        private NList<T> selectObjectsFromDb() {
            return selectObjectsFromDb(-1, null);
        }

        private NList<T> selectObjectsFromDb(float scoreAbove, String postShardName) {
//            TimerWithPrinter timer = new TimerWithPrinter("selectObjectsFromDb", "/tmp/luuux-timer-getPosts.log");
//            log.debug("DEBUG-TRACE", new Exception("DEBUG"));
            List<T> toReturn = new ArrayList<T>();
            List<Float> scoresList = new ArrayList<Float>();
            try {

                buildQuery();

//                timer.markLap("1");
                NoSQLService noSQLService = ModelObjectSearchService.noSQLService(selectClass);
//                timer.markLap("2");
//                if(!DEBUG_EXPLAIN){
//                    noSqlQuery.setFields("objectID");
//                } else {
//                    noSqlQuery.setFields("*, score, _explain_");
//                    noSqlQuery.setParam("debug", true);
//                    log.debug("***************** DEBUG = TRUE *************************");
//                    log.debug("***************** DEBUG = TRUE *************************");
//                    log.debug("***************** DEBUG = TRUE *************************");
//                    log.debug("***************** DEBUG = TRUE *************************");
//                }
//                noSqlQuery.setParam("bf", "sum(_Post_pageViewCounter__ID_Counter_count__LONG,8)");
                long start = System.currentTimeMillis();
                NoSQLResponse queryResponse = noSQLService.query(this, postShardName);
                log.info("[{}ms size: {}] Will query: {}", System.currentTimeMillis() - start, queryResponse.getNumFound(), toStringDebugQuery());
//                log.info("[{}ms size: {}] Will solr query: {}", System.currentTimeMillis() - start, queryResponse.getResults().getNumFound(), toStringDebugQuery());
//                timer.markLap("3");
//                log.debug("queryResponse = " + queryResponse.getResults().size());
//                log.debug("queryResponse = " + queryResponse.getResults().getNumFound());
//                log.debug("queryResponse = " + queryResponse.getResults().);
//                int size = queryResponse.getResults().size();
                int size = queryResponse.size();
//                timer.markLap("4");
                boolean loadObject = true;
                int loadedObjects = this.startLimit;
                for(int i = 0; i < size; i++){
//                    SolrDocument entries = queryResponse.getResults().get(i);
//                    String objectID = entries.get("objectID").toString();
//                    SolrDocument entries = queryResponse.getResults().get(i);
                    String objectID = queryResponse.getID(i);

                    if(DEBUG_EXPLAIN && queryResponse.getRaw(i) instanceof SolrDocument) {
                        SolrDocument entries = (SolrDocument) queryResponse.getRaw(i);
                        if (i == 0) {
                            Iterator<String> iterator = entries.getFieldNames().iterator();
                            for (; iterator.hasNext(); ) {
                                String next = iterator.next();
                                log.debug("Fieldnames:" + next);
                            }
                        }

                        if (entries.containsKey("score")) {
                            float score = new Float("" + entries.get("score"));
                            log.debug("objectID(" + objectID + ") has score(" + score + ")");
                            if(loadedObjects > 6 && (scoreAbove > 0 && score < scoreAbove)){
                                loadObject = false;
                            } else {
                                scoresList.add(score);
                            }
                        }
                        if (entries.containsKey("_explain_")) {
                            log.debug("_explain_ :: (" + entries.get("_explain_") + ")");
                        }
                    }

                    if(loadObject) {
                        loadedObjects++;
                        T t = MQL.selectByID(selectClass, objectID);
                        if (t == null) {
                            log.error("We have a problem with the sync between the DB & Solr ... Can't find objectID(" + objectID + ") class(" + selectClass + ")", new Exception("Sync problem"));
                        } else {
                            toReturn.add(t);
                        }
                    }
                }
//                timer.markLap("5");
                log.debug("Returns the size of Nlist.size() -> " + toReturn.size() + " .... size("+ size +")");
                return (NList<T>) Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class[]{NList.class},
                        new NListImpl(queryResponse, toReturn, scoresList, postShardName));

            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return null;
        }

        public SearchQuery<T> search(String query) {
            rootConstraints.add(has(query));
            return this;
        }


//        private int selectCountFromDb() {
//            statement.addExpression(getExpressionAddJoins());
//            return DbObjectSelector.countObjectsFromDb(statement); // The cache arguments is ignored
//        }
    }


    public static <T extends ModelObjectInterface> NList<T> reInit(NList<T> prev, List<T> newContent){
        NList<T> ts = (NList<T>) Proxy.newProxyInstance(
                prev.getClass().getClassLoader(),
                new Class[]{NList.class},
                new NListImpl(((NListImpl) prev.getImpl()).queryResponse, newContent, ((NListImpl) prev.getImpl()).scoreList, ((NListImpl) prev.getImpl()).postShardName));
        log.debug("reInit-ts : " + ts.size());
        return ts;
    }


    private static class NListImpl implements InvocationHandler {

        private final NoSQLResponse queryResponse;
        private final List resultList;
        private final List<Float> scoreList;
        private final String postShardName;

        public NListImpl(NoSQLResponse queryResponse, List resultList, List<Float> scoreList, String postShardName) {
            this.queryResponse = queryResponse;
            this.resultList = resultList;
            this.scoreList = scoreList;
            this.postShardName = postShardName;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("getNumberFound")) {
                return queryResponse.getNumFound();
            } else if (methodName.equals("getQTime")) {
                return queryResponse.getQTime();
            } else if (methodName.equals("getQTimeIncludingNetwork")) {
                return queryResponse.getQTimeIncludingNetwork();
            } else if (methodName.equals("getHighlighting")) {
                return queryResponse.getHighlighting();
            } else if (methodName.equals("getPostShardName")) {
                return postShardName;
            } else if (methodName.equals("getImpl")) {
                return this;
            } else if (methodName.equals("getScore")) {
                int i = (int) args[0];
                if(scoreList != null && scoreList.size() > i){
                    return scoreList.get(i);
                } else {
                    return null;
                }
            } else if (methodName.equals("getStats")) {
                List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
                Pair<Class, String> pair = getSourceAttributePair();
                String attributeIdentifier = makeAttributeIdentifier(pair);
                NQL.clearMockCallSequence();
                return queryResponse.getStats(attributeIdentifier);
            } else if (methodName.equals("getFacet")) {
                List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
                Pair<Class, String> pair = getSourceAttributePair();
                String solrName = makeAttributeIdentifier(pair);
                String attributeIdentifier = createFinalSolrAttributeName(joints, solrName);
                NQL.clearMockCallSequence();
                return queryResponse.getFacet(attributeIdentifier);
            } else if (methodName.equals("getDateRangeFacet")) {
                List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
                Pair<Class, String> pair = getSourceAttributePair();
                String solrName = makeAttributeIdentifier(pair);
                String attributeIdentifier = createFinalSolrAttributeName(joints, solrName);
                NQL.clearMockCallSequence();
                return queryResponse.getDateRangeFacet();
            } else if (methodName.equals("getAggregations")) {
                return queryResponse.getAggregations();
            }
            return method.invoke(resultList, args);
        }
    }




    private interface MockExtra {
        public <C extends ModelObjectInterface> Class<C> mockExtra_getSourceClass();
    }

    private static class MockInvocationHandler implements InvocationHandler {

        private final Class<? extends ModelObjectInterface> sourceClass;

        public MockInvocationHandler(Class<? extends ModelObjectInterface> sourceClass) {
            this.sourceClass = sourceClass;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            log.debug("MockInvocationHandler::Calling " + sourceClass.getSimpleName() + "." + method.getName() + "()");
            String methodName = method.getName();
            if (methodName.equals("mockExtra_getSourceClass") || methodName.equals("getInterface")) {
                return sourceClass;
            }
            if (methodName.equals("equals") && args.length == 1) {
                Object arg = args[0];
                return isMock(arg) && ((MockExtra) proxy).mockExtra_getSourceClass().equals(
                        ((MockExtra) arg).mockExtra_getSourceClass());
            }
            if (methodName.equals("toString")) {
                return "STUPID-MOCK-OF-" + sourceClass.getSimpleName();
            }
            if( ! methodName.startsWith("get")) {
                throw new IllegalArgumentException("Only get-methods may be called on this db instance. " +
                        "Called: " + methodName);
            }
            if( args != null && args.length > 0) {
                String argString = StringUtils.join(args, ", ");
                throw new IllegalArgumentException(String.format("Only no-arg-get-methods may be called " +
                        "on this db instance. Called: %s(%s)", methodName, argString));
            }
            Class returnType = method.getReturnType();
            addToMockCallSequence(proxy, method);
            if (returnType.equals(boolean.class)) {
                return true;
            } else if(returnType.equals(boolean.class)){
                return 0;
            } else if(returnType.equals(int.class)){
                return 0;
            } else if(returnType.equals(float.class)){
                return 0f;
            } else if(returnType.equals(double.class)){
                return 0d;
            } else if(returnType.equals(char.class)){
                return (char) 0;
            } else if(returnType.equals(long.class)){
                return 0L;
            } else if(returnType.isPrimitive()){
                return 0;
            } else if (returnType.isArray() && ModelObjectInterface.class.isAssignableFrom(returnType.getComponentType())) {
                Class compumentType = returnType.getComponentType();
                ModelObjectInterface[] array = (ModelObjectInterface[]) Array.newInstance(compumentType, 1);
                ModelObjectInterface mock = mock(compumentType);
                array[ANY] = mock;
                return array;
            } else if (returnType.isArray() && returnType.getComponentType().isEnum()) {
                Class compumentType = returnType.getComponentType();
                Enum[] array = (Enum[]) Array.newInstance(compumentType, 1);
                return array;
            } else {
                if (ModelObjectInterface.class.isAssignableFrom(returnType)) {
                    ModelObjectInterface mock = mock(returnType);
                    return mock;
                } else {
                    return null;
                }

            }
        }

    }


    /**
     * Find the the model class and attribute name for the last mock call made
     * @return model class and attribute name wrapped in a pair
     */
    private static Pair<Class, String> getSourceAttributePair() {
//        log.debug("NQL.getSourceAttributePair:1");
        LinkedList<Pair<Object, Method>> mockSequence = threadMockCallSequenceMap.get(Thread.currentThread());
        if (mockSequence == null) {
            throw new RuntimeException("Did you mix up MQL and NQL???? ... Or did you call 2 mock-methods in the same statement? .... Some mock calls are expected to have been made at this time Thread.currentThread().getId("+ Thread.currentThread().getId() +")");
        }
        Pair<Object, Method> pair = mockSequence.getLast();
        MockExtra proxy = (MockExtra) pair.getFirst();
        Method method = pair.getSecond();
        Class sourceClass;
        String attributeName;
        if (method.getReturnType().isArray()) {
            Class componentType = method.getReturnType().getComponentType();
            if (ModelObjectInterface.class.isAssignableFrom(componentType)) {
                sourceClass = (Class<ModelObjectInterface>) componentType;
            } else if (componentType.isEnum()) {
                sourceClass = componentType;
            } else {
                throw new RuntimeException(String.format(
                        "Return type of %s is an array but component type is not a model object.",
                        method.getName()));
            }
            attributeName = "objectID";
        } else {
            sourceClass = proxy.mockExtra_getSourceClass();
            attributeName = fieldName(method);
        }
        return new Pair<Class, String>(sourceClass, attributeName);
    }

    private static List<Pair<Class, String>> getJoinsByMockCallSequence() {
//        log.debug("NQL.getJoinsByMockCallSequence:1");
        LinkedList<Pair<Object, Method>> mockSequence = threadMockCallSequenceMap.get(Thread.currentThread());
        List<Pair<Class, String>> joints = new ArrayList<Pair<Class, String>>();
        if (mockSequence == null) {
            log.warn("Did you mix up MQL and NQL???? ... Or did you call 2 mock-methods in the same statement? .... Some mock calls are expected to have been made at this time Thread.currentThread().getId(" + Thread.currentThread().getId() + ")");
            return joints;
        }
//        log.debug("NQL.getJoinsByMockCallSequence:mockSequence:: " + (mockSequence.size() > 0 ? ((mockSequence.size() > 1 ? mockSequence.get(0).getSecond().getName() + "." + mockSequence.get(1).getSecond().getName() : mockSequence.get(0).getSecond().getName())) : null) );

        for (Pair<Object, Method> pair: mockSequence) {
            //log.debug("NQL.getJoinsByMockCallSequence:2");
            if (pair.equals(mockSequence.getLast()) && ! pair.getSecond().getReturnType().isArray()) break;
            Object mock = pair.getFirst();
            Method method = pair.getSecond();
            String field = fieldName(method);
            Class fieldType = method.getReturnType();
            if (fieldType.isArray() || ModelObjectInterface.class.isAssignableFrom(fieldType)) {
                joints.add(new Pair<Class, String>(((MockExtra)mock).mockExtra_getSourceClass(), field));
            }
        }
        return joints;
    }



    public static String makeAttributeIdentifier(Pair<Class, String> pair) {
//        log.debug("makeAttributeIdentifier("+ pair.getFirst() + "," + pair.getSecond() +")");
        return makeAttributeIdentifier(pair.getFirst(), pair.getSecond());
    }

    public static String makeAttributeIdentifier(Class sourceClass, String attributeName) {
//        log.debug("makeAttributeIdentifier("+ sourceClass + "," + attributeName +")");
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
        return dbAttribute.getSolrAttributeName("");
    }


    /*
     * all, any, has expressions (constraints) interface
     */

    public static Constraint has(int mockValue, Comp comp, int value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(pair.getFirst());
        if(dbAttributeContainer.getAttributeContainer().getSearchShardAnnotation() != null){
            if(dbAttributeContainer.getAttributeContainer().getSearchShardAnnotationAttribute().getAttributeName().equals(pair.getSecond())){
                expression.setSharding(true);
            }
        }
        return new NoSQLConstraint(expression, joints);
    }

    public static Constraint has(boolean mockValue, Comp comp, boolean value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value ? 1 : 0);
        return new NoSQLConstraint(expression, joints);
    }

    public static Constraint has(String mockValue, Comp comp, String value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        String attributeName = makeAttributeIdentifier(pair);
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(pair.getFirst());
        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(pair.getSecond());
        DbStrip dbStripAnnotation = dbAttribute.getAttribute().getDbStripAnnotation();
        if(dbStripAnnotation != null && !dbStripAnnotation.stripItHard() && !dbStripAnnotation.stripItSoft()){
            if(value != null && value.length() > 0) {
                value = "\"" + value + "\"";
            }
        } else {
            value = value.startsWith("\"") && (value.contains("\"~") || value.endsWith("\"")) ? value : (attributeName.endsWith("ID") && (dbStripAnnotation != null && !dbStripAnnotation.stripItHard() && dbStripAnnotation.stripItSoft()) ? value : createSearchString(value));
            if(comp == Comp.EQUAL && !value.startsWith("\"") && !value.equals("*")){
                value = "\"" + value + "\"";
            }
        }
        value = (value == null || value.trim().equals("") ? "*" : value);
        NoSQLExpression expression = null;
        if(joints.size() == 0 && pair.getSecond().equals("objectID")){
            expression = newLeafExpression().addConstrain("objectID", comp, value);
        } else {
            expression = newLeafExpression().addConstrain(attributeName, comp, value);
        }
        if(dbAttributeContainer.getAttributeContainer().getSearchShardAnnotation() != null){
            if(dbAttributeContainer.getAttributeContainer().getSearchShardAnnotationAttribute().getAttributeName().equals(pair.getSecond())){
                expression.setSharding(true);
            }
        }
        return new NoSQLConstraint(expression, joints);
    }


    public static Constraint hasWithOr(String mockValue, Comp comp, String value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(pair.getFirst());
        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(pair.getSecond());
        DbStrip dbStripAnnotation = dbAttribute.getAttribute().getDbStripAnnotation();
        if(dbStripAnnotation != null && !dbStripAnnotation.stripItHard() && !dbStripAnnotation.stripItSoft()){
            if(value != null && value.length() > 0) {
                value = "\"" + value + "\"";
            }
        } else {
            value = value.startsWith("\"") && value.endsWith("\"~1") ? value : createSearchString(value);
            if(comp == Comp.EQUAL && !value.startsWith("\"") && !value.equals("*")){
                value = value;
            }
        }
        value = (value == null || value.trim().equals("") ? "*" : value);
        NoSQLExpression expression = null;
        if(joints.size() == 0 && pair.getSecond().equals("objectID")){
            expression = newLeafExpression().addConstrain("objectID", comp, value);
        } else {
            expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        }
        return new NoSQLConstraint(expression, joints);
    }



    public static Constraint has(Enum mockValue, Comp comp, Enum value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new NoSQLConstraint(expression, joints);
    }

    public static Constraint has(Enum mockValue, Comp comp, String value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);

        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(pair.getFirst());
        if(dbAttributeContainer.getAttributeContainer().getSearchShardAnnotation() != null){
            if(dbAttributeContainer.getAttributeContainer().getSearchShardAnnotationAttribute().getAttributeName().equals(pair.getSecond())){
                expression.setSharding(true);
            }
        }


        return new NoSQLConstraint(expression, joints);
    }

    public static Constraint has(Calendar mockValue, Comp comp, Calendar value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);

        if(joints.size() == 0 && pair.getSecond().equals("creationDate")){
            if(comp == Comp.EQUAL){
                expression.setTo(value);
                expression.setFrom(value);
            } else if(comp == Comp.EQUAL_OR_GREATER){
                expression.setFrom(value);
            } else if(comp == Comp.EQUAL_OR_LESS){
                expression.setTo(value);
            }
        }
        return new NoSQLConstraint(expression, joints);
    }

    public static Constraint has(double mockValue, Comp comp, double value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new NoSQLConstraint(expression, joints);
    }

    public static Constraint has(long mockValue, Comp comp, long value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new NoSQLConstraint(expression, joints);
    }


    public static <M extends ModelObjectInterface> Constraint has(M mockValue, Comp comp, M model) {
        return has(mockValue, comp, model, 0);
    }

    public static <M extends ModelObjectInterface> Constraint has(M mockValue, Comp comp, M model, int boost) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();

        String solrName = makeAttributeIdentifier(pair);
        String sortByAttributeName = createFinalSolrAttributeName(joints, solrName);


        clearMockCallSequence();
        NoSQLExpression expression;
        if (model instanceof MockExtra) {
            expression = trueExpression();
        } else {
            expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, model.getObjectID());
            if(joints.size() == 0 && comp == Comp.EQUAL){
                DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(pair.getFirst());
                if(dbAttributeContainer.getAttributeContainer().getSearchRouteAnnotationAttribute() != null){
                    if(dbAttributeContainer.getAttributeContainer().getSearchRouteAnnotationAttribute().getAttributeName().equals(pair.getSecond())){
                        expression.setRouting(true);
                    }
                }
            }
            if(boost > 0){
                log.debug("Adding boost function");
                expression.addFunction(new Boost(boost));
            }
        }
        return new NoSQLConstraint(expression, joints);
    }

    public static Constraint has(String query) {
        NoSQLExpression expression = new NoSQLExpression();
        expression.addConstrain(query);
        return new NoSQLConstraint(expression, new ArrayList<>());
    }

    public static <M extends ModelObjectInterface> Constraint hasNull(M mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().isNull(makeAttributeIdentifier(pair), joints);
        return new NoSQLConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNotNull(M mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().isNotNull(makeAttributeIdentifier(pair), joints);
        return new NoSQLConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNull(Calendar mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().isNull(makeAttributeIdentifier(pair), joints);
        return new NoSQLConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNull(String mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().isNull(makeAttributeIdentifier(pair), joints);
        return new NoSQLConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNotNull(Calendar mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        NoSQLExpression expression = newLeafExpression().isNotNull(makeAttributeIdentifier(pair), joints);
        return new NoSQLConstraint(expression, joints);
    }

//
//    public static <M extends ModelObjectInterface> Constraint hasIn(Enum mockValue, Enum ... values) {
//        if (values == null || values.length == 0) {
//            return new AnyConstraint();
//        }
//        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
//        Pair<Class, String> pair = getSourceAttributePair();
//        clearMockCallSequence();
//        String attribute = makeAttributeIdentifier(pair);
//        Constraint[] constraints = new Constraint[values.length];
//        for (int i = 0; i < values.length; i++) {
//            SolrExpression expression = newLeafExpression().addConstrain(attribute, Comp.EQUAL, values[i].toString());
//            constraints[i] = new NoSQLConstraint(expression, joints);
//        }
//        return new AnyConstraint(constraints);
//    }
//
//    public static <M extends ModelObjectInterface> Constraint hasIn(ModelObjectInterface mockValue, ModelObjectInterface ... values) {
//        if(values == null) return new AnyConstraint();
//        ArrayList<String> strs = new ArrayList<String>();
//        for(int i = 0; i < values.length; i++){
//            if(values[i] != null){
//                strs.add(values[i].getObjectID());
//            }
//        }
//        return hasIn(mockValue.getObjectID(), strs.toArray(new String[strs.size()]));
//    }

//    public static Constraint hasIn(String mockValue, String ... values) {
//        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
//        Pair<Class, String> pair = getSourceAttributePair();
//        clearMockCallSequence();
//        String attribute = makeAttributeIdentifier(pair);
//        Constraint[] constraints = new Constraint[values.length];
//        for(int i = 0; i < values.length; i++){
//            SolrExpression expression = newLeafExpression().addConstrain(attribute, Comp.EQUAL, values[i]);
//            constraints[i] = new NoSQLConstraint(expression, i == 0 ? joints : new ArrayList<Pair<Class, String>>());
//        }
//        return new AnyConstraint(constraints);
//    }

    private static NoSQLExpression newLeafExpression(){
//        return ModelObjectSearchService.noSQLService(selectClass);
        return new NoSQLExpression();
    }


    public static class NoSQLNegatingExpression extends NoSQLExpression {
        NoSQLExpression expression = null;
        ArrayList<NoSQLFunction> noSQLFunctions = new ArrayList<NoSQLFunction>();

        public void setExpression(NoSQLExpression expression) {
            this.expression = expression;
        }

        public NoSQLExpression getExpression() {
            return expression;
        }

        //        //TODO: noSQLFunctions is not in use in the moment for Containers ...
//        @Override
//        public void addFunction(NoSQLFunction noSQLFunction){
//            noSQLFunctions.add(noSQLFunction);
//        }

//        @Override
//        public String updateQuery(SolrQuery solrQuery) {
//            StringBuilder builder = new StringBuilder("-");
//            String subQuery = expression.updateQuery(solrQuery);
//            if(subQuery != null){
//                builder.append(subQuery);
//            }
//
//            return builder.toString();
//        }

    }

    public static class NoSQLContainerExpression extends NoSQLExpression {
        List<NoSQLExpression> expressions = new ArrayList<NoSQLExpression>();
        List<Integer> conditions = new ArrayList<Integer>();

        public NoSQLContainerExpression addExpression(NoSQLExpression expression) {
            expressions.add(expression);
            return this;
        }

        public NoSQLContainerExpression addExpression(int condition, NoSQLExpression expression) {
            addExpression(expression);
            addCondition(condition);
            return this;
        }

        public NoSQLContainerExpression addCondition(int condition) {
            conditions.add(condition);
            return this;
        }

        public List<NoSQLExpression> getExpressions() {
            return expressions;
        }

        public List<Integer> getConditions() {
            return conditions;
        }

        //        public String updateQuery(SolrQuery solrQuery) {
//            StringBuilder builder = new StringBuilder();
//            for(int i = 0; i < expressions.size(); i++) {
//                NoSQLExpression expression = expressions.get(i);
//                Integer condition = conditions.get(i);
//                String subQuery = expression.updateQuery(solrQuery);
////                log.debug("Will add subQuery("+ subQuery +") with " + SolrOperator.name(condition));
//                if(subQuery != null){
//                    builder.append(subQuery);
//                    if(expressions.size() > 1 && i + 1 < expressions.size()){
//                        builder.append(" " + NoSQLOperator.name(condition) + " ");
//                    }
//                }
//            }
//
//            if(builder.length() > 2){
//                builder.insert(0, "(");
//                builder.append(")");
//            }
//            return builder.toString();
//        }
    }


    public static String removeFunnyChars(String s){
//        log.debug("START: removeFunnyChars: input ("+ s +")");
        if(s == null || s.equals("")){
            return s;
        } else {
            s = s.replaceAll("\"", " ").replaceAll("!", " ").replaceAll("\\|", " ").replaceAll("'", " ").replaceAll("\\^", " ")
                    .replaceAll("$", " ").replaceAll("§", " ").replaceAll("#", " ").replaceAll(":", " ").replaceAll("_", " ")
                    .replaceAll("/", " ").replaceAll(";", " ").replaceAll("€", " ").replaceAll("%", " ").replaceAll("/", " ")
                    .replaceAll("\\?", " ").replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\{", " ").replaceAll("\\}", " ")
                    .replaceAll("\\[", " ").replaceAll("\\]", " ")
                    .replaceAll("<", " ").replaceAll(">", " ").replaceAll("^", " ").replaceAll("~", " ").replaceAll("\\+", " ").replaceAll("-", " ")
                    .trim();
            s = " " + s + " ";
            String[] noWords = new String[]{"or", "and", "not"};
            String[] split = s.trim().split(" ");
            List<String> ss = new LinkedList<String>();
            for(int i = 0; i < split.length; i++){
                ss.add(split[i]);
            }


            StringBuilder toReturn = new StringBuilder();
//            for(int j = 0; j < ss.length; j++){
//                boolean cleanWord = true;
//                for(int i = 0; cleanWord && i < noWords.length; i++){
//                    if(ss[j].equalsIgnoreCase(noWords[i])){
//                        cleanWord = false;
//                   }
//                }
//                if(cleanWord){
//                    toReturn.append(ss[j]);
//                    toReturn.append(' ');
//                }
//            }
            for(int j = 0; j < ss.size(); j++){
                boolean cleanWord = true;
                if(j == 0) {
                    for (int i = 0; cleanWord && i < noWords.length; i++) {
                        if (ss.get(j).equalsIgnoreCase(noWords[i])) {
                            cleanWord = false;
                        }
                    }
                }
                if(!cleanWord){
                    ss.remove(j);
                    j--;
                }
            }

            for(int j = 0; j < ss.size(); j++){
                boolean cleanWord = true;
                if(j == 0) {
                    for (int i = 0; cleanWord && i < noWords.length; i++) {
                        if (ss.get(ss.size() - j - 1).equalsIgnoreCase(noWords[i])) {
                            cleanWord = false;
                        }
                    }
                }
                if(!cleanWord){
                    ss.remove(ss.size() - j - 1);
                    j--;
                }
            }


            for(int j = 0; j < ss.size(); j++){
                toReturn.append(ss.get(j));
                toReturn.append(" ");
            }


            s = toReturn.toString();
            s = (s == null || s.trim().equals("") ? "*" : s.trim());
            log.trace("END: removeFunnyChars returns input ("+ s +")");
            return s;


        }

    }


    public static void main(String[] args) {
        System.out.println(removeFunnyChars("wegner and sort"));
    }


    private static String createSearchString(String textQuery) {
        String cleanText = removeFunnyChars(textQuery);
//        if(!cleanText.equals("")){
//            StringTokenizer toks = n//ew StringTokenizer(cleanText, " ");
//            String nString = "";
//            ArrayList<String> nt = new ArrayList<String>();
//            while (toks.hasMoreTokens()){
//                String s = toks.nextToken().toLowerCase();
//                if(s.equalsIgnoreCase("AND") || s.equalsIgnoreCase("OR")){
//                    nt.add(s);
//                } else if(s.indexOf("*") != -1) {
//                    String substring = s;
//                    if(substring.startsWith("*")){
//                        substring = substring.substring(1);
//                    }
//                    nt.add(substring);
//                } else if(s.indexOf("*") == -1){
//                    nt.add(s);
//                } else {
//                    nt.add("(" + s + ") ");
//                }
//            }
//            for(int i = 0; i < nt.size(); i++){
//                if(!nt.get(i).equalsIgnoreCase("AND") && !nt.get(i).equalsIgnoreCase("OR")){
//                    nString += "("+ nt.get(i) +")" + (i + 1 < nt.size() && !nt.get(i+1).equalsIgnoreCase("AND") && !nt.get(i+1).equalsIgnoreCase("OR") ? " AND " : " ");
//                } else {
//                    if(i + 1 < nt.size()){
//                        nString += nt.get(i).toUpperCase() + " ";
//                    } else {
//                        nString += "\""+ nt.get(i).toLowerCase() + "\"" + " ";
//                    }
//                }
//            }
//
//            return nString;
//        }
        return cleanText;
    }





    public static String asString(Object expression){
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        String attr = makeAttributeIdentifier(pair);
        clearMockCallSequence();
        String finalSolrAttributeName = createFinalSolrAttributeName(joints, attr);
//        log.debug("asString("+ finalSolrAttributeName +")");
        return finalSolrAttributeName;
    }



    public static String createFinalSolrAttributeName(List<Pair<Class, String>> joints, String attr){
        if(joints == null || joints.isEmpty()){
            return attr;
        } else {
            boolean isMultiAss = false;
            String attributeName = "";
            for(int i = 0; i < joints.size(); i++){
                Pair<Class, String> classStringPair = joints.get(i);
                DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(classStringPair.getFirst());
                DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(classStringPair.getSecond());
                if(dbAttribute.isMultiAssociation()){
                    isMultiAss = true;
                }
                attributeName = dbAttribute.getSolrAttributeName(attributeName);
            }
            return attributeName + attr + (isMultiAss ? "_ARRAY" : "");
        }
    }



    public static Constraint not(Constraint constraint) {
        return new NotConstraint(constraint);
    }

    public static Constraint all(Collection<Constraint> constraints) {
        return all(toConstraintArray(constraints));
    }

    public static Constraint all(Constraint ... constraints) {
        return new AllConstraint(constraints);
    }

    public static Constraint any(Collection<Constraint> constraints) {
        return any(toConstraintArray(constraints));
    }

    public static Constraint any(Constraint ... constraints) {
        return new AnyConstraint(constraints);
    }

    private static Constraint[] toConstraintArray(Collection<Constraint> constraints) {
        return (Constraint[]) new ArrayList(constraints).toArray(new Constraint[constraints.size()]);
    }



    public static class NoSQLExpression {

        private static final Logger log = LoggerFactory.getLogger(NQL.class);

        protected String attr = null;
        protected Object value = null;
        protected Class valueClazz = null;
        protected boolean isNotNull = false;
        protected boolean isNull = false;
        protected boolean isEnum = false;
        protected boolean isRouting = false;
        protected boolean isSharding = false;
        protected String raw = null;
        protected Calendar from = null;
        protected Calendar to = null;
        protected List<Pair<Class, String>> joints;
        protected NQL.Comp comparator;
        protected ArrayList<NQL.NoSQLFunction> noSQLFunctions = new ArrayList<NQL.NoSQLFunction>();


        public void addFunction(NQL.NoSQLFunction noSQLFunction){
            noSQLFunctions.add(noSQLFunction);
        }

        public NoSQLExpression addConstrain(String attributeName, NQL.Comp comparator, String value) {
            this.attr = attributeName;
            this.value = value;
            this.valueClazz = (value != null ? value.getClass() : null);
            this.comparator = comparator;
            return this;
        }

        public NoSQLExpression addConstrain(String attributeName, NQL.Comp comparator, Enum value) {
            this.attr = attributeName;
            this.value = value;
            this.isEnum = true;
            this.valueClazz = (value != null ? value.getClass() : null);
            this.comparator = comparator;
            return this;
        }

        public NoSQLExpression addConstrain(String attributeName, NQL.Comp comparator, Calendar value) {
            this.value = value;
            this.valueClazz = (value != null ? value.getClass() : null);
            this.attr = attributeName;
            this.comparator = comparator;
            return this;
        }

        public NoSQLExpression addConstrain(String attributeName, NQL.Comp comparator, int value) {
            log.trace("addConstrain:int("+ value +")");
            this.attr = attributeName;
            this.value = value;
            this.valueClazz = Integer.class;
            this.comparator = comparator;
            return this;
        }

        public NoSQLExpression addConstrain(String attributeName, NQL.Comp comparator, double value) {
            log.trace("addConstrain:double("+ value +")");
            this.attr = attributeName;
            this.value = value;
            this.valueClazz = Double.class;
            this.comparator = comparator;
            return this;
        }

        public NoSQLExpression addConstrain(String attributeName, NQL.Comp comparator, float value) {
            log.trace("addConstrain:float("+ value +")");
            this.attr = attributeName;
            this.value = value;
            this.valueClazz = Float.class;
            this.comparator = comparator;
            return this;
        }

        public NoSQLExpression addConstrain(String attributeName, NQL.Comp comparator, long value) {
            log.trace("addConstrain:long("+ value +")");
            this.attr = attributeName;
            this.value = value;
            this.valueClazz = Long.class;
            this.comparator = comparator;
            return this;
        }
        public NoSQLExpression addConstrain(String query) {
            log.trace("addConstrain:("+ query+")");
            raw = query;
            return this;
        }

        public String getRaw() {
            return raw;
        }

        public boolean isRouting() {
            return isRouting;
        }

        public void setRouting(boolean routing) {
            isRouting = routing;
        }

        public boolean isSharding() {
            return isSharding;
        }

        public void setSharding(boolean sharding) {
            isSharding = sharding;
        }

        public Calendar getFrom() {
            return from;
        }

        public void setFrom(Calendar from) {
            this.from = from;
        }

        public Calendar getTo() {
            return to;
        }

        public void setTo(Calendar to) {
            this.to = to;
        }

        public NoSQLExpression isNull(String attributeName, List<Pair<Class, String>> joints) {
            this.attr = NQL.createFinalSolrAttributeName(joints, attributeName);
            isNull = true;
            return this;
        }

        public NoSQLExpression isNotNull(String attributeName, List<Pair<Class, String>> joints) {
//            log.debug("isNotNull("+ attributeName +")");
            this.attr = NQL.createFinalSolrAttributeName(joints, attributeName);
            isNotNull = true;
            return this;
        }

        public NoSQLExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, String value) {
            return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }

        public NoSQLExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, Calendar value) {
            return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }


        public NoSQLExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, int value) {
            return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }

        public NoSQLExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, double value) {
            return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }

        public NoSQLExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, float value) {
            return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }

        public void addJoints(List<Pair<Class, String>> joints) {
            this.joints = joints;
        }

        public String getAttr() {
            return attr;
        }

        public Object getValue() {
            return value;
        }

        public Class getValueClazz() {
            return valueClazz;
        }

        public boolean isNotNull() {
            return isNotNull;
        }

        public boolean isNull() {
            return isNull;
        }

        public List<Pair<Class, String>> getJoints() {
            return joints;
        }

        public Comp getComparator() {
            return comparator;
        }

        public boolean isEnum() {
            return isEnum;
        }

        public ArrayList<NoSQLFunction> getNoSQLFunctions() {
            return noSQLFunctions;
        }
    }




    public static abstract class NoSQLFunction {

    }


    public static class NoSQLMathFunction extends NoSQLFunction {

        String expression = null;

        public NoSQLMathFunction(String expression){
            this.expression = expression;
        }

        @Override
        public String toString() {
            return expression;
        }
    }


    public static class Boost extends NoSQLFunction {
        int boost;
        public Boost(int boost){
            this.boost = boost;
        }

        public int getBoost() {
            return boost;
        }
    }


    public static abstract class Constraint {
        abstract public NoSQLExpression getExpression();
        abstract public List<Pair<Class, String>> getJoints();
    }


    public static class NoSQLConstraint extends Constraint {

        final NoSQLExpression expression;
        final List<Pair<Class, String>> joints;

        public NoSQLConstraint(NoSQLExpression expression, List<Pair<Class, String>> joints) {
            this.expression = expression;
            expression.addJoints(joints);
            this.joints = joints;
        }

        @Override
        public NoSQLExpression getExpression() {
            return expression;
        }

        @Override
        public List<Pair<Class, String>> getJoints() {
            return joints;
        }
    }


    public abstract static class OperatorConstraint extends Constraint {
        private final Constraint[] constraints;
        private final NoSQLOperator operator;

        protected boolean hasConstraints() {
            return constraints.length > 0;
        }

        private OperatorConstraint(NoSQLOperator operator, Constraint... constraints) {
            this.operator = operator;
            this.constraints = constraints;
        }



        public NoSQLExpression getExpression() {
            NoSQLContainerExpression container = new NoSQLContainerExpression();
            for (Constraint c: constraints) {
//                log.debug("getExpression() with operator.operator("+ operator.name() +")");
                container.addExpression(operator.operator, c.getExpression());

            }
            return container;
        }

        public List<Pair<Class, String>> getJoints() {
            List<Pair<Class, String>> joints = new ArrayList<Pair<Class, String>>();
            for (Constraint constraint: constraints) {
                joints.addAll(constraint.getJoints());
            }
            return joints;
        }
    }


    /**
     * This constraint is true if all of its child constraints is true.
     */
    public static class AllConstraint extends OperatorConstraint {
        private AllConstraint(Constraint... constraints) {
            super(NoSQLOperator.AND, constraints);
        }
    }

    /**
     * This constraint is true if at least one of its child constraints is true.
     * If non child constraints is given, this constraint is false.
     */
    public static class AnyConstraint extends OperatorConstraint {
        private AnyConstraint(Constraint... constraints) {
            super(NoSQLOperator.OR, constraints);
        }

        @Override
        public NoSQLExpression getExpression() {
            if (! hasConstraints()) {
                return new NoSQLExpression();
            }
            return super.getExpression();
        }
    }

    /**
     * This constraint that will negate the wrapped constraint.
     * If non child constraints is given, this constraint is false.
     */
    public static class NotConstraint extends Constraint {
        private Constraint constraint;
        private NotConstraint(Constraint constraint) {
            this.constraint = constraint;
        }

        @Override
        public NoSQLExpression getExpression() {
            NoSQLNegatingExpression container = new NoSQLNegatingExpression();
            container.setExpression(constraint.getExpression());
            return container;
        }

        @Override
        public List<Pair<Class, String>> getJoints() {
            List<Pair<Class, String>> joints = new ArrayList<Pair<Class, String>>();
            joints.addAll(constraint.getJoints());
            return joints;
        }
    }

    private static NoSQLExpression trueExpression() {
        return null;
    }

    /*
     * Mock stuff
     */

    private static boolean isMock(Object object) {
        return object instanceof MockExtra;
    }

    private static void addToMockCallSequence(Object mock, Method method) {
        LinkedList<Pair<Object, Method>> mockSequence = threadMockCallSequenceMap.get(Thread.currentThread());
        if (mockSequence == null) {
            mockSequence = new LinkedList<Pair<Object, Method>>();
            threadMockCallSequenceMap.put(Thread.currentThread(), mockSequence);
        }
//        log.debug("threadMockCallSequenceMap.addLast(" + method +") on " + Thread.currentThread().getId());
        mockSequence.addLast(new Pair<Object, Method>(mock, method));
    }

    private static void clearMockCallSequence() {
        threadMockCallSequenceMap.remove(Thread.currentThread());
    }

    private static String fieldName(Method getMethod) {
        String methodName = getMethod.getName();
        if (methodName.startsWith("get")) {
            return methodName.substring(3,4).toLowerCase()+ methodName.substring(4);
        } else {
            throw new IllegalArgumentException("Get-method expected. Got method "+methodName);
        }
    }




}

package dk.lessismore.nojpa.db.methodquery;

import dk.lessismore.nojpa.db.statements.ContainerExpression;
import dk.lessismore.nojpa.db.statements.Expression;
import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.reflection.db.model.SolrService;
import dk.lessismore.nojpa.utils.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

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
    public enum ReadOnly {RO, RW}
    public enum SolrOperator {OR(0), AND(1);
        final int operator;
        SolrOperator(int i){
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
        return new SearchQuery<T>((Class<T>) ((MockExtra)sourceMock).mockExtra_getSourceClass());
    }
    public static <T extends ModelObjectInterface> SearchQuery<T> search(Class<T> sourceClass) {
        clearMockCallSequence();
        return new SearchQuery<T>(sourceClass);
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
    public static class SearchQuery<T extends ModelObjectInterface> {

        private final Class<T> selectClass;
//        private final SelectSolrStatementCreator creator;
        ArrayList<Constraint> rootConstraints;
        SolrQuery solrQuery;
        String orderByAttribute;
        Order orderByORDER = Order.ASC;
        int startLimit = -1;
        int endLimit = -1;
        String preBoost = null;
//        private final SelectSQLStatement statement;
        private boolean useCache = true;

        public SearchQuery(Class<T> selectClass) {
            this.selectClass = selectClass;
            solrQuery = new SolrQuery();
//            creator = new SelectSolrStatementCreator();
//            creator.setSource(selectClass);
            rootConstraints = new ArrayList<Constraint>();
//            statement = creator.getSelectSQLStatement();
        }



        public SearchQuery<T> addFunction(SolrFunction solrMathFunction) {
            rootConstraints.get(rootConstraints.size()-1).getExpression().addSolrFunction(solrMathFunction);
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

        public <M extends ModelObjectInterface> SearchQuery<T> scoreMin(float lower) {
            solrQuery.setFilterQueries("{!frange l=" + lower + "}query($q)");
            return this;
        }

        public <M extends ModelObjectInterface> SearchQuery<T> scoreMax(float upper) {
            solrQuery.setFilterQueries("{!frange u=" + upper + "}query($q)");
            return this;
        }

        public <M extends ModelObjectInterface> SearchQuery<T> scoreWithin(float lower, float upper) {
            solrQuery.setFilterQueries("{!frange l=" + lower + " " + "u=" + upper + "}query($q)");
            return this;
        }
//
//        private String makeAttributeIdentifier(Pair<Class, String> sourceAttributePair) {
//            return DbClassReflector.getDbAttributeContainer(sourceAttributePair.getFirst()).getDbAttribute(sourceAttributePair.getSecond()).getAttributeName();
//        }
//
//        private String makeAttributeIdentifier(Class clazz, String attributeName) {
//            return DbClassReflector.getDbAttributeContainer(clazz).getDbAttribute(attributeName).getAttributeName();
//        }

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

        public <N> List<Pair<String, Long>> getCloud(N variable, int limit) {
            List<Pair<String, Long>> toReturn = new ArrayList<Pair<String, Long>>();
            try {

                List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
                Pair<Class, String> pair = getSourceAttributePair();
                String attributeIdentifier = makeAttributeIdentifier(pair);
                clearMockCallSequence();


                buildQuery();
                SolrService solrServer = ModelObjectSearchService.solrService(selectClass);

                solrQuery.setFacet(true);
                solrQuery.setFacetLimit(limit);
                solrQuery.addFacetField(attributeIdentifier);

                QueryResponse response = solrServer.query(solrQuery);
                List<FacetField.Count> facets = response.getFacetFields().get(0).getValues();
                for (FacetField.Count facet : facets) {
                    toReturn.add(new Pair<>(facet.getName(), facet.getCount()));
                }

            } catch (Exception e){
                log.error("Some error in getMax() : " + e, e);
                throw new RuntimeException("getMax - error", e);
            }

            return toReturn;
        }




        public <N extends Number> NStats<N> getStats(N variable) {
            NStats<N> nStats = new NStats<N>();
            try {

                List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
                Pair<Class, String> pair = getSourceAttributePair();
                String attributeIdentifier = makeAttributeIdentifier(pair);
                clearMockCallSequence();


                buildQuery();
                SolrService solrServer = ModelObjectSearchService.solrService(selectClass);

                solrQuery.setGetFieldStatistics(true);
                solrQuery.setParam("stats.field", attributeIdentifier);
                solrQuery.setParam("stats.facet", attributeIdentifier);

                QueryResponse queryResponse = solrServer.query(solrQuery);

                Map<String, FieldStatsInfo> fieldStatsInfo = queryResponse.getFieldStatsInfo();

                FieldStatsInfo sInfo = fieldStatsInfo.get(attributeIdentifier);
                if(sInfo == null){
                    return nStats;
                }
                nStats.min = (Double) sInfo.getMin();
                nStats.max = (Double) sInfo.getMax();
                nStats.sum = (Double) sInfo.getSum();
                nStats.count = sInfo.getCount();
                nStats.mean = (Double) sInfo.getMean();
                nStats.stddev = (Double) sInfo.getStddev();
            } catch (Exception e){
                log.error("Some error in getMax() : " + e, e);
                throw new RuntimeException("getMax - error", e);
            }

            return nStats;
        }




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



        public List<String> suggest(String userInput){
            ArrayList<String> tags = new ArrayList<String>();
            try {
                SolrQuery query = new SolrQuery();
                query.setRequestHandler("/suggest");
                query.setQuery(ClientUtils.escapeQueryChars(userInput));
                SolrService solrServer = ModelObjectSearchService.solrService(selectClass);
                QueryResponse response = solrServer.query(query);

//                NamedList<Object> namedList = response.getResponse();
//                for(Iterator<Map.Entry<String, Object>> iterator = namedList.iterator(); iterator.hasNext(); ){
//                    Map.Entry<String, Object> kv = iterator.next();
//                    log.debug("NamedList::: k("+ kv.getKey() +") -> " + );
//                }


                Object solrSuggester = ((HashMap) response.getResponse().get("suggest")).get("default");
                SimpleOrderedMap suggestionsList = ((SimpleOrderedMap<SimpleOrderedMap>)solrSuggester).getVal(0);
                ArrayList<SimpleOrderedMap> suggestions = (ArrayList<SimpleOrderedMap>)suggestionsList.get("suggestions");

                for(SimpleOrderedMap suggestion : suggestions) {
                    tags.add((String)suggestion.get("term"));
                }

            } catch (Exception e) {
                log.error("Some error when running suggest: " + e, e);
            }
            return tags;
        }




//        public void visit(DbObjectVisitor visitor){
//            log.debug("Will run: DbObjectSelector.iterateObjectsFromDb(selectClass, statement, visitor)");
//            statement.addExpression(getExpressionAddJoins());
//            DbObjectSelector.iterateObjectsFromDb(selectClass, statement, visitor);
//        }
//


        public SolrQuery buildQuery(){
            StringBuilder builder = new StringBuilder();
            solrQuery.setQuery("*:*");
            for (int i = 0; i < rootConstraints.size(); i++) {
                Constraint constraint = rootConstraints.get(i);
                String subQuery = constraint.getExpression().updateSolrQuery(solrQuery);
                if (subQuery != null) {
                    builder.append(subQuery);
                }
            }
            String query = builder.toString();
            if (query == null || query.length() < 2) {
                query = "*:*";
            } else {
                if (preBoost != null) {
                    query = preBoost + " " + query;
                }
            }
            log.debug("We will query = " + query);
            solrQuery.setQuery(query);
            if (startLimit != -1) {
                solrQuery.setStart(startLimit);
                solrQuery.setRows(endLimit - startLimit);
            }
            if (this.orderByAttribute != null) {
                log.debug("Will sort by " + orderByAttribute + " with " + this.orderByORDER);
                solrQuery.addSort(this.orderByAttribute, this.orderByORDER == Order.ASC ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
            }

            return solrQuery;
        }




        @SuppressWarnings("unchecked")
        private NList<T> selectObjectsFromDb() {
//            TimerWithPrinter timer = new TimerWithPrinter("selectObjectsFromDb", "/tmp/luuux-timer-getPosts.log");
//            log.debug("DEBUG-TRACE", new Exception("DEBUG"));
            List<T> toReturn = new ArrayList<T>();
            try {

                buildQuery();

//                timer.markLap("1");
                SolrService solrServer = ModelObjectSearchService.solrService(selectClass);
//                timer.markLap("2");
                if(!DEBUG_EXPLAIN){
                    solrQuery.setFields("objectID");
                } else {
                    solrQuery.setFields("*, score, _explain_");
                }
//                solrQuery.setParam("bf", "sum(_Post_pageViewCounter__ID_Counter_count__LONG,8)");
                long start = System.currentTimeMillis();
                QueryResponse queryResponse = solrServer.query(solrQuery);
                log.info("[{}ms size: {}] Will solr query: {}", System.currentTimeMillis() - start, queryResponse.getResults().getNumFound(), solrQuery.toString().replace('+', ' '));

//                timer.markLap("3");
//                log.debug("queryResponse = " + queryResponse.getResults().size());
//                log.debug("queryResponse = " + queryResponse.getResults().getNumFound());
//                log.debug("queryResponse = " + queryResponse.getResults().);
                int size = queryResponse.getResults().size();
//                timer.markLap("4");
                for(int i = 0; i < size; i++){
                    SolrDocument entries = queryResponse.getResults().get(i);
                    String objectID = entries.get("objectID").toString();

                    if(DEBUG_EXPLAIN) {
                        if (i == 0) {
                            Iterator<String> iterator = entries.getFieldNames().iterator();
                            for (; iterator.hasNext(); ) {
                                String next = iterator.next();
                                log.debug("Fieldnames:" + next);
                            }
                        }

                        if (entries.containsKey("score")) {
                            log.debug("objectID(" + objectID + ") has score(" + entries.get("score") + ")");
                        }
                        if (entries.containsKey("_explain_")) {
                            log.debug("_explain_ :: (" + entries.get("_explain_") + ")");
                        }
                    }

                    T t = MQL.selectByID(selectClass, objectID);
                    if(t == null){
                        log.error("We have a problem with the sync between the DB & Solr ... Can't find objectID("+ objectID +") class("+ selectClass +")" , new Exception("Sync problem"));
                    } else {
                        toReturn.add(t);
                    }
                }
//                timer.markLap("5");
                log.debug("Returns the size of Nlist.size() -> " + toReturn.size() + " .... size("+ size +")");
                return (NList<T>) Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class[]{NList.class},
                        new NListImpl(queryResponse, toReturn));

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



    private static class NListImpl implements InvocationHandler {

        private final QueryResponse queryResponse;
        private final List resultList;

        public NListImpl(QueryResponse queryResponse, List resultList) {
            this.queryResponse = queryResponse;
            this.resultList = resultList;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            log.debug("RListImpl::Calling " + method.getName() + "()");
            String methodName = method.getName();
            if (methodName.equals("getNumberFound")) {
                return queryResponse.getResults().getNumFound();
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
            log.debug("Did you mix up MQL and NQL???? ... Or did you call 2 mock-methods in the same statement? .... Some mock calls are expected to have been made at this time Thread.currentThread().getId(" + Thread.currentThread().getId() + ")");
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
        SolrExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new SolrConstraint(expression, joints);
    }

    public static Constraint has(boolean mockValue, Comp comp, boolean value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value ? 1 : 0);
        return new SolrConstraint(expression, joints);
    }

    public static Constraint has(String mockValue, Comp comp, String value) {
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
            value = createSearchString(value);
        }
        value = (value == null || value.trim().equals("") ? "*" : value);
        SolrExpression expression = null;
        if(joints.size() == 0 && pair.getSecond().equals("objectID")){
            expression = newLeafExpression().addConstrain("objectID", comp, value);
        } else {
            expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        }
        return new SolrConstraint(expression, joints);
    }



    public static Constraint has(Enum mockValue, Comp comp, Enum value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
//        if(pair.getFirst().isEnum() && pair.getSecond().equals("objectID")){ //Enum-array
//            Pair<Class, String> classStringPair = joints.get(joints.size() - 1);
//            pair = new Pair<Class, String>(classStringPair.getFirst(), classStringPair.getSecond());
////            joints.remove(joints.size() - 1);
//        }
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new SolrConstraint(expression, joints);
    }

    public static Constraint has(Calendar mockValue, Comp comp, Calendar value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new SolrConstraint(expression, joints);
    }

    public static Constraint has(double mockValue, Comp comp, double value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new SolrConstraint(expression, joints);
    }

    public static Constraint has(long mockValue, Comp comp, long value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new SolrConstraint(expression, joints);
    }


    public static <M extends ModelObjectInterface> Constraint has(M mockValue, Comp comp, M model) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression;
        if (model instanceof MockExtra) {
            expression = trueExpression();
        } else {
            expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, model.getObjectID());
        }
        return new SolrConstraint(expression, joints);
    }

    public static Constraint has(String query) {
        SolrExpression expression = new SolrExpression();
        expression.addConstrain(query);
        return new SolrConstraint(expression, new ArrayList<>());
    }

    public static <M extends ModelObjectInterface> Constraint hasNull(M mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().isNull(makeAttributeIdentifier(pair), joints);
        return new SolrConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNotNull(M mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().isNotNull(makeAttributeIdentifier(pair), joints);
        return new SolrConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNull(Calendar mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().isNull(makeAttributeIdentifier(pair), joints);
        return new SolrConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNull(String mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().isNull(makeAttributeIdentifier(pair), joints);
        return new SolrConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNotNull(Calendar mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().isNotNull(makeAttributeIdentifier(pair), joints);
        return new SolrConstraint(expression, joints);
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
//            constraints[i] = new SolrConstraint(expression, joints);
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
//            constraints[i] = new SolrConstraint(expression, i == 0 ? joints : new ArrayList<Pair<Class, String>>());
//        }
//        return new AnyConstraint(constraints);
//    }

    private static SolrExpression newLeafExpression(){
        return new SolrExpression();
    }


    public static class SolrNegatingExpression implements UpdateSolrQueryAble {
        UpdateSolrQueryAble expression = null;
        ArrayList<SolrFunction> solrFunctions = new ArrayList<SolrFunction>();

        public void setExpression(UpdateSolrQueryAble expression) {
            this.expression = expression;
        }

        //TODO: solrFunctions is not in use in the moment for Containers ...
        @Override
        public void addSolrFunction(SolrFunction solrFunction){
            solrFunctions.add(solrFunction);
        }

        @Override
        public String updateSolrQuery(SolrQuery solrQuery) {
            StringBuilder builder = new StringBuilder("-");
            String subQuery = expression.updateSolrQuery(solrQuery);
            if(subQuery != null){
                builder.append(subQuery);
            }

            return builder.toString();
        }

    }

    public static class SolrContainerExpression implements UpdateSolrQueryAble{
        List<UpdateSolrQueryAble> expressions = new ArrayList<UpdateSolrQueryAble>();
        List<Integer> conditions = new ArrayList<Integer>();
        ArrayList<SolrFunction> solrFunctions = new ArrayList<SolrFunction>();

        //TODO: solrFunctions is not in use in the moment for Containers ...
        public void addSolrFunction(SolrFunction solrFunction){
            solrFunctions.add(solrFunction);
        }


        public SolrContainerExpression addExpression(UpdateSolrQueryAble expression) {
            expressions.add(expression);
            return this;
        }

        public SolrContainerExpression addExpression(int condition, UpdateSolrQueryAble expression) {
            addExpression(expression);
            addCondition(condition);
            return this;
        }

        public SolrContainerExpression addCondition(int condition) {
            conditions.add(condition);
            return this;
        }

        //    public ContainerExpression reAddCondition(int condition) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
        public int nrOfExpressions() {
            int n = 0;
            Iterator iterator = expressions.iterator();
            while (iterator.hasNext()) {
                Expression expression = (Expression) iterator.next();
                if (expression instanceof ContainerExpression) {
                    n += ((ContainerExpression) expression).nrOfExpressions();
                } else {
                    n++;
                }
            }
            return n;
        }


        public String updateSolrQuery(SolrQuery solrQuery) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < expressions.size(); i++) {
                UpdateSolrQueryAble expression = expressions.get(i);
                Integer condition = conditions.get(i);
                String subQuery = expression.updateSolrQuery(solrQuery);
//                log.debug("Will add subQuery("+ subQuery +") with " + SolrOperator.name(condition));
                if(subQuery != null){
                    builder.append(subQuery);
                    if(expressions.size() > 1 && i + 1 < expressions.size()){
                        builder.append(" " + SolrOperator.name(condition) + " ");
                    }
                }
            }

            if(builder.length() > 2){
                builder.insert(0, "(");
                builder.append(")");
            }
            return builder.toString();
        }
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
            s = (s == null || s.trim().equals("") ? "*" : s);
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



    public interface UpdateSolrQueryAble {


        String updateSolrQuery(SolrQuery solrQuery);
        void addSolrFunction(SolrFunction solrFunction);


    }




    public static class SolrExpression implements UpdateSolrQueryAble {

        String statement = "*:*";
        String attr = null;
        String value = null;
        private List<Pair<Class, String>> joints;
        Comp comparator;
        ArrayList<SolrFunction> solrFunctions = new ArrayList<SolrFunction>();


        public void addSolrFunction(SolrFunction solrFunction){
            solrFunctions.add(solrFunction);
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, String value) {
            this.statement = "("+ attributeName +":("+ value +"))";
            this.attr = attributeName;
            this.value = value;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, Enum value) {
            this.statement = "("+ attributeName +":("+ value +"))";
            this.attr = attributeName;
            this.value = ""+ value;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, Calendar value) {
            SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2011-11-28T18:30:30Z
            xmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            //solrObj.addField(solrAttributeName, xmlDateFormat.format(((Calendar) value).getTime()));

            this.value = xmlDateFormat.format(value.getTime());
            this.statement = "("+ attributeName +":("+ this.value +"))";
            this.attr = attributeName;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, int value) {
            log.trace("addConstrain:int("+ value +")");
            this.statement = "("+ attributeName +":"+ value +")";
            this.attr = attributeName;
            this.value = "" + value;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, double value) {
            log.trace("addConstrain:double("+ value +")");
            this.statement = "("+ attributeName +":"+ value +")";
            this.attr = attributeName;
            this.value = "" + value;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, float value) {
            log.trace("addConstrain:float("+ value +")");
            this.statement = "("+ attributeName +":"+ value +")";
            this.attr = attributeName;
            this.value = "" + value;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, long value) {
            log.trace("addConstrain:long("+ value +")");
            this.statement = "("+ attributeName +":"+ value +")";
            this.attr = attributeName;
            this.value = "" + value;
            this.comparator = comparator;
            return this;
        }
        public SolrExpression addConstrain(String query) {
            log.trace("addConstrain:("+ query+")");
            this.statement = query;
            return this;
        }

        public SolrExpression isNull(String attributeName, List<Pair<Class, String>> joints) {
//            log.warn("isNull ("+ attributeName +")");
            attributeName = createFinalSolrAttributeName(joints, attributeName);
            if (attributeName.endsWith("__DATE")) {
                this.statement = "-(" + attributeName + ":[* TO *])";
            } else {
                this.statement = "-(" + attributeName + ":[\"\" TO *])";
            }
            this.attr = "-" + attributeName;
            return this;
        }

        public SolrExpression isNotNull(String attributeName, List<Pair<Class, String>> joints) {
//            log.debug("isNotNull("+ attributeName +")");
            attributeName = createFinalSolrAttributeName(joints, attributeName);
            if (attributeName.endsWith("__DATE")) {
                this.statement = "(" + attributeName + ":[* TO *])";
            } else {
                this.statement = "(" + attributeName + ":[\"\" TO *])";
            }
            this.attr = attributeName;
            return this;
        }

        public SolrExpression addConstrain(Class sourceClass, String attributeName, Comp comparator, String value) {
            return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }

        public SolrExpression addConstrain(Class sourceClass, String attributeName, Comp comparator, Calendar value) {
            return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }


        public SolrExpression addConstrain(Class sourceClass, String attributeName, Comp comparator, int value) {
            return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }

        public SolrExpression addConstrain(Class sourceClass, String attributeName, Comp comparator, double value) {
            return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }

        public SolrExpression addConstrain(Class sourceClass, String attributeName, Comp comparator, float value) {
            return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
        }

        @Override
        public String updateSolrQuery(SolrQuery solrQuery) {
            String solrAttributeName = createFinalSolrAttributeName(joints, attr);
            String boostQuery = "";
            String otherFunctions = " ";
            for(int i = 0; i < solrFunctions.size(); i++){
                SolrFunction solrFunction = solrFunctions.get(i);
                if(solrFunction instanceof Boost){
                    boostQuery = "^" + ((Boost)solrFunction).boost;
                } else {
                    otherFunctions += " " + solrFunction;
                }
            }
            if (this.value == null) {
                return this.statement;
            }
            if(this.comparator == Comp.EQUAL_OR_LESS){
                return " (" + solrAttributeName + ":[* TO " + value + "]"+ boostQuery +")" + otherFunctions;
            } else if(this.comparator == Comp.EQUAL_OR_GREATER){
                return " (" + solrAttributeName + ":[" + value + " TO *]"+ boostQuery +")" + otherFunctions;
            } else if(this.comparator == Comp.NOT_EQUAL){
                return
                        (otherFunctions.equals(" ") ? "" : " (") +
                        " -(" + solrAttributeName + ":(" + value + "))"+ boostQuery +" " +
                                (otherFunctions.equals(" ") ? "" : " )" + otherFunctions);
            }

//            return " (" + solrAttributeName + ":(" + removeFunnyChars(value) + ")"+ boostQuery +")" + otherFunctions;
            return " (" + solrAttributeName + ":(" + (value) + ")"+ boostQuery +")" + otherFunctions;

        }
        //_Post_shareCounter__ID_Counter_count__TXT
        //_Post_shareCounter__ID_Counter_count__TXT

        public void addJoints(List<Pair<Class, String>> joints) {
            this.joints = joints;
        }
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



    private static String createFinalSolrAttributeName(List<Pair<Class, String>> joints, String attr){
        if(joints == null || joints.isEmpty()){
            return attr;
        } else {
            String attributeName = "";
            for(int i = 0; i < joints.size(); i++){
                Pair<Class, String> classStringPair = joints.get(i);
                DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(classStringPair.getFirst());
                DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(classStringPair.getSecond());
                attributeName = dbAttribute.getSolrAttributeName(attributeName);
            }
            return attributeName + attr + (attributeName.contains("_ARRAY") ? "_ARRAY" : "");
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


    public static abstract class SolrFunction {

    }


    public static class SolrMathFunction extends SolrFunction {

        String expression = null;

        public SolrMathFunction(String expression){
            this.expression = expression;
        }

        @Override
        public String toString() {
            return expression;
        }
    }


    public static class Boost extends SolrFunction {
        int boost;
        public Boost(int boost){
            this.boost = boost;
        }
    }


    public static abstract class Constraint {
        abstract public UpdateSolrQueryAble getExpression();
        abstract public List<Pair<Class, String>> getJoints();
    }

    public abstract static class OperatorConstraint extends Constraint {
        private final Constraint[] constraints;
        private final SolrOperator operator;

        protected boolean hasConstraints() {
            return constraints.length > 0;
        }

        private OperatorConstraint(SolrOperator operator, Constraint... constraints) {
            this.operator = operator;
            this.constraints = constraints;
        }



        public UpdateSolrQueryAble getExpression() {
            SolrContainerExpression container = new SolrContainerExpression();
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

    public static class SolrConstraint extends Constraint {

        final SolrExpression expression;
        final List<Pair<Class, String>> joints;

        public SolrConstraint(SolrExpression expression, List<Pair<Class, String>> joints) {
            this.expression = expression;
            expression.addJoints(joints);
            this.joints = joints;
        }

        @Override
        public SolrExpression getExpression() {
            return expression;
        }

        @Override
        public List<Pair<Class, String>> getJoints() {
            return joints;
        }
    }

    /**
     * This constraint is true if all of its child constraints is true.
     */
    public static class AllConstraint extends OperatorConstraint {
        private AllConstraint(Constraint... constraints) {
            super(SolrOperator.AND, constraints);
        }
    }

    /**
     * This constraint is true if at least one of its child constraints is true.
     * If non child constraints is given, this constraint is false.
     */
    public static class AnyConstraint extends OperatorConstraint {
        private AnyConstraint(Constraint... constraints) {
            super(SolrOperator.OR, constraints);
        }

        @Override
        public UpdateSolrQueryAble getExpression() {
            if (! hasConstraints()) {
                return new SolrExpression();
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
        public UpdateSolrQueryAble getExpression() {
            SolrNegatingExpression container = new SolrNegatingExpression();
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

    private static SolrExpression trueExpression() {
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

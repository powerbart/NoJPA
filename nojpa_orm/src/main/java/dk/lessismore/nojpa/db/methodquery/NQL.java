package dk.lessismore.nojpa.db.methodquery;

import dk.lessismore.nojpa.db.statements.*;
import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.utils.Pair;
import dk.lessismore.nojpa.utils.Strings;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* Created : with IntelliJ IDEA.
* User: seb
*/
public class NQL {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NQL.class);

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

        public SearchQuery<T> search(Constraint constraint) {
            rootConstraints.add(constraint);
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
//
//        // Only needed for reflection and should not be public exposed
//        public SearchQuery<T> orderByUnsafe(String attributeName, Order order) {
//            statement.setOrderBy(attributeName, orderToNum(order));
//            return this;
//        }

//        /**
//         * Set group by
//         * @param sourceInterface source class or interface
//         * @param attributeName attribute name
//         * @return this
//         */
//        public SearchQuery<T> groupBy(Class<? extends ModelObjectInterface> sourceInterface, String attributeName) {
//            statement.setGroupBy(getTableName(sourceInterface), attributeName);
//            return this;
//        }
//
//
//        /**
//         * Set having
//         * @param having sql having string
//         * @return this
//         */
//        public SearchQuery<T> having(String having) {
//            statement.setHaving(having);
//            return this;
//        }

//        public int getSum(int mock){
//            return (int) getSum(0L);
//        }
//
//
//
//        public float getSum(float mock){
//            return (float) getSum(0d);
//        }
//
//        public long getSum(long mock){
//            Pair<Class, String> sourceAttributePair = getSourceAttributePair();
//            String sumAtt = creator.makeAttributeIdentifier(sourceAttributePair.getFirst(), sourceAttributePair.getSecond());
//            statement.addExpression(getExpressionAddJoins());
//            clearMockCallSequence();
//            return DbObjectSelector.countSumFromDbAsLong(sumAtt, statement);
//        }
//
//        public double getSum(double mock){
//            Pair<Class, String> sourceAttributePair = getSourceAttributePair();
//            String sumAtt = creator.makeAttributeIdentifier(sourceAttributePair.getFirst(), sourceAttributePair.getSecond());
//            statement.addExpression(getExpressionAddJoins());
//            clearMockCallSequence();
//            return DbObjectSelector.countSumFromDbAsDouble(sumAtt, statement);
//        }


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

//
//        /**
//         * Return the number of results of the query
//         * @return result count.
//         */
//        public int getCount() {
//            return selectCountFromDb();
//        }

        /**
         * Execute query
         * @return result list, possible empty, never null.
         */
        public NList<T> getList() {
            NList<T> list = selectObjectsFromDb();
            return list;
        }

//        /**
//         * Execute query
//         * @return result array, possible empty, never null.
//         */
//        public T[] getArray() {
//            List<T> list = getList();
//            T[] array = (T[]) Array.newInstance(selectClass, list.size());
//            return  list.toArray(array);
//        }
//
//        /**
//         * Execute query with limit 1
//         * @return result if any, else null.
//         */
//        public T getFirst() {
//            limit(1);
//            List<T> list = getList();
//            if (list.isEmpty()) return null;
//            else return list.get(0);
//        }

//        /**
//         * Add join on attribute.
//         * This enables one to put constraint on the class of the join Attribute.
//         * Remark that this does not change the type of the elements later fetched (as in SQL).
//         * @param sourceClass source class
//         * @param joinAttributeName attribute name
//         * @return this
//         */
//        private SearchQuery<T> joinOn(Class sourceClass, String joinAttributeName) {
//            creator.addJoin(sourceClass, joinAttributeName);
//            return this;
//        }

//        private Expression getExpressionAddJoins() {
//            Constraint[] constraints = rootConstraints.toArray((Constraint[]) Array.newInstance(Constraint.class, rootConstraints.size()));
//            AllConstraint allConstraint = new AllConstraint(constraints);
//            for (Pair<Class, String> joint: allConstraint.getJoints()) {
//                joinOn(joint.getFirst(), joint.getSecond());
//            }
//            return allConstraint.getExpression();
//        }

//        public void visit(DbObjectVisitor visitor){
//            log.debug("Will run: DbObjectSelector.iterateObjectsFromDb(selectClass, statement, visitor)");
//            statement.addExpression(getExpressionAddJoins());
//            DbObjectSelector.iterateObjectsFromDb(selectClass, statement, visitor);
//        }
//
//        public SelectSQLStatement getSelectSQLStatement(){
//            statement.addExpression(getExpressionAddJoins());
//            return statement;
//        }    efasdfasdfasdfadsfa asdf asdfasdf

        @SuppressWarnings("unchecked")
        private NList<T> selectObjectsFromDb() {
            List<T> toReturn = new ArrayList<T>();
            try {
                StringBuilder builder = new StringBuilder();
                solrQuery.setQuery("*:*");
                for(int i = 0; i < rootConstraints.size(); i++){
                    Constraint constraint = rootConstraints.get(i);
                    String subQuery = constraint.getExpression().updateSolrQuery(solrQuery);
                    if(subQuery != null){
                        builder.append(subQuery);
                    }
                }
                String query = builder.toString();
                if(query == null || query.length() < 2){
                    query = "*:*";
                }
                log.debug("We will query = " + query);
                solrQuery.setQuery(query);
                if(startLimit != -1){
                    solrQuery.setStart(startLimit);
                    solrQuery.setRows(endLimit - startLimit);
                }
                if(this.orderByAttribute != null){
                    log.debug("Will sort by " + orderByAttribute + " with " + this.orderByORDER);
                    solrQuery.addSort(this.orderByAttribute, this.orderByORDER == Order.ASC ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                }
                SolrServer solrServer = ModelObjectSearchService.solrServer(selectClass);
                solrQuery.setFields("*", "score");
//                solrQuery.setParam("bf", "sum(_Post_pageViewCounter__ID_Counter_count__LONG,8)");
                QueryResponse queryResponse = solrServer.query(solrQuery);
//                log.debug("queryResponse = " + queryResponse.getResults().size());
//                log.debug("queryResponse = " + queryResponse.getResults().getNumFound());
//                log.debug("queryResponse = " + queryResponse.getResults().);
                int size = queryResponse.getResults().size();
                for(int i = 0; i < size; i++){
                    SolrDocument entries = queryResponse.getResults().get(i);
//                    if(i == 0){
//                        Iterator<String> iterator = entries.getFieldNames().iterator();
//                        for(; iterator.hasNext() ;){
//                            String next = iterator.next();
//                            log.debug("Fieldnames:" + next);
//                        }
//                    }

                    String objectID = entries.get("objectID").toString();
                    if(entries.containsKey("score")){
                        log.debug("objectID("+ objectID +") has score("+ entries.get("score")+")reward("+ entries.get("_Post_rewardLevelBoost__INT") +"),("+ entries.get("_Post_category__ID_Category_dailyDecay__DOUBLE") +"), ("+ entries.get("_Post_pageViewCounter__ID_Counter_count__LONG") +")");
                    }


                    T t = MQL.selectByID(selectClass, objectID);
                    if(t == null){
                        log.error("We have a problem with the sync between the DB & Solr ... Can't find objectID("+ objectID +") class("+ selectClass +")");
                    } else {
                        toReturn.add(t);
                    }
                }
                return (NList<T>) Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class[]{NList.class},
                        new NListImpl(queryResponse, toReturn));

            } catch (SolrServerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return null;
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
            if (methodName.equals("mockExtra_getSourceClass")) {
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
                String argString = Strings.separateBy(args, ", ");
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
        //log.debug("MQ.getSourceAttributePair:1");
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
        //log.debug("MQ.getJoinsByMockCallSequence:1");
        LinkedList<Pair<Object, Method>> mockSequence = threadMockCallSequenceMap.get(Thread.currentThread());
        if (mockSequence == null) {
            throw new RuntimeException("Did you mix up MQL and NQL???? ... Or did you call 2 mock-methods in the same statement? .... Some mock calls are expected to have been made at this time Thread.currentThread().getId("+ Thread.currentThread().getId() +")");
        }
        List<Pair<Class, String>> joints = new ArrayList<Pair<Class, String>>();
        for (Pair<Object, Method> pair: mockSequence) {
            //log.debug("MQ.getJoinsByMockCallSequence:2");
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
        value = (value == null || value.trim().equals("") ? "*" : value);
        SolrExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, value);
        return new SolrConstraint(expression, joints);
    }



    public static Constraint has(Enum mockValue, Comp comp, Enum value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        SolrExpression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), comp, "" + value);
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
                log.debug("Will add subQuery("+ subQuery +") with " + SolrOperator.name(condition));
                if(subQuery != null){
                    builder.append(subQuery);
                    if(expressions.size() > 1 && i + 1 < expressions.size()){
                        builder.append(" " + SolrOperator.name(condition) + " ");
                    }
                }
            }
            if(builder.length() > 2){
                return "(" + builder.toString() + ")";
            } else {
                return builder.toString();
            }
        }
    }


    private static String removeFunnyChars(String s){
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
            String[] ss = s.split(" ");
            StringBuilder toReturn = new StringBuilder();
            for(int j = 0; j < ss.length; j++){
                boolean cleanWord = true;
                for(int i = 0; cleanWord && i < noWords.length; i++){
                    if(ss[j].equalsIgnoreCase(noWords[i])){
                        cleanWord = false;
                   }
                }
                if(cleanWord){
                    toReturn.append(ss[j]);
                    toReturn.append(' ');
                }
            }

            s = toReturn.toString();
            s = (s == null || s.trim().equals("") ? "*" : s);
            log.debug("END: removeFunnyChars returns input ("+ s +")");
            return s;


        }

    }


    public static void main(String[] args) {
        System.out.println(removeFunnyChars("(zzz k||k ^asdasd asd asd as d adsf!!!! ^not"));
    }


    private static String createSearchString(String textQuery) {
        String cleanText = removeFunnyChars(textQuery);
//        if(!cleanText.equals("")){
//            StringTokenizer toks = new StringTokenizer(cleanText, " ");
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
            this.statement = "("+ attributeName +":("+ createSearchString(value) +"))";
            this.attr = attributeName;
            this.value = value;
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
            log.debug("addConstrain:int("+ value +")");
            this.statement = "("+ attributeName +":"+ value +")";
            this.attr = attributeName;
            this.value = "" + value;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, double value) {
            log.debug("addConstrain:double("+ value +")");
            this.statement = "("+ attributeName +":"+ value +")";
            this.attr = attributeName;
            this.value = "" + value;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, float value) {
            log.debug("addConstrain:float("+ value +")");
            this.statement = "("+ attributeName +":"+ value +")";
            this.attr = attributeName;
            this.value = "" + value;
            this.comparator = comparator;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, long value) {
            log.debug("addConstrain:long("+ value +")");
            this.statement = "("+ attributeName +":"+ value +")";
            this.attr = attributeName;
            this.value = "" + value;
            this.comparator = comparator;
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
            if(this.comparator == Comp.EQUAL_OR_LESS){
                return " (" + solrAttributeName + ":[* TO " + value + "]"+ boostQuery +")" + otherFunctions;
            } else if(this.comparator == Comp.EQUAL_OR_GREATER){
                return " (" + solrAttributeName + ":[" + value + " TO *]"+ boostQuery +")" + otherFunctions;
            } else if(this.comparator == Comp.NOT_EQUAL){
                return " (" + solrAttributeName + ":-(" + value + ")"+ boostQuery +")" + otherFunctions;
            } else {
                return " (" + solrAttributeName + ":(" + removeFunnyChars(value) + ")"+ boostQuery +")" + otherFunctions;
            }

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
        abstract UpdateSolrQueryAble getExpression();
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



        UpdateSolrQueryAble getExpression() {
            SolrContainerExpression container = new SolrContainerExpression();
            for (Constraint c: constraints) {
                log.debug("getExpression() with operator.operator("+ operator.name() +")");
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
        SolrExpression getExpression() {
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
        UpdateSolrQueryAble getExpression() {
            if (! hasConstraints()) {
                return new SolrExpression();
            }
            return super.getExpression();
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

package dk.lessismore.reusable_v4.db.methodquery;

import dk.lessismore.reusable_v4.db.statements.*;
import dk.lessismore.reusable_v4.db.statements.mysql.MySqlUtil;
import dk.lessismore.reusable_v4.reflection.db.DbClassReflector;
import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttribute;
import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.reusable_v4.utils.Pair;
import dk.lessismore.reusable_v4.utils.Strings;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
* Created with IntelliJ IDEA.
* User: seb
*/
public class SQ {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SQ.class);

    public enum Comp {EQUAL, EQUAL_OR_GREATER, EQUAL_OR_LESS, GREATER, LESS, NOT_EQUAL, LIKE, NOT_LIKE}
    public enum Order {ASC, DESC}
    public static final int ANY = 0;
    public enum ReadOnly {RO, RW}
    public enum SolrOperator {OR(0), AND(1);
        final int operator;
        SolrOperator(int i){
            this.operator = i;
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

    public static String asString(Object mockValue){
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        String attribute = makeAttributeIdentifier(pair);
        attribute = attribute.replaceAll("_", "");
        return attribute;
    }


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


        /**
         * Add where constraint
         * @param mockValue a proxy instance
         * @param comp comparator
         * @param value compareWithNullLast value
         * @return this
         */
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

//        public SearchQuery<T>  whereIn(ModelObjectInterface mockValue, ModelObjectInterface[] values) {
//            if(values == null) return this;
//            ArrayList<String> strs = new ArrayList<String>();
//            for(int i = 0; i < values.length; i++){
//                if(values[i] != null){
//                    strs.add(values[i].getObjectID());
//                }
//            }
//            return whereIn(mockValue.getObjectID(), strs.toArray(new String[strs.size()]));
//        }
//
//
//        public SearchQuery<T>  whereIn(String mockValue, String ... values) {
//            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
//            Pair<Class, String> pair = getSourceAttributePair();
//            clearMockCallSequence();
//            String attribute = makeAttributeIdentifier(pair);
//            Constraint[] constraints = new Constraint[values.length];
//            for(int i = 0; i < values.length; i++){
//                Expression expression = newLeafExpression().addConstrain(attribute, Comp.EQUAL, values[i]);
//                constraints[i] = new SolrConstraint(expression, i == 0 ? joints : new ArrayList<Pair<Class, String>>());
//            }
//            rootConstraints.add(new AnyConstraint(constraints));
//            return this;
//        }
//
//        public SearchQuery<T>  whereIn(Enum mockValue, Enum ... values) {
//            if (values == null || values.length == 0) {
//                return this;
//            }
//            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
//            Pair<Class, String> pair = getSourceAttributePair();
//            clearMockCallSequence();
//            String attribute = makeAttributeIdentifier(pair);
//            Constraint[] constraints = new Constraint[values.length];
//            for (int i = 0; i < values.length; i++) {
//                //constraints[i] = has(mockValue, Comp.EQUAL, values[i]);
//                Expression expression = newLeafExpression().addConstrain(attribute, Comp.EQUAL, values[i].toString());
//                constraints[i] = new SolrConstraint(expression, joints);
//            }
//            rootConstraints.add(new AnyConstraint(constraints));
//            return this;
//        }

        public SearchQuery<T> search(Calendar mockValue, Comp comp, Calendar value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SearchQuery<T> search(double mockValue, Comp comp, double value) {
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
//        public SearchQuery<T> orderBy(int mockValue, Order order) {
//            return orderBy(order);
//        }
//        public SearchQuery<T> orderBy(String mockValue, Order order) {
//            return orderBy(order);
//        }
//        public SearchQuery<T> orderBy(Calendar mockValue, Order order) {
//            return orderBy(order);
//        }
//        public SearchQuery<T> orderBy(double mockValue, Order order) {
//            return orderBy(order);
//        }
//        public SearchQuery<T> orderBy(T mockValue, Order order) {
//            return orderBy(order);
//        }
//        private SearchQuery<T> orderBy(Order order) {
//            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
//            Pair<Class, String> pair = getSourceAttributePair();
//            clearMockCallSequence();
//            String tableName = getTableName(pair.getFirst());
//            String attributeName = pair.getSecond();
//            for (Pair<Class, String> joint: joints) {
//                joinOn(joint.getFirst(), joint.getSecond());
//            }
//            statement.setOrderBy(tableName, attributeName, orderToNum(order));
//            return this;
//        }
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


//        /**
//         * Set limit
//         * @param start start
//         * @param end end
//         * @return this
//         */
//        public SearchQuery<T> limit(int start, int end) {
//            creator.addLimit(start, end);
//            return this;
//        }
//        public SearchQuery<T> limit(int count) {
//            return limit(0, count);
//        }
//
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
        public List<T> getList() {
            List<T> list = selectObjectsFromDb();
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
//        }

        private List<T> selectObjectsFromDb() {
            List<T> toReturn = new ArrayList<T>();
            try {
                StringBuilder builder = new StringBuilder();
                solrQuery.setQuery("*:*");
                for(int i = 0; i < rootConstraints.size(); i++){
                    String subQuery = rootConstraints.get(i).getExpression().updateSolrQuery(solrQuery);
                    if(subQuery != null){
                        builder.append(subQuery);
                    }
                }
                String query = builder.toString();
                if(query == null || query.length() < 2){
                    solrQuery.setQuery("*:*");
                }
                log.debug("We will query = " + query);
                solrQuery.setQuery(query);
                SolrServer solrServer = ModelObjectSearchService.solrServer(selectClass);
                QueryResponse queryResponse = solrServer.query(solrQuery);
                log.debug("queryResponse = " + queryResponse.getResults().size());
                log.debug("queryResponse = " + queryResponse.getResults().getNumFound());
                int size = queryResponse.getResults().size();
                for(int i = 0; i < size; i++){
                    SolrDocument entries = queryResponse.getResults().get(i);
                    String objectID = entries.get("objectID").toString();
                    T t = MQ.selectByID(selectClass, objectID);
                    toReturn.add(t);
                }
            } catch (SolrServerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return toReturn;
        }

//        private int selectCountFromDb() {
//            statement.addExpression(getExpressionAddJoins());
//            return DbObjectSelector.countObjectsFromDb(statement); // The cache arguments is ignored
//        }
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
            throw new RuntimeException("Some mock calls are expected to have been made at this time Thread.currentThread().getId("+ Thread.currentThread().getId() +")");
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
            throw new RuntimeException("Some mock calls are expected to have been made at this time Thread.currentThread().getId("+ Thread.currentThread().getId() +")");
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
        return makeAttributeIdentifier(pair.getFirst(), pair.getSecond());
    }

    public static String makeAttributeIdentifier(Class sourceClass, String attributeName) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
        return dbAttribute.getSolrAttributeName();
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
        List<UpdateSolrQueryAble> expressions = new LinkedList();
        List<Integer> conditions = new LinkedList();

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
            Iterator iterator = expressions.iterator();
            StringBuilder builder = new StringBuilder();
            while (iterator.hasNext()) {
                UpdateSolrQueryAble expression = (UpdateSolrQueryAble) iterator.next();
                String subQuery = expression.updateSolrQuery(solrQuery);
                if(subQuery != null){
                    builder.append(subQuery);
                }
            }
            return builder.toString();
        }
    }


    private static String removeFunnyChars(String s){
        if(s == null || s.equals("")){
            return s;
        } else {
            return s.replaceAll("\"", " ").replaceAll("!", " ").replaceAll("'", " ").replaceAll("^", " ")
                    .replaceAll("$", " ").replaceAll("§", " ").replaceAll("#", " ").replaceAll(":", " ").replaceAll("_", " ")
                    .replaceAll("/", " ").replaceAll(";", " ").replaceAll("€", " ").replaceAll("%", " ").replaceAll("/", " ")
                    .replaceAll("\\?", " ").replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\{", " ").replaceAll("\\}", " ")
                    .replaceAll("\\[", " ").replaceAll("\\]", " ")
                    .replaceAll("<", " ").replaceAll(">", " ").replaceAll("^", " ").replaceAll("~", " ").replaceAll("\\+", " ").replaceAll("-", " ")
                    .trim();
        }

    }


    private static String createSearchString(String textQuery) {
        String cleanText = removeFunnyChars(textQuery);
        if(!cleanText.equals("")){
            StringTokenizer toks = new StringTokenizer(cleanText, " ");
            String nString = "";
            ArrayList<String> nt = new ArrayList<String>();
            while (toks.hasMoreTokens()){
                String s = toks.nextToken().toLowerCase();
                if(s.equalsIgnoreCase("AND") || s.equalsIgnoreCase("OR")){
                    nt.add(s);
                } else if(s.indexOf("*") != -1) {
                    String substring = s;
                    if(substring.startsWith("*")){
                        substring = substring.substring(1);
                    }
                    nt.add(substring);
                } else if(s.indexOf("*") == -1){
                    nt.add(s);
                } else {
                    nt.add("(" + s + ") ");
                }
            }
            for(int i = 0; i < nt.size(); i++){
                if(!nt.get(i).equalsIgnoreCase("AND") && !nt.get(i).equalsIgnoreCase("OR")){
                    nString += "("+ nt.get(i) +")" + (i + 1 < nt.size() && !nt.get(i+1).equalsIgnoreCase("AND") && !nt.get(i+1).equalsIgnoreCase("OR") ? " AND " : " ");
                } else {
                    if(i + 1 < nt.size()){
                        nString += nt.get(i).toUpperCase() + " ";
                    } else {
                        nString += "\""+ nt.get(i).toLowerCase() + "\"" + " ";
                    }
                }
            }

            return nString;
        }
        return cleanText;
    }



    public interface UpdateSolrQueryAble {


        public String updateSolrQuery(SolrQuery solrQuery);



    }




    public static class SolrExpression implements UpdateSolrQueryAble {

        String statement = "*:*";
        String attr = null;
        String value = null;


        public SolrExpression addConstrain(String attributeName, Comp comparator, String value) {
            this.statement = "("+ attributeName +":("+ createSearchString(value) +"))";
            this.attr = attributeName;
            this.value = value;
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, Calendar value) {
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, int value) {
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, double value) {
            return this;
        }

        public SolrExpression addConstrain(String attributeName, Comp comparator, float value) {
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
            return " (" + attr + ":(" + value + "))";
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

    /**
     * Constraint AST with ANDs and ORs
     */

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

        private final SolrExpression expression;
        private final List<Pair<Class, String>> joints;

        public SolrConstraint(SolrExpression expression, List<Pair<Class, String>> joints) {
            this.expression = expression;
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

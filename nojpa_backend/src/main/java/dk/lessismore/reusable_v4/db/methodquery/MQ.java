package dk.lessismore.reusable_v4.db.methodquery;

import dk.lessismore.reusable_v4.db.statements.*;
import dk.lessismore.reusable_v4.reflection.db.DbClassReflector;
import dk.lessismore.reusable_v4.reflection.db.DbObjectReader;
import dk.lessismore.reusable_v4.reflection.db.DbObjectSelector;
import dk.lessismore.reusable_v4.reflection.db.DbObjectVisitor;
import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttribute;
import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObject;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import dk.lessismore.reusable_v4.reflection.db.statements.SelectSqlStatementCreator;
import dk.lessismore.reusable_v4.utils.Pair;
import dk.lessismore.reusable_v4.utils.Strings;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Method Query - A sugar shell wrapping reusable queries.
 * Currently only select queries are supported
 */
public class MQ {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MQ.class);

    public static <T  extends ModelObjectInterface> T selectByID(Class<T> aClass, String objectID) {
//        return MQ.select(aClass).where(MQ.mock(aClass).getObjectID(), MQ.Comp.EQUAL, objectID).getFirst();
        return (T) DbObjectReader.readObjectFromDb(objectID, aClass);
    }
    public static <T  extends ModelObjectInterface> T[] selectByIDs(Class<T> aClass, String[] objectIDs) {
        return MQ.select(aClass).whereIn(MQ.mock(aClass).getObjectID(), objectIDs).getArray();
    }

    public enum Comp {EQUAL, EQUAL_OR_GREATER, EQUAL_OR_LESS, GREATER, LESS, NOT_EQUAL, LIKE, NOT_LIKE}
    public enum Order {ASC, DESC}
    public static final int ANY = 0;
    public enum NoCache {NO_CACHE}

    private static Hashtable<Thread, LinkedList<Pair<Object, Method>>> threadMockCallSequenceMap =
            new Hashtable<Thread, LinkedList<Pair<Object, Method>>>();

    /**
     * Create a select query object.
     * @param sourceMock A mock object used to determine the source model.
     * @param <T> The type of the entities fetched.
     * @return self.
     */
    public static <T extends ModelObjectInterface> SelectQuery<T> select(T sourceMock) {
        clearMockCallSequence();
        return new SelectQuery<T>((Class<T>) ((MockExtra)sourceMock).mockExtra_getSourceClass());
    }
    public static <T extends ModelObjectInterface> SelectQuery<T> select(Class<T> sourceClass) {
        clearMockCallSequence();
        return new SelectQuery<T>(sourceClass);
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
                new Class[] {modelInterface, MockExtra.class},
                new MockInvocationHandler(modelInterface));
    }

    /**
     * A select query on which you can put constraints and fetch results.
     * @param <T> The model source class
     */
    public static class SelectQuery<T extends ModelObjectInterface> {

        private final Class<T> selectClass;
        private final SelectSqlStatementCreator creator;
        ArrayList<Constraint> rootConstraints;
        private final SelectSQLStatement statement;
        private boolean useCache = true;

        public SelectQuery(Class<T> selectClass) {
            this.selectClass = selectClass;
            creator = new SelectSqlStatementCreator();
            creator.setSource(selectClass);
            rootConstraints = new ArrayList<Constraint>();
            statement = creator.getSelectSQLStatement();
        }


        /**
         * Add where constraint
         * @param mockValue a proxy instance
         * @param comp comparator
         * @param value compareWithNullLast value
         * @return this
         */
        public SelectQuery<T> where(int mockValue, Comp comp, int value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SelectQuery<T> where(boolean mockValue, Comp comp, boolean value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SelectQuery<T> where(String mockValue, Comp comp, String value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SelectQuery<T> where(Enum mockValue, Comp comp, Enum value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }

        public SelectQuery<T>  whereIn(ModelObjectInterface mockValue, ModelObjectInterface[] values) {
            if(values == null) return this;
            ArrayList<String> strs = new ArrayList<String>();
            for(int i = 0; i < values.length; i++){
                if(values[i] != null){
                    strs.add(values[i].getObjectID());
                }
            }
            return whereIn(mockValue.getObjectID(), strs.toArray(new String[strs.size()]));
        }


        public SelectQuery<T>  whereIn(String mockValue, String ... values) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String attribute = makeAttributeIdentifier(pair);
            Constraint[] constraints = new Constraint[values.length];
            for(int i = 0; i < values.length; i++){
                Expression expression = newLeafExpression().addConstrain(attribute, compToNum(Comp.EQUAL), values[i]);
                constraints[i] = new ExpressionConstraint(expression, i == 0 ? joints : new ArrayList<Pair<Class, String>>());
            }
            rootConstraints.add(new AnyConstraint(constraints));
            return this;
        }

        public SelectQuery<T>  whereIn(Enum mockValue, Enum ... values) {
            if (values == null || values.length == 0) {
                return this;
            }
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String attribute = makeAttributeIdentifier(pair);
            Constraint[] constraints = new Constraint[values.length];
            for (int i = 0; i < values.length; i++) {
                //constraints[i] = has(mockValue, Comp.EQUAL, values[i]);
                Expression expression = newLeafExpression().addConstrain(attribute, compToNum(Comp.EQUAL), values[i].toString());
                constraints[i] = new ExpressionConstraint(expression, joints);
            }
            rootConstraints.add(new AnyConstraint(constraints));
            return this;
        }

        public SelectQuery<T> where(Calendar mockValue, Comp comp, Calendar value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }
        
        public SelectQuery<T> where(double mockValue, Comp comp, double value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }
        public SelectQuery<T> where(long mockValue, Comp comp, long value) {
            rootConstraints.add(has(mockValue, comp, value));
            return this;
        }
        public SelectQuery<T> where(long mockValue, Comp comp, int value) {
            rootConstraints.add(has(mockValue, comp, new Long(value).longValue()));
            return this;
        }
        public <M extends ModelObjectInterface> SelectQuery<T> where(M mockValue, Comp comp, M model) {
            rootConstraints.add(has(mockValue, comp, model));
            return this;
        }

        public SelectQuery<T> where(Constraint constraint) {
            rootConstraints.add(constraint);
            return this;
        }

        // TODO seb, please review my isNull methods (by Atanas)
        public SelectQuery<T> whereIsNull(Calendar mockValue) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String attribute = makeAttributeIdentifier(pair);
            Expression expression = newLeafExpression().isNull(attribute);
            rootConstraints.add(new ExpressionConstraint(expression, joints));
            return this;
        }

        public SelectQuery<T> whereIsNotNull(Calendar mockValue) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String attribute = makeAttributeIdentifier(pair);
            Expression expression = newLeafExpression().isNotNull(attribute);
            rootConstraints.add(new ExpressionConstraint(expression, joints));
            return this;
        }

        public <M extends ModelObjectInterface> SelectQuery<T> whereIsNull(M mockValue) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String attribute = makeAttributeIdentifier(pair);
            Expression expression = newLeafExpression().isNull(attribute);
            rootConstraints.add(new ExpressionConstraint(expression, joints));
            return this;
        }

        public <M extends ModelObjectInterface> SelectQuery<T> whereIsNotNull(M mockValue) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String attribute = makeAttributeIdentifier(pair);
            Expression expression = newLeafExpression().isNotNull(attribute);
            rootConstraints.add(new ExpressionConstraint(expression, joints));
            return this;
        }

        public SelectQuery<T> whereIsNull(String mockValue) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String attribute = makeAttributeIdentifier(pair);
            Expression expression = newLeafExpression().isNull(attribute);
            rootConstraints.add(new ExpressionConstraint(expression, joints));
            return this;
        }

        public SelectQuery<T> whereIsNotNull(String mockValue) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String attribute = makeAttributeIdentifier(pair);
            Expression expression = newLeafExpression().isNotNull(attribute);
            rootConstraints.add(new ExpressionConstraint(expression, joints));
            return this;
        }

        // TODO remove - use only hasUnsafe
        public SelectQuery<T> whereUnsafe(Class<? extends ModelObjectInterface> modelInterface, String attributeName, Comp comp, String value) {
            rootConstraints.add(hasUnsafe(modelInterface, attributeName, comp, value));
            return this;
        }

        private String makeAttributeIdentifier(Pair<Class, String> sourceAttributePair) {
            return creator.makeAttributeIdentifier(sourceAttributePair.getFirst(), sourceAttributePair.getSecond());
        }

        private String makeAttributeIdentifier(Class clazz, String attributeName) {
            return creator.makeAttributeIdentifier(clazz, attributeName);
        }

        /**
         * Set order by
         * @param mockValue this value is ignored, but actual parameter is expected to be a mock call
         * @param order ascending, descending.
         * @return this.
         */
        public SelectQuery<T> orderBy(int mockValue, Order order) {
            return orderBy(order);
        }
        public SelectQuery<T> orderBy(String mockValue, Order order) {
            return orderBy(order);
        }
        public SelectQuery<T> orderBy(Calendar mockValue, Order order) {
            return orderBy(order);
        }
        public SelectQuery<T> orderBy(double mockValue, Order order) {
            return orderBy(order);
        }
        public SelectQuery<T> orderBy(T mockValue, Order order) {
            return orderBy(order);
        }
        private SelectQuery<T> orderBy(Order order) {
            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
            Pair<Class, String> pair = getSourceAttributePair();
            clearMockCallSequence();
            String tableName = getTableName(pair.getFirst());
            String attributeName = pair.getSecond();
            for (Pair<Class, String> joint: joints) {
                joinOn(joint.getFirst(), joint.getSecond());
            }
            statement.setOrderBy(tableName, attributeName, orderToNum(order));
            return this;
        }

        // Only needed for reflection and should not be public exposed
        public SelectQuery<T> orderByUnsafe(String attributeName, Order order) {
            statement.setOrderBy(attributeName, orderToNum(order));
            return this;
        }

        /**
         * Set group by
         * @param sourceInterface source class or interface
         * @param attributeName attribute name
         * @return this
         */
        public SelectQuery<T> groupBy(Class<? extends ModelObjectInterface> sourceInterface, String attributeName) {
            statement.setGroupBy(getTableName(sourceInterface), attributeName);
            return this;
        }


        /**
         * Set having
         * @param having sql having string
         * @return this
         */
        public SelectQuery<T> having(String having) {
            statement.setHaving(having);
            return this;
        }

        public int getSum(int mock){
            return (int) getSum(0L);
        }



        public float getSum(float mock){
            return (float) getSum(0d);
        }

        public long getSum(long mock){
            Pair<Class, String> sourceAttributePair = getSourceAttributePair();
            String sumAtt = creator.makeAttributeIdentifier(sourceAttributePair.getFirst(), sourceAttributePair.getSecond());
            statement.addExpression(getExpressionAddJoins());
            clearMockCallSequence();
            return DbObjectSelector.countSumFromDbAsLong(sumAtt, statement);
        }

        public double getSum(double mock){
//            List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
//            Pair<Class, String> pair = getSourceAttributePair();
//            clearMockCallSequence();
//            Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), value);
//            return new ExpressionConstraint(expression, joints);
            Pair<Class, String> sourceAttributePair = getSourceAttributePair();
            String sumAtt = creator.makeAttributeIdentifier(sourceAttributePair.getFirst(), sourceAttributePair.getSecond());
            statement.addExpression(getExpressionAddJoins());
            clearMockCallSequence();
            return DbObjectSelector.countSumFromDbAsDouble(sumAtt, statement);
        }


        /**
         * Set limit
         * @param start start
         * @param end end
         * @return this
         */
        public SelectQuery<T> limit(int start, int end) {
            creator.addLimit(start, end);
            return this;
        }
        public SelectQuery<T> limit(int count) {
            return limit(0, count);
        }


        /**
         * Return the number of results of the query
         * @return result count.
         */
        public int getCount() {
            return selectCountFromDb();
        }

        /**
         * Execute query
         * @return result list, possible empty, never null.
         */
        public List<T> getList() {
            List<T> list = selectObjectsFromDb();
            if (list != null) return list;
            else return new ArrayList<T>();
        }

        /**
         * Execute query
         * @return result array, possible empty, never null.
         */
        public T[] getArray() {
            List<T> list = getList();
            T[] array = (T[]) Array.newInstance(selectClass, list.size());
            return  list.toArray(array);
        }

        /**
         * Execute query with limit 1
         * @return result if any, else null.
         */
        public T getFirst() {
            limit(1);
            List<T> list = getList();
            if (list.isEmpty()) return null;
            else return list.get(0);
        }

        /**
         * Add join on attribute.
         * This enables one to put constraint on the class of the join Attribute.
         * Remark that this does not change the type of the elements later fetched (as in SQL).
         * @param sourceClass source class
         * @param joinAttributeName attribute name
         * @return this
         */
        private SelectQuery<T> joinOn(Class sourceClass, String joinAttributeName) {
            creator.addJoin(sourceClass, joinAttributeName);
            return this;
        }

        private Expression getExpressionAddJoins() {
            Constraint[] constraints = rootConstraints.toArray((Constraint[]) Array.newInstance(Constraint.class, rootConstraints.size()));
            AllConstraint allConstraint = new AllConstraint(constraints);
            for (Pair<Class, String> joint: allConstraint.getJoints()) {
                joinOn(joint.getFirst(), joint.getSecond());
            }
            return allConstraint.getExpression();
        }

        public void visit(DbObjectVisitor visitor){
            log.debug("Will run: DbObjectSelector.iterateObjectsFromDb(selectClass, statement, visitor)");
            statement.addExpression(getExpressionAddJoins());
            DbObjectSelector.iterateObjectsFromDb(selectClass, statement, visitor);
        }

        public SelectSQLStatement getSelectSQLStatement(){
            statement.addExpression(getExpressionAddJoins());
            return statement;
        }

        // TODO consider: are joints potentially added multiple time?? does it matter ?
        private List<T> selectObjectsFromDb() {
            statement.addExpression(getExpressionAddJoins());
            return (List<T>) DbObjectSelector.selectObjectsFromDb(selectClass, statement, useCache); // The cache arguments is ignored
        }

        private int selectCountFromDb() {
            statement.addExpression(getExpressionAddJoins());
            return DbObjectSelector.countObjectsFromDb(statement); // The cache arguments is ignored
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


    private static LeafExpression newLeafExpression() {
        return SQLStatementFactory.getLeafExpression();
    }

    public static String makeAttributeIdentifier(Pair<Class, String> pair) {
        return makeAttributeIdentifier(pair.getFirst(), pair.getSecond());
    }

    public static String makeAttributeIdentifier(Class sourceClass, String attributeName) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
        return dbAttributeContainer.getTableName()+"."+dbAttribute.getAttributeName();
    }


    /*
     * all, any, has expressions (constraints) interface
     */

    public static Constraint has(int mockValue, Comp comp, int value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), value);
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint has(boolean mockValue, Comp comp, boolean value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), value ? 1 : 0);
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint has(String mockValue, Comp comp, String value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), value);
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint hasNull(String mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().isNull(makeAttributeIdentifier(pair));
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint hasNotNull(String mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().isNotNull(makeAttributeIdentifier(pair));
        return new ExpressionConstraint(expression, joints);
    }


    public static Constraint has(Enum mockValue, Comp comp, Enum value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), "" + value);
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint has(Calendar mockValue, Comp comp, Calendar value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), value);
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint hasNull(Calendar mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().isNull(makeAttributeIdentifier(pair));
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint hasNotNull(Calendar mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().isNotNull(makeAttributeIdentifier(pair));
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint has(double mockValue, Comp comp, double value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), value);
        return new ExpressionConstraint(expression, joints);
    }

    public static Constraint has(long mockValue, Comp comp, long value) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), value);
        return new ExpressionConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint has(M mockValue, Comp comp, M model) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression;
        if (model instanceof MockExtra) {
            expression = trueExpression();
        } else {
            expression = newLeafExpression().addConstrain(makeAttributeIdentifier(pair), compToNum(comp), model.getObjectID());
        }
        return new ExpressionConstraint(expression, joints);
    }


    public static <M extends ModelObjectInterface> Constraint hasIn(Enum mockValue, Enum ... values) {
        if (values == null || values.length == 0) {
            return new AnyConstraint();
        }
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        String attribute = makeAttributeIdentifier(pair);
        Constraint[] constraints = new Constraint[values.length];
        for (int i = 0; i < values.length; i++) {
            Expression expression = newLeafExpression().addConstrain(attribute, compToNum(Comp.EQUAL), values[i].toString());
            constraints[i] = new ExpressionConstraint(expression, joints);
        }
        return new AnyConstraint(constraints);
    }

    public static <M extends ModelObjectInterface> Constraint hasIn(ModelObjectInterface mockValue, ModelObjectInterface ... values) {
        if(values == null) return new AnyConstraint();
        ArrayList<String> strs = new ArrayList<String>();
        for(int i = 0; i < values.length; i++){
            if(values[i] != null){
                strs.add(values[i].getObjectID());
            }
        }
        return hasIn(mockValue.getObjectID(), strs.toArray(new String[strs.size()]));
    }

    public static Constraint hasIn(String mockValue, String ... values) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        String attribute = makeAttributeIdentifier(pair);
        Constraint[] constraints = new Constraint[values.length];
        for(int i = 0; i < values.length; i++){
            Expression expression = newLeafExpression().addConstrain(attribute, compToNum(Comp.EQUAL), values[i]);
            constraints[i] = new ExpressionConstraint(expression, i == 0 ? joints : new ArrayList<Pair<Class, String>>());
        }
        return new AnyConstraint(constraints);
    }


    public static <M extends ModelObjectInterface> Constraint hasNull(M mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression;
        expression = newLeafExpression().isNull(makeAttributeIdentifier(pair));
        return new ExpressionConstraint(expression, joints);
    }

    public static <M extends ModelObjectInterface> Constraint hasNotNull(M mockValue) {
        List<Pair<Class, String>> joints = getJoinsByMockCallSequence();
        Pair<Class, String> pair = getSourceAttributePair();
        clearMockCallSequence();
        Expression expression;
        expression = newLeafExpression().isNotNull(makeAttributeIdentifier(pair));
        return new ExpressionConstraint(expression, joints);
    }

    // This is only needed for reflection
    public static Constraint hasUnsafe(Class<? extends ModelObjectInterface> modelInterface, String attributeName, Comp comp, String value) {
        Expression expression = newLeafExpression().addConstrain(makeAttributeIdentifier(modelInterface, attributeName), compToNum(comp), value);
        return new ExpressionConstraint(expression, Collections.<Pair<Class, String>>emptyList());
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
        abstract Expression getExpression();
        abstract public List<Pair<Class, String>> getJoints();
    }

    public abstract static class OperatorConstraint extends Constraint {
        private final Constraint[] constraints;
        private final int operator;

        protected boolean hasConstraints() {
            return constraints.length > 0;
        }

        private OperatorConstraint(int operator, Constraint... constraints) {
            this.operator = operator;
            this.constraints = constraints;
        }



        @Override
        Expression getExpression() {
            ContainerExpression container = SQLStatementFactory.getContainerExpression();
            for (Constraint c: constraints) {
                container.addExpression(operator, c.getExpression());
            }
            return container;
        }

        @Override
        public List<Pair<Class, String>> getJoints() {
            List<Pair<Class, String>> joints = new ArrayList<Pair<Class, String>>();
            for (Constraint constraint: constraints) {
                joints.addAll(constraint.getJoints());
            }
            return joints;
        }
    }

    public static class ExpressionConstraint extends Constraint {

        private final Expression expression;
        private final List<Pair<Class, String>> joints;

        public ExpressionConstraint(Expression expression, List<Pair<Class, String>> joints) {
            //log.debug("MQ$ExpressionConstraint.ExpressionConstraint joints.size() = " + (joints != null ? joints.size() : 0));
            this.expression = expression;
            this.joints = joints;
        }

        @Override
        Expression getExpression() {
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
            super(WhereSQLStatement.AND, constraints);
        }
    }

    /**
     * This constraint is true if at least one of its child constraints is true.
     * If non child constraints is given, this constraint is false.
     */
    public static class AnyConstraint extends OperatorConstraint {
        private AnyConstraint(Constraint... constraints) {
            super(WhereSQLStatement.OR, constraints);
        }

        @Override
        Expression getExpression() {
            if (! hasConstraints()) {
                return SQLStatementFactory.getLeafExpression().setToFalse();
            }
            return super.getExpression();
        }
    }

    private static Expression trueExpression() {
        return SQLStatementFactory.getContainerExpression();
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

    private static String getTableName(Class<? extends ModelObjectInterface> c) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(c);
        return dbAttributeContainer.getTableName();
    }


    /*
     * Conversions
     */

    private static int compToNum(Comp comp) {
        switch (comp) {
            case EQUAL            : return  WhereSQLStatement.EQUAL;
            case EQUAL_OR_GREATER : return  WhereSQLStatement.EQUAL_OR_GREATER;
            case EQUAL_OR_LESS    : return  WhereSQLStatement.EQUAL_OR_LESS;
            case GREATER          : return  WhereSQLStatement.GREATER;
            case LESS             : return  WhereSQLStatement.LESS;
            case NOT_EQUAL        : return  WhereSQLStatement.NOT_EQUAL;
            case LIKE             : return  WhereSQLStatement.LIKE;
            case NOT_LIKE         : return  WhereSQLStatement.NOT_LIKE;
            default               : throw new RuntimeException("Case expression not exhaustive");
        }
    }

    private static int orderToNum(Order order) {
        switch (order) {
            case ASC  : return SelectSQLStatement.ASC;
            case DESC : return SelectSQLStatement.DESC;
            default   : throw new RuntimeException("Case expression not exhaustive");
        }
    }
}
package dk.lessismore.nojpa.reflection.db;

import dk.lessismore.nojpa.db.DbDataType;
import dk.lessismore.nojpa.db.LimResultSet;
import dk.lessismore.nojpa.db.SQLStatementExecutor;
import dk.lessismore.nojpa.db.statements.CreateSQLStatement;
import dk.lessismore.nojpa.db.statements.DropSQLStatement;
import dk.lessismore.nojpa.db.statements.SQLStatement;
import dk.lessismore.nojpa.db.statements.SQLStatementFactory;
import dk.lessismore.nojpa.reflection.db.annotations.Compressed;
import dk.lessismore.nojpa.reflection.db.annotations.IgnoreFromTableCreation;
import dk.lessismore.nojpa.reflection.db.annotations.IndexClass;
import dk.lessismore.nojpa.reflection.db.annotations.IndexField;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can analyse an object and make a create sql statement that match the
 * object attributes.
 *
 * @author LESS-IS-MORE
 * @version 1.0 21-5-2
 */
public class DatabaseCreator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCreator.class);

    public DatabaseCreator() { }


    private static Map<String, String> getIndexFromClass(Class targetClass){
        LinkedHashMap<String, String> indices = new LinkedHashMap<>();
        if (!ModelObjectInterface.class.isAssignableFrom(targetClass)) {
            // it is not a ModelObjectInterface derivate
            return Collections.emptyMap();
        }
        for (Method method : targetClass.getMethods()) {
            for (Annotation anno : method.getAnnotations()) {
                if (anno.annotationType().equals(IndexField.class)) {
                    if(!method.getReturnType().isArray()) {
                        IndexField indexField = (IndexField) anno;
                        String indexName;
                        if (StringUtils.isNotEmpty(indexField.value())) {
                            indexName = indexField.value();
                        } else {
                            indexName = getIndexName(targetClass.getSimpleName(), method.getName());
                        }
                        indices.put(getFieldName(method.getName()), indexName);
                    } else {
                        log.warn("Will ignore index on " + targetClass.getSimpleName() + "." + method.getName());
                    }
                }
            }
        }
        for (Annotation classAnno : targetClass.getAnnotations()) {
            if (classAnno instanceof IndexClass) {
                for (String idx : ((IndexClass) classAnno).value()) {
                    indices.put(idx, getIndexName(targetClass.getSimpleName(), idx));
                }
            }
        }
        return indices;
    }

    public static List<SQLStatement> makeTableFromClass(Class targetClass) {
        Map<String, String> indices = getIndexFromClass( targetClass );
        String logMsg = "Will create table for class " + targetClass.getCanonicalName() + " with indices [";

        for (String index: indices.values()) {
            logMsg += " '" + index + "'";
        }
        logMsg += "]";
        log.debug(logMsg);
        return DatabaseCreator.makeTableFromClass(targetClass, indices);

    }


    public static List<SQLStatement> makeTableFromClass(Class targetClass, Map<String, String> namesToIndex) {
        List<SQLStatement> tables = new LinkedList<SQLStatement>();
        //log.debug("makeTableFromClass : 1");
        DbAttributeContainer attributeContainer = DbClassReflector.getDbAttributeContainer(targetClass);
        if (attributeContainer == null) {
            log.error("makeTableFromClass : attributeContainer = " + attributeContainer);
            return tables;
        }
        log.debug("makeTableFromClass : attributeContainer = " + attributeContainer);
        DropSQLStatement dropTable = SQLStatementFactory.getDropSQLStatement();
        dropTable.addTableName(attributeContainer.getTableName());
        //log.debug("makeTableFromClass : 3");
        tables.add(dropTable);
        //log.debug("makeTableFromClass : 4");
        CreateSQLStatement statement = SQLStatementFactory.getCreateSQLStatement();

        for (Annotation classAnno : targetClass.getAnnotations()) {
            if (classAnno instanceof Compressed) {
                statement.addCompressed(((Compressed) classAnno).key_block_size());
            }
        }


        tables.add(statement);
        //log.debug("makeTableFromClass : 5");
        statement.addTableName(attributeContainer.getTableName());

        statement.setNamesToIndex(namesToIndex);

        //Loop through the attributes, and add them one by one.
        for (Iterator iterator = attributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();) {
            DbAttribute dbAttribute = (DbAttribute) iterator.next();

            if(dbAttribute.getInlineAttributeName() != null) {
                statement.addAttribute(dbAttribute.getInlineAttributeName(), new DbDataType(dbAttribute));
            } else if (!dbAttribute.isAssociation()) {
                //This is not an association.

                String attributeName = dbAttribute.getAttributeName();
                DbDataType dataType = new DbDataType(dbAttribute);
                if (!dbAttribute.isPrimaryKey()) {
                    log.debug("makeTableFromClass : targetClass = " + targetClass);
                    log.debug("makeTableFromClass : dbAttribute.getAttributeName() = " + dbAttribute.getAttributeName());
                    log.debug("makeTableFromClass : dbAttribute.getAttribute().getDeclaredAnnotations() = " + dbAttribute.getAttribute().getDeclaredAnnotations());
                    if(dbAttribute.isTranslatedAssociation()){
                        statement.addAttribute(attributeName + "Locale", new DbDataType(DbDataType.DB_CHAR, 2));
                        statement.addAttribute(attributeName, dataType);
                    } else {
                        statement.addAttribute(attributeName, dataType);
                    }



                } else {
                    statement.addAttribute(attributeName, dataType, new int[]{CreateSQLStatement.PROPERTY_NOT_NULL});
                    statement.addPrimaryKey(attributeName);
                }
            } else {
                if (dbAttribute.isMultiAssociation()) {

                    String associationTableName = attributeContainer.getTableName() + "_" + dbAttribute.getAttributeName();
                    DropSQLStatement associationDropTable = SQLStatementFactory.getDropSQLStatement();
                    associationDropTable.addTableName(associationTableName);
                    tables.add(associationDropTable);

                    CreateSQLStatement associationTable = SQLStatementFactory.getCreateSQLStatement();
                    associationTable.addTableName(associationTableName);

                    String targetName = null;
                    if (!dbAttribute.isPrimitivArrayAssociation()) {
                        DbAttributeContainer targetDbAttributeContainer = DbClassReflector.getDbAttributeContainer(dbAttribute.getAttributeClass());
                        if (targetDbAttributeContainer == null) {
                            log.error("Could not store class: " + dbAttribute.getAttributeClass());
                        } else if (targetDbAttributeContainer.getPrimaryKeyAttribute() == null) {
                            log.error("No primary key attribute for: " + dbAttribute.getAttributeClass());
                        }

                        targetName = targetDbAttributeContainer.getPrimaryKeyAttribute().getAttributeName();

                    } else {
                        targetName = dbAttribute.getAttributeName();
                    }

                    DbDataType dataType = new DbDataType(attributeContainer.getPrimaryKeyAttribute());
                    associationTable.addAttribute(attributeContainer.getAttributeContainer().getClassName() + "_" + attributeContainer.getPrimaryKeyAttribute().getAttributeName(), dataType, new int[]{CreateSQLStatement.PROPERTY_NOT_NULL});

                    DbDataType dbDataType = null;
                    if (dbAttribute.isPrimitivArrayAssociation()) {
                        dbDataType = new DbDataType(dbAttribute);
                        dbDataType.setType(DbDataType.DB_VARCHAR);
                    } else {
                        dbDataType = new DbDataType(dbAttribute);
                    }
                    associationTable.addAttribute(/*"target_id"*/targetName, dbDataType, new int[]{CreateSQLStatement.PROPERTY_NOT_NULL});
                    //associationTable.addPrimaryKey(/*"source_id"*/attributeContainer.getPrimaryKeyAttribute().getAttributeName());
                    associationTable.addPrimaryKey(attributeContainer.getAttributeContainer().getClassName() + "_" + attributeContainer.getPrimaryKeyAttribute().getAttributeName());
                    associationTable.addPrimaryKey(/*"target_id"*/targetName);
                    tables.add(associationTable);
                } else {
                    if(!dbAttribute.isInlineInterface()) {
                        statement.addAttribute(dbAttribute.getAttributeName(), new DbDataType(dbAttribute));
                    }
                }
            }

        }
        //log.debug("makeTableFromClass : ending");
        return tables;
    }



    public static void alterTableToThisClass(Class targetClass) throws Exception {

        log.debug("alterTableToThisClass (version:2011mar26) : " + targetClass);

        DbAttributeContainer attributeContainer = DbClassReflector.getDbAttributeContainer(targetClass);
        LimResultSet limSet = SQLStatementExecutor.doQuery("select * from " + attributeContainer.getTableName() + " limit 0,1");

        if (limSet == null) {
            log.debug("No table for this class ... Creating new " + targetClass);
            List list = makeTableFromClass(targetClass, Collections.emptyMap());
            Iterator iterator = list.iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                String tableStr = ((SQLStatement) iterator.next()).makeStatement();
                log.debug(i + " Creating table: " + tableStr);
                SQLStatementExecutor.doUpdate(tableStr);
            }
            return;
        }
        ResultSet resultSet = limSet.getResultSet();
        ResultSetMetaData metaData = resultSet.getMetaData();
        HashMap names = new HashMap();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            names.put(metaData.getColumnName(i), "" + i);
        }

        for (Iterator iterator = attributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();) {
            DbAttribute dbAttribute = (DbAttribute) iterator.next();
            log.debug("dbAttribute.getAttributeName():" + dbAttribute.getAttributeName());
            if (!dbAttribute.isAssociation()) {
                //This is not an association.

                String attributeName = dbAttribute.getAttributeName();
//                DbDataType dataType = new DbDataType(dbAttribute.getAttributeClass());
                DbDataType dataType = new DbDataType(dbAttribute);

                log.debug("alterTableToThisClass : attributeName=" + attributeName);
                log.debug("alterTableToThisClass : dataType=" + dataType);
                int cCount = -1;
                try {
                    cCount = Integer.parseInt("" + names.get(attributeName));
                } catch (NumberFormatException e) {
                }
                log.debug("alterTableToThisClass : metaData - TYPE =" + (cCount != -1 ? metaData.getColumnTypeName(cCount) : "UNKNOWED"));
                if ((("" + dataType).indexOf(cCount != -1 ? metaData.getColumnTypeName(cCount) : "UNKNOWED") != -1 || (dataType.toString().equals("INT") && ((cCount != -1 ? metaData.getColumnTypeName(cCount) : "UNKNOWED").equals("LONG") || (cCount != -1 ? metaData.getColumnTypeName(cCount) : "UNKNOWED").startsWith("INT"))))) {
                    log.debug("alterTableToThisClass : gooood");
                } else if (cCount == -1) {
                    log.debug("alterTableToThisClass : bad!!! ... have to add this!! " + attributeName + " (dataType=" + dataType + ")");
                    SQLStatementExecutor.doUpdate("ALTER TABLE " + attributeContainer.getTableName() + " ADD " + attributeName + " " + dataType);
                } else {
                    log.error("alterTableToThisClass : bad!!! ... have to delete and then add this!! " + attributeName + " (dataType=" + dataType + " - " + metaData.getColumnTypeName(cCount) + ")");
                    SQLStatementExecutor.doUpdate("ALTER TABLE " + attributeContainer.getTableName() + " DROP " + attributeName);
                    SQLStatementExecutor.doUpdate("ALTER TABLE " + attributeContainer.getTableName() + " ADD " + attributeName + " " + dataType);
                }

            } else {
                if (dbAttribute.isMultiAssociation()) {

                    String associationTableName = attributeContainer.getTableName() + "_" + dbAttribute.getAttributeName();
                    LimResultSet limSet2 = SQLStatementExecutor.doQuery("select * from " + associationTableName + " limit 0,1");
                    ResultSet resultSetAss = limSet2 != null ? limSet2.getResultSet() : null;
                    if (resultSetAss != null) {
                        log.debug("Goood " + associationTableName);
                    } else {
                        log.debug("bad ... No table for this class ... Creating new " + associationTableName);
                        CreateSQLStatement associationTable = SQLStatementFactory.getCreateSQLStatement();
                        associationTable.addTableName(associationTableName);

                        String targetName = null;
                        if (!dbAttribute.isPrimitivArrayAssociation()) {
                            DbAttributeContainer targetDbAttributeContainer = DbClassReflector.getDbAttributeContainer(dbAttribute.getAttributeClass());
                            targetName = targetDbAttributeContainer.getPrimaryKeyAttribute().getAttributeName();

                        } else {
                            targetName = dbAttribute.getAttributeName();
                        }


                        associationTable.addAttribute(attributeContainer.getAttributeContainer().getClassName() + "_" + attributeContainer.getPrimaryKeyAttribute().getAttributeName(), new DbDataType(DbDataType.DB_VARCHAR), new int[]{CreateSQLStatement.PROPERTY_NOT_NULL});

                        DbDataType dbDataType = new DbDataType(dbAttribute);
                        if (!dbAttribute.isPrimitivArrayAssociation()) {
                            dbDataType.setType(DbDataType.DB_VARCHAR);
                        } else {
                            dbDataType.setType(dbAttribute.getAttributeClass());
                        }
                        associationTable.addAttribute(/*"target_id"*/targetName, dbDataType, new int[]{CreateSQLStatement.PROPERTY_NOT_NULL});

                        associationTable.addPrimaryKey(attributeContainer.getAttributeContainer().getClassName() + "_" + attributeContainer.getPrimaryKeyAttribute().getAttributeName());
                        associationTable.addPrimaryKey(/*"target_id"*/targetName);
                        List tables = new LinkedList();
                        SQLStatementExecutor.doUpdate(associationTable.makeStatement());

                    }
                    if (limSet2 != null) {
                        limSet2.close();
                    }
                    limSet2 = null;
                } else {
                    String columnTypeName = dbAttribute.getAttributeName() == null
                            ? null
                            : (names.get(dbAttribute.getAttributeName()) != null ? metaData.getColumnTypeName(Integer.parseInt("" + names.get(dbAttribute.getAttributeName()))) : null);
                    if (columnTypeName != null &&
                            (columnTypeName.startsWith("VARCHAR") || columnTypeName.startsWith("CHAR"))) {
                        log.debug("Goood for association " + dbAttribute.getAttributeName());

                    } else {
                        log.debug("bad for association " + dbAttribute.getAttributeName());
                        if (names.get(dbAttribute.getAttributeName()) != null) {
                            SQLStatementExecutor.doUpdate("ALTER TABLE " + attributeContainer.getTableName() + " DROP " + dbAttribute.getAttributeName());
                        }
                        SQLStatementExecutor.doUpdate("ALTER TABLE " + attributeContainer.getTableName() + " ADD " + dbAttribute.getAttributeName() + " VARCHAR(250)");

                    }
                }
            }
        }

        log.debug("WE are NOT recreating indexes .... Do this in the hand");
//        String[] indexs = getIndexFromClass(targetClass);
//        for(int i = 0; i < indexs.length; i++){
//            String tableName = "_" + targetClass.getSimpleName();
//            String index = indexs[i];
//            String indexName = (tableName + index).replaceAll(" |,", "");
//            SQLStatementExecutor.doUpdateAndIgnoreExceptions("CREATE INDEX " + indexName + " ON " + tableName + " ("+ index +")");
//        }
//
        limSet.close();
        limSet = null;
    }


    public static void alterDatabase(String rootPackage) throws Exception {
        ArrayList<Class> subTypesList = getSubtypes(rootPackage);
        for(int i = 0; i < subTypesList.size(); i++){
            alterTableToThisClass(subTypesList.get(i));
        }
    }

    public static void alterDatabase(Class clazz) throws Exception {
        ArrayList<Class> subTypesList = getSubtypes(clazz);
        for(int i = 0; i < subTypesList.size(); i++){
            alterTableToThisClass(subTypesList.get(i));
        }
    }

    public static void checkDatabase(String rootPackage) {
        ArrayList<Class> subTypesList = getSubtypes(rootPackage);
        //TODO:  check the Database(subTypesList);
    }

    public static void createDatabase(String rootPackage) {
        createDatabase(rootPackage, null);
    }
    public static void createDatabase(Class clazz) {
        createDatabase(clazz, null);
    }


    public static void createDatabase(String rootPackage, Class[] ignores) {
        ArrayList<Class> subTypesList = getSubtypes(rootPackage);
        createDatabase(subTypesList, ignores);
    }

    public static void createDatabase(Class clazz, Class[] ignores) {
        ArrayList<Class> subTypesList = getSubtypes(clazz);
        createDatabase(subTypesList, ignores);
    }

    public static ArrayList<Class> getSubtypes(String rootPackage) {
        log.debug("------------ Getting all children of MOI from package: " + rootPackage);
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(rootPackage)))
                .setUrls(ClasspathHelper.forPackage(rootPackage))
                .setScanners(new SubTypesScanner(),
                        new TypeAnnotationsScanner(),
                        new ResourcesScanner()));
        ArrayList<Class> annoList = new ArrayList<Class>();
        annoList.addAll(reflections.getTypesAnnotatedWith(IgnoreFromTableCreation.class, true));
        log.debug("annoList.size()::" + annoList.size());
        ArrayList<Class> subTypesList = new ArrayList<Class>();
        subTypesList.addAll(reflections.getSubTypesOf(ModelObjectInterface.class));
        log.debug("getSubtypes: removing because it's annotated with @IgnoreFromTableCreation clazz = " + annoList);
        subTypesList.removeAll(annoList);
        log.debug("getSubtypes: found types: " + subTypesList);
        return subTypesList;
    }

    public static ArrayList<Class> getSubtypes(Class rootClass) {
        log.debug("------------ Getting all children of MOI for class: " + rootClass.getCanonicalName());
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClass(rootClass))
                .setScanners(new SubTypesScanner(),
                        new TypeAnnotationsScanner(),
                        new ResourcesScanner()));
        ArrayList<Class> annoList = new ArrayList<Class>();
        annoList.addAll(reflections.getTypesAnnotatedWith(IgnoreFromTableCreation.class, true));
        log.debug("annoList.size()::" + annoList.size());
        ArrayList<Class> subTypesList = new ArrayList<Class>();
        subTypesList.addAll(reflections.getSubTypesOf(ModelObjectInterface.class));
        log.debug("getSubtypes: removing because it's annotated with @IgnoreFromTableCreation clazz = " + annoList);
        subTypesList.removeAll(annoList);
        log.debug("getSubtypes: found types: " + subTypesList);
        return subTypesList;
    }


    public static void createDatabase(List<Class> clazzes) {
        createDatabase(clazzes, null);
    }

    public static void createDatabase(List<Class> clazzes, Class[] ignores) {
        log.debug("********* re/createDatabase ***************** - START");
		List<SQLStatement> tables = new LinkedList<SQLStatement>();
		for(Class clazz : clazzes) {
            boolean ignore = false;
            for(int i = 0; !ignore && ignores != null && i < ignores.length; i++){
                ignore = clazz.equals(ignores[i]);
            }
			tables.addAll(DatabaseCreator.makeTableFromClass(clazz));
		}
        for (SQLStatement sqlStatement : tables) {
            String tableStr = sqlStatement.makeStatement();
            SQLStatementExecutor.doUpdate(tableStr);
        }
        log.debug("********* re/createDatabase ********************* - DONE");
    }

    private static String getFieldName(String methodName) {
		return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
	}

    private static String getIndexName(String tableName, String idx) {
        String indexName = "_";
        String indexFullName = tableName + "," + idx;
        Pattern p = Pattern.compile("[A-Z ,]{1}[a-z]*");
        Matcher m = p.matcher(indexFullName);
        while(m.find()) {
            String group = m.group().replaceAll(" |,", "");
            if (group.length() > 3) {
                indexName += group.substring(0, 3);
            } else {
                indexName += group;
            }
        }
        return indexName;
    }

    public static void main(String[] args) throws Exception {
        DatabaseCreator.checkDatabase(args[0]);
    }
}







package dk.lessismore.nojpa.reflection.db;

import dk.lessismore.nojpa.reflection.db.attributes.*;
import dk.lessismore.nojpa.db.statements.*;
import dk.lessismore.nojpa.reflection.db.statements.*;
import dk.lessismore.nojpa.db.*;
import dk.lessismore.nojpa.reflection.db.model.*;
import java.util.*;
import java.sql.*;

/**
 * This class can execute an select sql statement; and load each of the objects which
 * is selected by the sql request. The sql statement must select the primary key; of
 * just one table/modelObject; but the where statement can contain a number of
 * joins; expressions etc. The class uses the <tt>DbObjectReader</tt> to read
 * the different objects.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class DbObjectSelector {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DbObjectSelector.class);

    public static void iterateObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement, DbObjectVisitor visitor) {
        log.debug("Will run: iterateObjectsFromDb");
        iterateObjectsFromDb(targetClass, selectSqlStatement, new AssociationConstrain(), true, true, 0,0, visitor);
    }

    public static <T> T[] asDistinctArrray(List list, Class<T> typeOfArray){
        if(list == null || list.isEmpty()) return null;
        HashSet set = new HashSet();
        set.addAll(list);
        T[] objects = (T[]) set.toArray((T[]) java.lang.reflect.Array.newInstance(typeOfArray, set.size()));
        return objects;
    }



    public static void iterateObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement, AssociationConstrain associationConstrain, boolean cache, boolean loadAll, int intervalStart, int intervalEnd, DbObjectVisitor visitor) {
        log.debug("Now running the REAL visitor :-) iterateObjectsFromDb(..., cache("+ cache +"), loadAll("+ loadAll +"), intervalStart("+ intervalStart +"), intervalEnd("+ intervalEnd +"), visitor("+ visitor.getClass().getSimpleName() +")) ");
        LimResultSet limSet = null;
        try {
            if (!ModelObjectInterface.class.isAssignableFrom(targetClass)) {
                log.fatal("This is not a model object. We can not continue.");
                return;
            }
            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(targetClass);
            String sqlNameQuery = null;
            if (dbAttributeContainer.getSqlNameQuery() == null) {
                for (Iterator iterator = dbAttributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();)
                {
                    DbAttribute dbAttribute = (DbAttribute) iterator.next();
                    //If the attribute is not an multi association.
                    if (!dbAttribute.isMultiAssociation())
                        sqlNameQuery = (sqlNameQuery == null ? "" : sqlNameQuery + ", ") + (dbAttributeContainer.getTableName() + "." + dbAttribute.getAttributeName());
                }
                dbAttributeContainer.setSqlNameQuery(sqlNameQuery);
            } else {
                sqlNameQuery = dbAttributeContainer.getSqlNameQuery();
            }
            selectSqlStatement.addAttributeName(sqlNameQuery);
            // ****************************************
            limSet = SQLStatementExecutor.doQuery(selectSqlStatement);
            ResultSet resultSet = limSet != null ? limSet.getResultSet() : null;
            log.debug("iterateObjectsFromDb :: 1");
            if (resultSet != null) {
                try {
                    log.debug("iterateObjectsFromDb :: 2");
                    //Load each of the objects.
                    for (int i = 0; !visitor.getDone() && resultSet.next(); i++) {
                        log.debug("iterateObjectsFromDb :: 3");
                        if (loadAll || (i >= intervalStart && i < intervalEnd)) {
                            String objectId = resultSet.getString(dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName());

                            ModelObject modelObject = DbObjectReader.readObjectFromDb(objectId, targetClass, associationConstrain, limSet);
                            log.debug("iterateObjectsFromDb :: 4");
                            if (modelObject != null){
                                log.debug("Now calling the visitor.visit");
                                visitor.visit(modelObject);

                            }
                        } else if (!loadAll && i >= intervalEnd)
                            break;
                    }
                } catch (Exception e) {
                    log.error("iterateObjectsFromDb: (1) Corrupt result set from db in object selector or visitor exception for: " + targetClass.getName(), e);
                }
            }
        } catch (Exception e) {
            log.error("iterateObjectsFromDb:Some error " + e, e);
        } finally {
            if (limSet != null) limSet.close();
            log.debug("will call visit.setDone true");
            visitor.setDone(true);
        }

    }


    
    public static List selectObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement, int startInterval, int endInterval) {
        return selectObjectsFromDb(targetClass, selectSqlStatement, new AssociationConstrain(), true, false, startInterval, endInterval);
    }
    public static List selectObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement) {
        return selectObjectsFromDb(targetClass, selectSqlStatement, new AssociationConstrain(), true, true, 0,0);
    }
    public static List selectObjectsFromDb(Class targetClass, String rawSqlStatement) {
        return selectObjectsFromDb(targetClass, rawSqlStatement, new AssociationConstrain(), true, true, 0,0);
    }
    public static List selectObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement, boolean cache, int startInterval, int endInterval) {
        return selectObjectsFromDb(targetClass, selectSqlStatement, new AssociationConstrain(), cache, false, startInterval, endInterval);
    }
    public static List selectObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement, boolean cache) {
        return selectObjectsFromDb(targetClass, selectSqlStatement, new AssociationConstrain(), cache, true, 0,0);
    }

    public static List selectObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement, AssociationConstrain associationConstrain) {
        return selectObjectsFromDb(targetClass, selectSqlStatement, associationConstrain, true, true, 0,0);
    }

    public static List selectObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement, AssociationConstrain associationConstrain, boolean cache, boolean loadAll, int intervalStart, int intervalEnd) {
        //log.debug("selectObjectsFromDb:1.0");
    LimResultSet limSet  = null;
	try{
	    List selectedObjects = new ArrayList(128);

	    if(!ModelObjectInterface.class.isAssignableFrom(targetClass)) {
		//This is not a model object. We can not continue.
		    return selectedObjects;
	    }
        //log.debug("selectObjectsFromDb:1.1");
	    DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(targetClass);
	    String sqlNameQuery = null;
        //log.debug("selectObjectsFromDb:1.2");
        if(dbAttributeContainer.getSqlNameQuery() == null){
            //NEW ************************************
	        //log.debug("selectSqlStatement.makeStatement() = ");
            //log.debug("selectObjectsFromDb:1.3");
            for (Iterator iterator = dbAttributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();) {
                DbAttribute dbAttribute = (DbAttribute) iterator.next();
                //If the attribute is not an multi association.
                if(!dbAttribute.isMultiAssociation()){
                    sqlNameQuery = (sqlNameQuery == null ? "" : sqlNameQuery + ", ") + (dbAttributeContainer.getTableName() + "." + dbAttribute.getAttributeName());
                }
            }
            //log.debug("selectObjectsFromDb:1.4");
            dbAttributeContainer.setSqlNameQuery(sqlNameQuery);
            //log.debug("selectObjectsFromDb:1.5");
        } else {
		    sqlNameQuery = dbAttributeContainer.getSqlNameQuery();
	    }
        //log.debug("selectObjectsFromDb:1.6");
        selectSqlStatement.addAttributeName(sqlNameQuery);
        //log.debug("selectObjectsFromDb:1.7");
        // ****************************************
	    limSet = SQLStatementExecutor.doQuery(selectSqlStatement);
//        log.debug("selectObjectsFromDb:1.8 targetClass("+ targetClass +") limSet("+ limSet +") loadAll("+ loadAll +") intervalStart("+ intervalStart +") intervalEnd("+ intervalEnd +")");
        ResultSet resultSet = limSet != null ? limSet.getResultSet() : null;
	    //log.debug("selectSqlStatement :: 2");
	    if(resultSet != null) {
            try {
//                log.debug("selectObjectsFromDb :: 1.9");
                //Load each of the objects.
                for(int i = 0; resultSet.next(); i++) {
//                    log.debug("selectObjectsFromDb :: 2.0");
                    if(loadAll || (i >= intervalStart && i < intervalEnd) ) {
                        String objectId = resultSet.getString(dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName());
//                        log.debug("selectObjectsFromDb:2.1: targetClass("+ targetClass +") objectId("+ objectId +")");
                        ModelObject modelObject = DbObjectReader.readObjectFromDb(objectId, targetClass, associationConstrain, limSet);
                        //log.debug("selectSqlStatement :: 5");
//                        log.debug("selectObjectsFromDb:2.2: targetClass("+ targetClass +") modelObject("+ modelObject +")");
                        if(modelObject != null){
                            selectedObjects.add(modelObject);
                        }
                    } else if(!loadAll && i >= intervalEnd) {
                        break;
                    }
                }
            }catch(Exception e) {
                log.error("selectObjectsFromDb: (2) Corrupt result set from db in object selector " +targetClass.getName(), e);
            }
	    }
        //log.debug("selectSqlStatement :: 6");
	    return selectedObjects;
	} catch(Exception e){
	    log.error("Some error "+ e, e);
	    return  new ArrayList();
	} finally {
	    if(limSet != null) limSet.close();	
	}

    }
    
    public static List selectObjectsFromDb(Class targetClass, String rawSqlStatement, AssociationConstrain associationConstrain, boolean cache, boolean loadAll, int intervalStart, int intervalEnd) {
	LimResultSet limSet = null;
	try{
	
	    List selectedObjects = new ArrayList();

	    if(!ModelObject.class.isAssignableFrom(targetClass)) {
		//This is not a model object. We can not continue.
		return selectedObjects;
	    }

	    DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(targetClass);

	    limSet = SQLStatementExecutor.doQuery(rawSqlStatement);
	    ResultSet resultSet = limSet.getResultSet();

	    if(resultSet != null) {
		try {
		    //Load each of the objects.
		    for(int i = 0; resultSet.next(); i++) {
			if(loadAll || (i >= intervalStart && i < intervalEnd) ) {
			    String objectId = resultSet.getString(dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName());

			    ModelObject modelObject = DbObjectReader.readObjectFromDb(objectId, targetClass, associationConstrain);
			    if(modelObject != null)
				selectedObjects.add(modelObject);
			}
			else if(!loadAll && i >= intervalEnd)
			    break;
		    }
		}catch(Exception e) {
		    log.warn("selectObjectsFromDb: (3) Corrupt result set from db in object selector " +targetClass.getName(), e);
		}
	    }
	    return selectedObjects;
	} catch(Exception e){
	    log.error("Some error "+ e, e);
	    return new ArrayList();
	} finally {
	    if(limSet != null) limSet.close();
	}
    }
    
    public static int countObjectsFromDb(Class targetClass) throws SQLException {
        SelectSqlStatementCreator selectSqlStatementCreator = new SelectSqlStatementCreator();
        selectSqlStatementCreator.addTable(targetClass);
        return countObjectsFromDb(selectSqlStatementCreator.getSelectSQLStatement() );
    }

    public static int countObjectsFromDb(SelectSQLStatement selectSqlStatement) {
        LimResultSet limSet = null;
        try{

            String countAttribute = "count(*) as nr";
            selectSqlStatement.addAttributeName(countAttribute);
            limSet = SQLStatementExecutor.doQuery(selectSqlStatement);
            ResultSet resultSet = limSet.getResultSet();
            selectSqlStatement.removeAttributeName(countAttribute);
            if(resultSet != null && resultSet.next()) {
                try {
                    return resultSet.getInt("nr");
                }catch(Exception e) {
                    log.error("Count objects failed", e);
                    return 0;
                }
            }

            else {
                log.warn("Resultset was null");
                return 0;
            }
        } catch(Exception e){
            log.error("countObjectsFromDb: "+ e, e);
            e.printStackTrace();
            throw new RuntimeException("countObjectsFromDb:" +  e);
        } finally {
            if(limSet != null) limSet.close();
        }
    }

    public static double countSumFromDbAsDouble(String sumAttribute, SelectSQLStatement selectSqlStatement) {
        LimResultSet limSet = null;
        try{
            String countAttribute = "sum("+ sumAttribute +") as nr";
            selectSqlStatement.addAttributeName(countAttribute);
            limSet = SQLStatementExecutor.doQuery(selectSqlStatement);
            ResultSet resultSet = limSet.getResultSet();
            selectSqlStatement.removeAttributeName(countAttribute);
            if(resultSet != null && resultSet.next()) {
                try {
                    return resultSet.getDouble("nr");
                }catch(Exception e) {
                    log.error("Count objects failed", e);
                    return 0;
                }
            } else {
                log.warn("Resultset was null");
                return 0;
            }
        } catch(Exception e){
            log.error("countSumFromDb: "+ e, e);
            e.printStackTrace();
            throw new RuntimeException("countSumFromDb:" +  e);
        } finally {
            if(limSet != null) limSet.close();
        }
    }

    public static long countSumFromDbAsLong(String sumAttribute, SelectSQLStatement selectSqlStatement) {
        LimResultSet limSet = null;
        try{
            String countAttribute = "sum("+ sumAttribute +") as nr";
            selectSqlStatement.addAttributeName(countAttribute);
            limSet = SQLStatementExecutor.doQuery(selectSqlStatement);
            ResultSet resultSet = limSet.getResultSet();
            selectSqlStatement.removeAttributeName(countAttribute);
            if(resultSet != null && resultSet.next()) {
                try {
                    return resultSet.getLong("nr");
                }catch(Exception e) {
                    log.error("Count objects failed", e);
                    return 0;
                }
            } else {
                log.warn("Resultset was null");
                return 0;
            }
        } catch(Exception e){
            log.error("countSumFromDb: "+ e, e);
            e.printStackTrace();
            throw new RuntimeException("countSumFromDb:" +  e);
        } finally {
            if(limSet != null) limSet.close();
        }
    }

}

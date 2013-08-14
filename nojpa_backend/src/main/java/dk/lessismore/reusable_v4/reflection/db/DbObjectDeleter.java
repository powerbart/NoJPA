package dk.lessismore.reusable_v4.reflection.db;

import dk.lessismore.reusable_v4.reflection.db.attributes.*;
import dk.lessismore.reusable_v4.db.statements.*;
import dk.lessismore.reusable_v4.db.*;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObject;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

import java.util.*;
import java.sql.*;

import dk.lessismore.reusable_v4.cache.*;

/**
 * This class can delete one model object from the database, or a selection of objects.
 * The class will delete the object recursivly; which means that all associations will'
 * allso be deleted. In most cases this is not desirable; so you can specify
 * which associations must be deleted and which may not. This class allso
 * does the job about; removing the object from the cache !!
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 21-5-2
 */
public class DbObjectDeleter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DbObjectDeleter.class);

    /**
     * The method deletes the selected objects from the database recursivly (all associations
     * will be deleted as well. Be carefull. )
     *
     * @param targetClass        The class which the objects has that shall be deleted.
     * @param selectSqlStatement The sql statement that selects the objects which is to be deleted.
     */
    public static boolean deleteObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement) {
        return deleteObjectsFromDb(targetClass, selectSqlStatement, new AssociationConstrain());
    }

    /**
     * The method deletes the selected objects from the database recursivly.
     *
     * @param targetClass          The class which the objects has that shall be deleted.
     * @param selectSqlStatement   The sql statement that selects the objects which is to be deleted.
     * @param associationConstrain The associations which may be deleted or not.
     */
    public static boolean deleteObjectsFromDb(Class targetClass, SelectSQLStatement selectSqlStatement, AssociationConstrain associationConstrain) {

        boolean successfull = true;

        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(targetClass);

        LimResultSet limSet = null;

        try {
            limSet = SQLStatementExecutor.doQuery(selectSqlStatement);
            ResultSet resultSet = limSet.getResultSet();
            if (resultSet != null) {
                //Loop through all selected objects.
                while (resultSet.next()) {
                    String objectId = resultSet.getString(dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName());
                    //Delete the object.
                    successfull = successfull && deleteObjectFromDb(objectId, targetClass, associationConstrain);
                }
            }
        } catch (Exception e) {
            log.warn("Corrupt result set from db in object selector " + targetClass.getName());
            return false;
        } finally {
            if (limSet != null)
                limSet.close();

        }
        return successfull;
    }

    /**
     * This method can delete one model object from the database.
     *
     * @param modelObject          The object to delete in the database.
     * @param associationConstrain The associations which should be deleted or not.
     */
    public static boolean deleteObjectFromDb(ModelObject modelObject, AssociationConstrain associationConstrain) {
        return deleteObjectFromDb(modelObject.getPrimaryKeyValue(), modelObject.getInterface(), associationConstrain);
    }

    /**
     * This method can delete one model object from the database.
     * NB: everything will be deleted recurivly; which means that all associations will
     * allso be deleted from the database. Be carefull.
     */
    public static boolean deleteObjectFromDb(ModelObject modelObject) {
        return deleteObjectFromDb(modelObject.getPrimaryKeyValue(), modelObject.getInterface());
    }

    /**
     * @param objectId             The primary key of the object to delete.
     * @param targetClass          The class of the object to delete.
     * @param associationConstrain The associations which may be deleted or not.
     */
    public static boolean deleteObjectFromDb(String objectId, Class targetClass, AssociationConstrain associationConstrain) {
        return deleteObjectFromDb(objectId, targetClass, new ArrayList(), associationConstrain, "");
    }

    /**
     * @param objectId    The primary key of the object to delete.
     * @param targetClass The class of the object to delete.
     */
    public static boolean deleteObjectFromDb(String objectId, Class targetClass) {
        return deleteObjectFromDb(objectId, targetClass, new ArrayList(), new AssociationConstrain(), "");
    }

    /**
     * This method identifies the associations and deletes these with a
     * recursive call. Then it deletes the object from the table.
     *
     * @param objectId             The primary key of the object to delete.
     * @param targetClass          The class of the object to delete.
     * @param modelObjects         The object which has been deleted before.
     * @param associationConstrain The associations which may be deleted or not.
     * @param attributePath        The concatenated attribute path so far.
     */
    public static boolean deleteObjectFromDb(String objectId, Class targetClass, List modelObjects, AssociationConstrain associationConstrain, String attributePath) {

        boolean successfull = true;
        log.debug("deleteObjectFromDb-1");
        if (objectId == null) {
            log.warn("Got an objectId which was null " + targetClass.getName());
            return true;
        }

        if (!(ModelObject.class.isAssignableFrom(targetClass) || ModelObjectInterface.class.isAssignableFrom(targetClass))) {
            //This is not a model object. We can not delete it.
            log.error("Tying to delete a object from the model which is not a ModelObject! " + targetClass + " " + objectId);
            return true;
        }
        log.debug("deleteObjectFromDb-2");
        if (associationConstrain.isNotAllowedAssociation(attributePath)) {
            //This is an association which we are not allowed to delete.
            log.debug("Could not delete this association " + attributePath);
            return true;
        }

        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(targetClass);

        //Have we been here before =>
        if (modelObjects.contains(dbAttributeContainer.getClassName() + ":" + objectId)) {
            //This object has allready been deleted before, in the recursive process.
            log.debug("Loop found. we have allready deleted this object. ");
            return true;
        } else {
            modelObjects.add(dbAttributeContainer.getClassName() + ":" + objectId);
        }

        log.debug("Deleting: " + dbAttributeContainer.getTableName() + " " + objectId);

        //Loop through the attributes looking for associations.
        for (Iterator someIte = dbAttributeContainer.getDbAttributes().values().iterator(); someIte.hasNext();) {
            DbAttribute dbAttribute = (DbAttribute) someIte.next();
            if (dbAttribute.isAssociation()) {
                //We have an association.

                String attributeName = dbAttribute.getAttributeName();
                String newAttributePath = AssociationConstrain.addAttributeToPath(attributePath, attributeName);
                if (associationConstrain.isNotAllowedAssociation(newAttributePath)) {
                    //We may not delete the association.
                    log.debug("May not delete this association " + newAttributePath);
                    if(dbAttribute.isMultiAssociation() && !dbAttribute.isPrimitivArrayAssociation()){
                        try {
                            SQLStatementExecutor.doUpdate("delete from " + AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute) + " where " + dbAttributeContainer.getAttributeContainer().getClassName() + "_" + dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName() + " = '" + objectId + "'");
                        } catch(Exception e){
                            log.error("Some error " + e, e);
                        }
                    }
                    continue;
                }
                if (dbAttribute.isMultiAssociation()) {

                    //First we get all the ids for the associations from the association table.
                    SelectSQLStatement selectSQLStatement = SQLStatementFactory.getSelectSQLStatement();
                    selectSQLStatement.addTableName(AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute));
                    String targetName = null;
                    if (!dbAttribute.isPrimitivArrayAssociation()) {
                        DbAttributeContainer targetDbAttributeContainer = DbClassReflector.getDbAttributeContainer(dbAttribute.getAttributeClass());
                        targetName = targetDbAttributeContainer.getPrimaryKeyAttribute().getAttributeName();
                    } else {
                        targetName = attributeName;
                    }
                    selectSQLStatement.addAttributeName(targetName);
                    selectSQLStatement.addConstrain(/*AssociationTable.SOURCE*/ dbAttributeContainer.getAttributeContainer().getClassName() + "_" + dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(), WhereSQLStatement.EQUAL, objectId);

                    LimResultSet limSet = null;
                    try {
                        limSet = SQLStatementExecutor.doQuery(selectSQLStatement);
                        ResultSet resultSet = limSet.getResultSet();
                        if (resultSet != null) {
                        List associationIds = new ArrayList();
                        //Gather the association ids in a list.
                        try {
                            while (resultSet.next())
                                associationIds.add(resultSet.getString(targetName));
                        } catch (Exception e) {
                            successfull = false;
                        }

                        //We got the id list. we now delete all associations in the association table
                        successfull = successfull && DbObjectWriter.deleteAssociations(objectId, dbAttributeContainer, dbAttribute);

                        if (!dbAttribute.isPrimitivArrayAssociation()) {
                            //Evaluate if we may delete the different associations.
                            //(note: we delete them in the associated table!)

                            //We delete each association with a recursive call.
                            Iterator iterator = associationIds.iterator();
                            while (iterator.hasNext()) {
                                String associationId = (String) iterator.next();
                                if (associationId != null)
                                    successfull = successfull && DbObjectDeleter.deleteObjectFromDb(associationId, dbAttribute.getAttributeClass(), modelObjects, associationConstrain, newAttributePath);
                            }
                        }
                        }
                    } catch (Exception exp) {
                        log.error("error.close() = " + exp);
                        exp.printStackTrace();
                    } finally {
                        if(limSet != null) limSet.close();
                    }

                } else {
                    //This is a singel association.
                    log.debug("deleteObjectFromDb-4");
                    //Evaluate if we may delete the association.
//                     String newAttributePath = AssociationConstrain.addAttributeToPath(attributePath, attributeName);
//                     if(associationConstrain.isNotAllowedAssociation(newAttributePath)) {
//                         //We may not delete the association.
//                         log.debug("May not delete this association "+ newAttributePath);
//                         continue;
//                     }

                    //Get the association id (the primary key) of the associated attribute.
                    SelectSQLStatement selectSQLStatement = SQLStatementFactory.getSelectSQLStatement();
                    selectSQLStatement.addTableName(dbAttributeContainer.getTableName());
                    String associationName = dbAttribute.getAttributeName();
                    String primaryKeyName = dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName();
                    selectSQLStatement.addAttributeName(associationName);
                    selectSQLStatement.addConstrain(primaryKeyName, WhereSQLStatement.EQUAL, objectId);
                    log.debug("deleteObjectFromDb-5");
                    LimResultSet limSet = null;
                    try {
                        limSet = SQLStatementExecutor.doQuery(selectSQLStatement);
                        ResultSet resultSet = limSet.getResultSet();
                        if (resultSet != null) {
                            if (resultSet.next()) {
                                String associationId = resultSet.getString(associationName);
                                if (associationId != null) {
                                    //We got the association Id which is primary key in the
                                    //associated table. We delete this one with a recursive call.
                                    successfull = successfull && DbObjectDeleter.deleteObjectFromDb(associationId, dbAttribute.getAttributeClass(), modelObjects, associationConstrain, newAttributePath);
                                }
                            }
                        }
                    } catch (Exception e) {
                        successfull = false;
                    } finally {
                        if(limSet != null){
                            limSet.close();
                            limSet = null;
                        }
                    }
                }
            }
            log.debug("deleteObjectFromDb-ends ... ");
        }

        //Delete this object from the table which it maps to.
        DeleteSQLStatement deleteSQLStatement = SQLStatementFactory.getDeleteSQLStatement();
        deleteSQLStatement.addTableName(dbAttributeContainer.getTableName());
        deleteSQLStatement.addConstrain(dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(), WhereSQLStatement.EQUAL, objectId);

        boolean deleteOK = SQLStatementExecutor.doUpdate(deleteSQLStatement.makeStatement());
        successfull = successfull && deleteOK;

        //Remove the object from the cache. =>
        log.debug("removing object from cache " + dbAttributeContainer.getTableName() + " " + objectId);
        ModelObject modelObject = (ModelObject) ObjectCacheFactory.getInstance().getObjectCache(targetClass).getFromCache(objectId);

        if (modelObject != null) {
            modelObject.setCachable(false);
            ObjectCacheFactory.getInstance().getObjectCache(targetClass).removeFromCache(objectId);
        }

        return successfull;
    }
}

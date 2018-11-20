package dk.lessismore.nojpa.reflection.db;

import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.cache.ObjectCacheRemote;
import dk.lessismore.nojpa.db.DbDataType;
import dk.lessismore.nojpa.db.LimResultSet;
import dk.lessismore.nojpa.db.SQLStatementExecutor;
import dk.lessismore.nojpa.db.statements.DeleteSQLStatement;
import dk.lessismore.nojpa.db.statements.InsertSQLStatement;
import dk.lessismore.nojpa.db.statements.SQLStatementFactory;
import dk.lessismore.nojpa.db.statements.SelectSQLStatement;
import dk.lessismore.nojpa.db.statements.UpdateSQLStatement;
import dk.lessismore.nojpa.db.statements.WhereSQLStatement;
import dk.lessismore.nojpa.reflection.attributeconverters.AttributeConverter;
import dk.lessismore.nojpa.reflection.attributeconverters.AttributeConverterFactory;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectComparator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import dk.lessismore.reusable_v4.db.pooling.*;

/**
 * This class can write an object, which extends the ModelObject class, to a table
 * which matches the class which the object is an instance of. The class will
 * deside wether the object is a new instance or if it allready exsists in the
 * table as an tupel. If it allready exists the tupel is updated; if not
 * a new tupel is inserted into the table. This action is done automaticly;
 * you do not have to worry about this feature!
 * <br>The object is only written to the table if the object is dirty (changed). If its
 * not dirty; and the object contains associations; either singel or multi; the
 * class will make a recursive call; writing all associations to the database.
 * If you have a big object structure; this will result in a faily large
 * stack trace; because of the nature of recurion. To optimise this; its possible
 * to give an <tt>AssociationConstrain</tt> object as one of the arguments. In this
 * object you can deside what association should not be saved; and which should. In this
 * way you can optimes the writing process if you know which associations have change
 * and which has not.
 * <br>But what about circular association (where one object associates an other object
 * which associates the first object). Can this class handle that to; or will it
 * go in an eternal loop. And yes it can handle this and prevent the loop; by registrating
 * the objects which allready has been saved.
 *
 * @author LESS-IS-MORE
 * @version 1.0 21-5-2
 */
public class DbObjectWriter {

    private static final Logger log = LoggerFactory.getLogger(DbObjectWriter.class);

    static {
        log.debug("NoJPA_version:0.1");
    }


    private static final int DEFAULT_DEEP = 2;

    private static boolean writeProtected = false;

    public DbObjectWriter() {
    }

    public static void setWriteProtected(boolean writeProtected) {
        DbObjectWriter.writeProtected = writeProtected;
    }

    /**
     * Writes the object to the database.
     */
    public static boolean writeObjectToDb(ModelObject modelObject) {
        AssociationConstrain associationConstrain = new AssociationConstrain();
        HashMap map = new HashMap();
//        System.out.println("DbObjectWriter.writeObjectToDb() : " + modelObject + " / " + modelObject.getClass());
        return writeObjectToDb(modelObject, map, associationConstrain, "", DEFAULT_DEEP);
    }

    /**
     * @param modelObject          The object to write to the database.
     * @param associationConstrain The associations which should be or not be saved.
     */
    public static boolean writeObjectToDb(ModelObject modelObject, AssociationConstrain associationConstrain) {
        HashMap map = new HashMap();
        return writeObjectToDb(modelObject, map, associationConstrain, "", DEFAULT_DEEP);
    }


     protected static Calendar preLastModifiedFromDB(Class<? extends ModelObjectInterface> interfaceClass, ModelObject modelObject){
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
        SelectSQLStatement selectSQLStatement = SQLStatementFactory.getSelectSQLStatement();
        selectSQLStatement.addTableName(dbAttributeContainer.getTableName());
        selectSQLStatement.addConstrain(dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(), WhereSQLStatement.EQUAL, modelObject.getObjectID());
        //Loop through the attributes.
        selectSQLStatement.addAttributeName("lastModified");
        LimResultSet limSet = null;
        try {
            limSet = SQLStatementExecutor.doQuery(selectSQLStatement);
            ResultSet resultSet = limSet.getResultSet();

            if (resultSet != null && resultSet.next()) {
                try {
                    String strValue = resultSet.getString("lastModified");
                    if (strValue != null) {
                        if (!strValue.equals("0000-00-00")) {
                            if (resultSet.getTime("lastModified") != null) {
                                if (resultSet.getDate("lastModified") != null) {
                                    Calendar time = Calendar.getInstance();
                                    Calendar date = Calendar.getInstance();

                                    time.setTime(resultSet.getTime("lastModified"));
                                    date.setTime(resultSet.getDate("lastModified"));
                                    date.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
                                    date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
                                    date.set(Calendar.SECOND, time.get(Calendar.SECOND));
                                    //log.debug("*** Time " + name + ":" + date.getTime());
                                    return date;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    if((e.toString().indexOf("0000-00-00") == -1)){
                        log.error("some error in preLastModifiedFromDB " + e, e);
                    }
                }

            }
        } catch (Exception e){
            log.error("some other error in preLastModifiedFromDB " + e, e);
        } finally {
            try {
                if(limSet != null){
                    limSet.close();
                    limSet = null;
                }
            } catch (Exception exp) {
                log.error("error.close() = " + exp);
                exp.printStackTrace();
            }
        }
        return null;
    }



    /**
     * Main save method. For internal and recursive use only.
     * The method will determine if the object allready has been saved. If this
     * is true; the method will return without doing any work. Then it determines
     * if the object has change (dirty or new). If this is true the object is
     * saved back to the database. Then the associations is evaluated; because
     * these could be dirty; but we are not sure. This is accomplised with
     * a recursive call to this method.
     * Finally, the object is cached, ready for new use.
     *
     * @param modelObject          The object to write to the database.
     * @param modelObjects         The objects which has been visited before.
     *                             (key=className:primaryKeyValue value=ModelObject)
     * @param associationConstrain The associations which should be or not be saved.
     * @param attributePath        The concatenated attribute path so far.
     */
    private static boolean writeObjectToDb(ModelObject modelObject, Map modelObjects, AssociationConstrain associationConstrain, String attributePath, int deep) {

        //log.debug("Writing object: " + modelObject + " with deep = " + deep);
        //log.debug("Writing object: " + modelObject.getClass().getName());
        //log.debug("Writing object: " + modelObject.getClass().getName()+ " "+modelObject + " deep: " + deep);

        if (writeProtected || deep <= 0) {
            return true;
        }
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(modelObject.getInterface());
        if (dbAttributeContainer != null) {


            if(!modelObject.isNew() && modelObject.isDirty()){
                DbAttribute dbAttributeLastModified = dbAttributeContainer.getDbAttributes().get("lastModified");
                if(dbAttributeLastModified == null){
                    //System.out.println("XXXX: Do nothing... ");
                } else {
                    Calendar myCurrentLastModified = (Calendar) dbAttributeLastModified.getAttribute().getAttributeValue(modelObject);
                    Calendar myPreLastModified = preLastModifiedFromDB(modelObject.getInterface(), modelObject);
                    if(myPreLastModified != null && myCurrentLastModified != null){
                        log.debug("XXX: Checking oldData vs new data... myCurrentLastModified[" + myCurrentLastModified.getTime() + "] vs myPreLastModified[" + myPreLastModified.getTime() + "] for modelObject.getInterface("+ modelObject.getInterface() +") ID("+ modelObject +")");
                        if(myCurrentLastModified.getTimeInMillis() < myPreLastModified.getTimeInMillis()){
                            String message = "Saving oldData ontop of new data... myCurrentLastModified[" + myCurrentLastModified.getTime() + "] vs myPreLastModified[" + myPreLastModified.getTime() + "] for modelObject.getInterface("+ modelObject.getInterface() +") ID("+ modelObject +")";
                            log.error(message);
                            throw new RuntimeException(message);
                        }
                    }
                }
            }



            boolean successfull = true;

            //We have an attribute container for the class.
            if (modelObjects.get(dbAttributeContainer.getClassName() + ":" + modelObject.getPrimaryKeyValue()) == null) {
                //We have not saved this object before.. we set it at the map.
                modelObjects.put(dbAttributeContainer.getClassName() + ":" + modelObject.getPrimaryKeyValue(), modelObject);
            } else {
                //log.debug("Loop detected " + dbAttributeContainer.getTableName() + " " + modelObject.getPrimaryKeyValue() + " , we have allready saved this one.");
                //We have saved this object before.
                return true;
            }

            if (modelObject.isNew() || modelObject.isDirty()) {
                //The object is new or dirty and we have to save it back to the database !

                deep = DEFAULT_DEEP;
                InsertSQLStatement insertSQLStatement = null;
                if (modelObject.isNew()) {
                    //Its new. we must make a insertstatement.
                    insertSQLStatement = SQLStatementFactory.getInsertSQLStatement();
                } else {
                    //An tupel allready exists in the database. We must make an update statement.
                    UpdateSQLStatement updateSQLStatement = SQLStatementFactory.getUpdateSQLStatement();
                    updateSQLStatement.addConstrain(dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(), WhereSQLStatement.EQUAL, dbAttributeContainer.getPrimaryKeyValue(modelObject));
                    insertSQLStatement = updateSQLStatement;

                    //Remove cache entry from other hosts
                    //TODO: PropertyTest if this works :-)

                    ObjectCacheRemote.removeFromRemoteCache(modelObject);
                }

                insertSQLStatement.addTableName(dbAttributeContainer.getTableName());

                saveAttributeValues(modelObject, dbAttributeContainer, insertSQLStatement);
                if (!writeProtected) {
                    successfull = SQLStatementExecutor.doUpdate(insertSQLStatement, dbAttributeContainer.containsLob());
                } else {
                    successfull = true;
                }

            } else {
                //log.debug("OPTIMEZE: This is not new or dirty: " + dbAttributeContainer.getTableName() + " " + modelObject.getPrimaryKeyValue());
                deep--;
            }

            //The associations may have been changed. We do not know that. so we must make
            //a recursive call for all of them.
            //log.debug("start of saveAssociations..");
            saveAssociations(modelObject, dbAttributeContainer, modelObjects, associationConstrain, attributePath, deep);
            //log.debug("ends of saveAssociations..");
            ////Now its no longer dirty or new !
            modelObject.setNew(false);
            modelObject.setDirty(false);

            if (modelObject.isCachable()) {
                //This object is fully loaded with all associations. We can put it
                //into the cache.
                //log.debug("start of putting in cache.");
                //if(modelObject != null && modelObject.getPrimaryKeyValue() != null){
                ObjectCacheFactory.getInstance().getObjectCache(modelObject).putInCache(modelObject.getPrimaryKeyValue(), modelObject);
                //log.debug("Caching: " + dbAttributeContainer.getTableName() + " " + modelObject.getPrimaryKeyValue());
                //}
                //log.debug("ends of putting in cache.");
            }
            modelObject.doneSavingByDbObjectWriter();
            return successfull;
        } else {
            return false;
        }
    }

    /**
     * This will fill in the attributes in the insert or update sql statement.
     *
     * @param modelObject          The object to fetch the attribute values from.
     * @param dbAttributeContainer The Database attribute container.
     * @param insertSQLStatement   The insert/update statement, to set the values at.
     */
    private static void saveAttributeValues(ModelObject modelObject, DbAttributeContainer dbAttributeContainer, InsertSQLStatement insertSQLStatement) {

        //Loop through  all attributes in the class.
        for (Iterator iterator = dbAttributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();) {
            DbAttribute dbAttribute = (DbAttribute) iterator.next();
            String attributeName = dbAttribute.getAttributeName();
            //If its not an association attribute.
            if(dbAttribute.getInlineAttributeName() != null){
                Object value = dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                addAttributeValueToStatement(dbAttribute, insertSQLStatement, value);
            } else if (!dbAttribute.isAssociation()) {
                Object value = null;
                if(attributeName.equals("lastModified")){
                    value = Calendar.getInstance();
                    dbAttributeContainer.setAttributeValue(modelObject.getProxyObject(), dbAttribute, value);
                } else {
                    value = dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                }
                addAttributeValueToStatement(dbAttribute, insertSQLStatement, value);
            }
            //If its an singel association we must save its id as a string
            else if (!dbAttribute.isMultiAssociation()) {

//                String associationId = null;
//                ModelObject value = (ModelObject) dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
//                if (value != null) {
//                    associationId = value.getPrimaryKeyValue();
//                }
                if(!dbAttribute.isInlineInterface()) {
                    insertSQLStatement.addAttributeValue(attributeName, modelObject.getSingleAssociationID(attributeName));
                }
            }
        }
    }

    private static void addAttributeValueToStatement(DbAttribute dbAttribute, InsertSQLStatement insertSQLStatement, Object value) {
        if(dbAttribute.isInlineInterface()){
            return;
        }



        String attributeName = dbAttribute.getInlineAttributeName() != null ? dbAttribute.getInlineAttributeName() : dbAttribute.getAttributeName();
        if (value != null) {
            //Convert the value to the equivalent data type.
            int type = dbAttribute.getDataType().getType();
            switch (type) {
                case DbDataType.DB_CLOB:
                case DbDataType.DB_CHAR:
                case DbDataType.DB_VARCHAR:
                    String valueStr = null;
                    if (value instanceof String) {
                        valueStr = (String) value;
                        if (valueStr.length() > dbAttribute.getNrOfCharacters() && type != DbDataType.DB_CLOB) {
                            String message = "Trunc : on " + dbAttribute.getAttributeClass().getSimpleName() + "." + dbAttribute.getAttributeName() + " db-size(" + dbAttribute.getNrOfCharacters() + ") value-size(" + valueStr.length() + ")";
                            log.error(message, new Exception(message));
                            valueStr = valueStr.substring(0, dbAttribute.getNrOfCharacters());
                        }

                    } else {
                        //This attribute is not a string; but we have to save it as one in the
                        //database. We must convert the object to a string!
                        AttributeConverter converter = AttributeConverterFactory.getInstance().getConverter(dbAttribute.getAttributeClass());
                        if (converter != null) {
                            //We have a converter for it.
                            if (!dbAttribute.getAttribute().isArray()) {
                                valueStr = converter.objectToString(value);
                            } else {
                                valueStr = converter.arrayToString((Object[]) value);
                            }
                        } else {
                            valueStr = value.toString();
                        }
                    }
                    insertSQLStatement.addAttributeValue(attributeName, valueStr);
                    break;
                case DbDataType.DB_INT:
                    insertSQLStatement.addAttributeValue(attributeName, ((Integer) value).intValue());
                    break;
                case DbDataType.DB_DOUBLE:
                    insertSQLStatement.addAttributeValue(attributeName, ((Double) value).doubleValue());
                    break;
                case DbDataType.DB_FLOAT:
                    insertSQLStatement.addAttributeValue(attributeName, ((Float) value).doubleValue());
                    break;
                case DbDataType.DB_LONG:
                    insertSQLStatement.addAttributeValue(attributeName, ((Long) value).longValue());
                    break;
                case DbDataType.DB_BOOLEAN:
                    insertSQLStatement.addAttributeValue(attributeName, ((Boolean) value).booleanValue() ? 1 : 0/*((Boolean)value).toString()*/);
                    break;
                case DbDataType.DB_DATE:
                    //log.debug("***TimeWrite: " + attributeName + " " + (value != null ? ((Calendar) value).getTime() : "null"));
                    insertSQLStatement.addAttributeValue(attributeName, ((Calendar) value));
                    break;
            }
        } else {
            if (dbAttribute.getDataType().getType() == DbDataType.DB_DATE) {
                insertSQLStatement.addAttributeValue(attributeName, ((Calendar) value));
            } else {
                insertSQLStatement.addAttributeValue(attributeName, (String) null);
            }
        }

    }

    /**
     * This method will save the associations of the model object.
     *
     * @param modelObject          The model object to get the associations from,.
     * @param dbAttributeContainer The attribute container of the model object.
     * @param modelObjects         The model objects which we allready have saved.
     * @param associationConstrain The associations which may or may not be saved.
     * @param attributePath        The concatenated attribute path so far.
     */
    public static boolean saveAssociations(ModelObject modelObject, DbAttributeContainer dbAttributeContainer, Map modelObjects, AssociationConstrain associationConstrain, String attributePath, int deep) {
        boolean successfull = true;

        boolean hasBeenChanged = modelObject.isNew() || modelObject.isDirty();

        //Loop through all the attributes in the class and finde the associations.
        for (Iterator iterator = dbAttributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();) {
            DbAttribute dbAttribute = (DbAttribute) iterator.next();
            String attributeName = dbAttribute.getAttributeName();
            //log.debug("saveAssociations:1:(" + attributeName + ") for " + modelObject.getClass().getSimpleName() + " ID:" + modelObject);
            //If its an association attribute.
            if (dbAttribute.isAssociation()) {

                if(dbAttribute.isInlineInterface()){
                    //log.debug("saveAssociations:2:(" + attributeName + ") continue ... not in cache.  for " + modelObject.getClass().getSimpleName() + " ID:" + modelObject);
                    continue;
                }
                if(!modelObject.containsAssociationInCache(attributeName)){
                    //log.debug("saveAssociations:2:(" + attributeName + ") continue ... not in cache.  for " + modelObject.getClass().getSimpleName() + " ID:" + modelObject);
                    continue;
                }


                //Determine if we are allowed to save this association.
                String newAttributePath = AssociationConstrain.addAttributeToPath(attributePath, attributeName);
                if (associationConstrain.isNotAllowedAssociation(newAttributePath)) {
                    log.warn("Not allowed to save association: " + newAttributePath);
                    continue; //We should not save this association.
                }

                Object association = dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                if (association != null) {
                    //log.debug("saveAssociations:3:(" + attributeName + ")");
                    if (!dbAttribute.isMultiAssociation()) {
                        //log.debug("saveAssociations:4:(" + attributeName + ")");
                        if (association instanceof ModelObjectInterface) {
                            ModelObject associationModelObject = (ModelObject) association;
                            //Recursive call.
                            //log.debug("saveAssociations:5:(" + attributeName + ")");
                            //if(associationModelObject.isNew() || associationModelObject.isDirty()) { TODO: Import this line again?
                                //log.debug("saveAssociations:6:(" + attributeName + ")");
                                successfull = successfull && DbObjectWriter.writeObjectToDb(associationModelObject, modelObjects, associationConstrain, newAttributePath, deep);
                            //}
                        }
                    } else {
                        if (association instanceof ModelObjectInterface[] && (((ModelObjectInterface[]) association).length < 64)) {
                            ModelObjectInterface[] associations = (ModelObjectInterface[]) association;
                            //log.debug("saveAssociations :. attributePath = " + attributePath);
                            //log.debug("saveAssociations :. newAttributePath = " + newAttributePath);
                            for (int i = 0; i < associations.length; i++) {
                                //recursive call.
                                if(associations[i].isNew() || associations[i].isDirty()) {
                                    ModelObject object = (ModelObject) associations[i];
                                    successfull = successfull && DbObjectWriter.writeObjectToDb(object, modelObjects, associationConstrain, newAttributePath, deep);
                                }
                            }
                        } else {
                            log.debug("saveAssociations :. dont save... ");
                        }

                        if (hasBeenChanged) {
                            //Object is dirty; and we need to update the association table,
                            //because we do not know if any change have been made to
                            //this multiassociation.
                            synchronized (modelObject) {
                                updateAssociationTable(modelObject, dbAttributeContainer, dbAttribute, (Object[]) association);
                            }
                        }

                    }
                } else {
                    if (dbAttribute.isMultiAssociation() && hasBeenChanged) {
                        //The value of the association was null; and the object is dirty.
                        //We must drop all associations from the association table if its an multiassociation.
                        deleteAssociations(modelObject.getPrimaryKeyValue(), dbAttributeContainer, dbAttribute);
                    }
                }
            }
        }
        return successfull;
    }

    /**
     * This method can update an association table. This is done by delete all previous
     * tupels that belong to the association from the table. And recreate them.
     *
     * @param modelObject          The model object to get the associations from,.
     * @param dbAttributeContainer The attribute container of the model object.
     * @param dbAttribute          The database attribute which has the association.
     * @param associations         The associations.
     */
    private static boolean updateAssociationTable(ModelObject modelObject, DbAttributeContainer dbAttributeContainer, DbAttribute dbAttribute, Object[] associations) {
        if (modelObject.isNew() || associations == null || associations.length < 1 || !(associations instanceof ModelObjectInterface[])) {
            //log.debug("updateAssociationTable old !! ");
//            boolean successfull = modelObject.isNew() || deleteAssociations(modelObject.getPrimaryKeyValue(), dbAttributeContainer, dbAttribute);
            boolean successfull = deleteAssociations(modelObject.getPrimaryKeyValue(), dbAttributeContainer, dbAttribute);
            successfull = successfull && insertAssociations(modelObject, dbAttributeContainer, dbAttribute, associations);
            return successfull;
        } else {
            //log.debug("updateAssociationTable new !! ");
            try {
                //log.debug("updateAssociationTable:  1: associations = " + associations);
                //log.debug("updateAssociationTable:  1: associations[0] = " + associations[0]);
                DbAttributeContainer associationContainer = DbClassReflector.getDbAttributeContainer(associations[0]);
                ArrayList arrayList = new ArrayList(associations.length);
                for (int i = 0; i < associations.length; i++) {
                    arrayList.add(associations[i]);
                }
                Collections.sort(arrayList, new ModelObjectComparator());

                SelectSQLStatement selectSQLStatement = SQLStatementFactory.getSelectSQLStatement();
                selectSQLStatement.addTableName(AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute));
                selectSQLStatement.addAttributeName(dbAttributeContainer.getAttributeContainer().getClassName() + "_" +
                        dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName());
                selectSQLStatement.addAttributeName(associationContainer.getPrimaryKeyAttribute().getAttributeName());
                selectSQLStatement.addConstrain(AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute) + "." +
                        dbAttributeContainer.getAttributeContainer().getClassName() + "_" +
                        dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(),
                        WhereSQLStatement.EQUAL, modelObject.getPrimaryKeyValue());
                LimResultSet limSet = SQLStatementExecutor.doQuery(selectSQLStatement);
                ResultSet associationResultSet = limSet.getResultSet();
                if (associationResultSet == null) {
                    boolean successfull = deleteAssociations(modelObject.getPrimaryKeyValue(), dbAttributeContainer, dbAttribute);
                    successfull = successfull && insertAssociations(modelObject, dbAttributeContainer, dbAttribute, associations);
                    return successfull;
                } else {
                    try {
                        String associationId = "0";
                        String arrayId = "0";
                        for (int arrayCount = 0; (arrayId != null || associationId != null);) {
                            if (arrayId != null && (associationId == null || arrayId.compareTo(associationId) < 0)) {
                                //log.debug("updateAssociationTable: do a insert .. ");
                                while (arrayId != null && (associationId == null || arrayId.compareTo(associationId) < 0)) {
                                    //Insert arrayId
                                    insertSingleAssociationId(modelObject, dbAttributeContainer, dbAttribute, arrayId, associationContainer);
                                    arrayId = (arrayCount < arrayList.size() ? arrayList.get(arrayCount++).toString() : null);
                                }
                            } else if (associationId != null && (arrayId == null || arrayId.compareTo(associationId) > 0)) {
                                while (associationId != null && (arrayId == null || arrayId.compareTo(associationId) > 0)) {
                                    //Delete associationId
                                    //log.debug("updateAssociationTable: do a delete .. ");
                                    deleteSingleAssociationId(modelObject, dbAttributeContainer, dbAttribute, associationId, associationContainer);
                                    associationId = (associationResultSet.next() ? associationResultSet.getString(
                                            associationContainer.getPrimaryKeyAttribute().getAttributeName()) : null);
                                }
                            } else {
                                //log.debug("updateAssociationTable: do nothing .. ");
                                arrayId = (arrayCount < arrayList.size() ? arrayList.get(arrayCount++).toString() : null);
                                associationId = (associationResultSet.next() ? associationResultSet.getString(
                                        associationContainer.getPrimaryKeyAttribute().getAttributeName()) : null);
                            }
                        }
                    } catch (Exception e) {

                    } finally {
                        try {
                            if (limSet != null) {
                                limSet.close();
                            }
                        } catch (Exception e) {
                            log.error("error.close() = " + e);
                            e.printStackTrace();
                        }
                        limSet = null;
                        return true;
                    }
                }
            } catch (Exception e) {
                log.error("Some error when doing smart update of array-association .. " + e.toString());
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * The method deletes the associations from an association table which belong to a certain
     * attribute in a given object. NB: the association is a multi association or an primitive array.
     *
     * @param objectId             The primary key.
     * @param dbAttributeContainer The attribute container of the object.
     * @param dbAttribute          The attribute which holdes the association.
     */
    public static boolean deleteAssociations(String objectId, DbAttributeContainer dbAttributeContainer, DbAttribute dbAttribute) {
        DeleteSQLStatement deleteSQLStatement = SQLStatementFactory.getDeleteSQLStatement();
        String associationTableName = AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute);
        deleteSQLStatement.addTableName(associationTableName);
        deleteSQLStatement.addConstrain(/*AssociationTable.SOURCE*/dbAttributeContainer.getAttributeContainer().getClassName() + "_" + dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(), deleteSQLStatement.EQUAL, objectId);

        if (!writeProtected) {
            return SQLStatementExecutor.doUpdate(deleteSQLStatement.makeStatement());
        } else {
            return true;
        }
    }

    private static boolean deleteSingleAssociationId(ModelObject modelObject, DbAttributeContainer dbAttributeContainer, DbAttribute dbAttribute, String associationId, DbAttributeContainer associationContainer) {

        DeleteSQLStatement deleteSQLStatement = SQLStatementFactory.getDeleteSQLStatement();
        String associationTableName = AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute);
        deleteSQLStatement.addTableName(associationTableName);
        deleteSQLStatement.addConstrain(dbAttributeContainer.getAttributeContainer().getClassName() + "_" + dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(), deleteSQLStatement.EQUAL, modelObject.getPrimaryKeyValue());
        deleteSQLStatement.addConstrain(associationContainer.getPrimaryKeyAttribute().getAttributeName(), deleteSQLStatement.EQUAL, associationId);
        return SQLStatementExecutor.doUpdate(deleteSQLStatement.makeStatement());
    }

    private static boolean insertSingleAssociationId(ModelObject modelObject, DbAttributeContainer dbAttributeContainer, DbAttribute dbAttribute, String associationId, DbAttributeContainer associationContainer) {

        InsertSQLStatement insertSQLStatement = SQLStatementFactory.getInsertSQLStatement();
        insertSQLStatement.addTableName(AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute));
        insertSQLStatement.addAttributeValue(dbAttributeContainer.getAttributeContainer().getClassName() + "_" + dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(), modelObject.getPrimaryKeyValue());
        insertSQLStatement.addAttributeValue(associationContainer.getPrimaryKeyAttribute().getAttributeName(), associationId);
        return SQLStatementExecutor.doUpdate(insertSQLStatement.makeStatement());
    }

    /**
     * The method will insert a multi association/primitive arrays into an association table.
     *
     * @param dbAttributeContainer The attribute container of the object.
     * @param dbAttribute          The attribute which holdes the association.
     * @param associations         The associations to be saved.
     */
    private static boolean insertAssociations(ModelObject modelObject, DbAttributeContainer dbAttributeContainer, DbAttribute dbAttribute, Object[] associations) {

        boolean successfull = true;
        String associationTableName = AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute);
        String sourceId = dbAttributeContainer.getPrimaryKeyValue(modelObject);

        for (int i = 0; i < associations.length; i++) {
            Object association = associations[i];


            InsertSQLStatement insertSQLStatement = SQLStatementFactory.getInsertSQLStatement();
            insertSQLStatement.addTableName(associationTableName);
            insertSQLStatement.addAttributeValue(/*AssociationTable.SOURCE*/dbAttributeContainer.getAttributeContainer().getClassName() + "_" + dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName(), sourceId);
            if (!dbAttribute.isPrimitivArrayAssociation()) {
                DbAttributeContainer associationContainer = DbClassReflector.getDbAttributeContainer(association);
                if (associationContainer != null) {
                    String targetId = associationContainer.getPrimaryKeyValue((ModelObject) association);
                    String attributeName = associationContainer.getPrimaryKeyAttribute().getAttributeName();
                    if(attributeName == null || !attributeName.equals("objectID")){
                        log.warn("Wrong in associationContainer: " + associationContainer);
                    }
                    insertSQLStatement.addAttributeValue(/*AssociationTable.TARGET*/attributeName, targetId);
                }
            } else {
                addAttributeValueToStatement(dbAttribute, insertSQLStatement, association);
            }

            if (!writeProtected) {
                successfull = SQLStatementExecutor.doUpdate(insertSQLStatement.makeStatement());
            } else {
                successfull = true;
            }

        }
        return successfull;
    }

    public static void main(String[] args) {

        /*  AttributeConverterFactory.getInstance().registrateConverter(Position.class, new PositionAttributeConverter());
          AttributeConverterFactory.getInstance().registrateConverter(Role.class, new RoleAttributeConverter());
          User user = User.getDummy();
          User user2 = User.getDummy();
          user2.setLoginName("b
           */
          }


}
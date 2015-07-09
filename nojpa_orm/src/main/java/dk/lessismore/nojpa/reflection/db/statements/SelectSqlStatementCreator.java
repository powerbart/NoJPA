package dk.lessismore.nojpa.reflection.db.statements;

import dk.lessismore.nojpa.reflection.db.attributes.*;
import dk.lessismore.nojpa.reflection.db.*;

import dk.lessismore.nojpa.db.statements.*;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * This class is a wrap around a sql select statement. It makes it easier to make
 * a select statement with the correct join attributes and expressions etc.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class SelectSqlStatementCreator  {

    private static final org.apache.log4j.Logger log = Logger.getLogger(SelectSqlStatementCreator.class);

    private SelectSQLStatement selectSQLStatement = null;

    public SelectSqlStatementCreator() {

    }
    public void setSelectSQLStatement(SelectSQLStatement selectSQLStatement) {
        this.selectSQLStatement = selectSQLStatement;
    }
    public SelectSQLStatement getSelectSQLStatement() {
        if(selectSQLStatement == null)
            selectSQLStatement = SQLStatementFactory.getSelectSQLStatement();
        return selectSQLStatement;
    }

    public void addLimit(int start, int end){
	getSelectSQLStatement().addLimit(start, end);
    }

    public boolean addJoin(Class sourceClass, String attributeName) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
        if(dbAttribute != null) {
            if(dbAttribute.isAssociation()) {
                DbAttributeContainer targetAttributeContainer = DbClassReflector.getDbAttributeContainer(dbAttribute.getAttributeClass());
                String sourceTableName = dbAttributeContainer.getTableName();
                String sourcePrimaryKey = dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName();
                String targetTableName = targetAttributeContainer.getTableName();
                String targetPrimaryKey = targetAttributeContainer.getPrimaryKeyAttribute().getAttributeName();

                getSelectSQLStatement().addTableName(sourceTableName);
                getSelectSQLStatement().addTableName(targetTableName);

                if(dbAttribute.isMultiAssociation()) {
//		        log.debug("addJoin ... is dbAttribute.isMultiAssociation = true");
                    String associationTableName = AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute);

                    getSelectSQLStatement().addTableName(associationTableName);
                    getSelectSQLStatement().addJoin(sourceTableName,  sourcePrimaryKey, associationTableName, /*AssociationTable.SOURCE*/dbAttributeContainer.getAttributeContainer().getClassName() + "_" + sourcePrimaryKey);
                    getSelectSQLStatement().addJoin(associationTableName, /*AssociationTable.TARGET*/targetPrimaryKey, targetTableName, targetPrimaryKey );
                }
                else {
//                    log.debug("addJoin ... is dbAttribute.isMultiAssociation = false");
                    getSelectSQLStatement().addJoin(sourceTableName, dbAttribute.getAttributeName(), targetTableName, targetPrimaryKey);
                }
                return true;
            }
        }
        log.error("addJoin: something wrong with the name or attributeName = " + attributeName +
            ", container=" + dbAttributeContainer + ", thread="+ Thread.currentThread().getName(), new Exception("addJoin"));
        return false;
    }

    public boolean addJoinForCountArray(Class sourceClass, String attributeName, String objectID) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
        if(dbAttribute != null) {
            if(dbAttribute.isAssociation()) {
                DbAttributeContainer targetAttributeContainer = DbClassReflector.getDbAttributeContainer(dbAttribute.getAttributeClass());
//                String sourceTableName = dbAttributeContainer.getTableName();
                String sourcePrimaryKey = dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName();

                String associationTableName = AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute);
                setSelectSQLStatement(SQLStatementFactory.getSelectSQLStatement());
                getSelectSQLStatement().addTableName(associationTableName);
                getSelectSQLStatement().addConstrain(dbAttributeContainer.getAttributeContainer().getClassName() + "_" + sourcePrimaryKey, WhereSQLStatement.EQUAL, objectID);
                return true;
            }
        }
        log.error("addJoin: something wrong with the name or attributeName = " + attributeName +
            ", container=" + dbAttributeContainer + ", thread="+ Thread.currentThread().getName(), new Exception("addJoin"));
        return false;
    }

    public void addConstrain(Class sourceClass, String attributeName, int comparator, int value) {
        getSelectSQLStatement().addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

    public void addConstrain(Class sourceClass, String attributeName, int comparator, String value) {
        getSelectSQLStatement().addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

//    public void whereIn(Class sourceClass, String attributeName, String[] values) {
//        getSelectSQLStatement().whereIn(makeAttributeIdentifier(sourceClass, attributeName), values);
//    }


    public void addConstrain(Class sourceClass, String attributeName, int comparator, Calendar value) {
        getSelectSQLStatement().addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

    public void addConstrain(Class sourceClass, String attributeName, int comparator, double value) {
        getSelectSQLStatement().addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

    public void isNull(Class sourceClass, String attributeName) {
        getSelectSQLStatement().isNull(makeAttributeIdentifier(sourceClass, attributeName));
    }

    public void isNotNull(Class sourceClass, String attributeName) {
        getSelectSQLStatement().isNotNull(makeAttributeIdentifier(sourceClass, attributeName));
    }

    public void addTable(Class tableClass) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(tableClass);
        if(dbAttributeContainer != null) {
            getSelectSQLStatement().addTableName(dbAttributeContainer.getTableName());
        }
    }

    public String makeAttributeIdentifier(Class sourceClass, String attributeName) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
        if(dbAttributeContainer != null) {
            DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
            getSelectSQLStatement().addTableName(dbAttributeContainer.getTableName());
//            log.debug("makeAttributeIdentifier : 1: dbAttributeContainer " + dbAttributeContainer);
//            log.debug("makeAttributeIdentifier : 2: dbAttributeContainer.getTableName() " + dbAttributeContainer.getTableName());
//            log.debug("makeAttributeIdentifier : 3: dbAttribute " + dbAttribute);
//            log.debug("makeAttributeIdentifier : 4: dbAttribute.getAttributeName() " + dbAttribute.getAttributeName());
            return dbAttributeContainer.getTableName()+"."+dbAttribute.getAttributeName();
        }
        return null;
    }

    public void setSource(Class sourceClass) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
        if(dbAttributeContainer != null) {
            getSelectSQLStatement().addTableName(dbAttributeContainer.getTableName());
            //getSelectSQLStatement().addAttributeName(dbAttributeContainer.getTableName(), dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName());
        }
    }
}

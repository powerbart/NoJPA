//package dk.lessismore.reusable_v4.reflection.db.statements;
//
//import dk.lessismore.reusable_v4.db.statements.SQLStatementFactory;
//import dk.lessismore.reusable_v4.db.statements.SelectSQLStatement;
//import dk.lessismore.reusable_v4.db.statements.solr.SolrSelectStatement;
//import dk.lessismore.reusable_v4.reflection.db.AssociationTable;
//import dk.lessismore.reusable_v4.reflection.db.DbClassReflector;
//import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttribute;
//import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttributeContainer;
//import org.apache.log4j.Logger;
//
//import java.util.Calendar;
//
///**
// * Created with IntelliJ IDEA.
// * User: seb
// */
//public class SelectSolrStatementCreator {
//
//    private static final org.apache.log4j.Logger log = Logger.getLogger(SelectSolrStatementCreator.class);
//
//    private SolrSelectStatement selectSQLStatement = null;
//
//    public SelectSolrStatementCreator() {
//
//    }
//    public void setSelectSQLStatement(SolrSelectStatement selectSQLStatement) {
//        this.selectSQLStatement = selectSQLStatement;
//    }
//
//    public SolrSelectStatement getSelectSQLStatement() {
//        if(selectSQLStatement == null)
//            selectSQLStatement = new SolrSelectStatement();
//        return selectSQLStatement;
//    }
//
//    public void addLimit(int start, int end){
//        getSelectSQLStatement().addLimit(start, end);
//    }
//
//    public boolean addJoin(Class sourceClass, String attributeName) {
//        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
//        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
//        if(dbAttribute != null) {
//            if(dbAttribute.isAssociation()) {
//                DbAttributeContainer targetAttributeContainer = DbClassReflector.getDbAttributeContainer(dbAttribute.getAttributeClass());
//                String sourceTableName = dbAttributeContainer.getTableName();
//                String sourcePrimaryKey = dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName();
//                String targetTableName = targetAttributeContainer.getTableName();
//                String targetPrimaryKey = targetAttributeContainer.getPrimaryKeyAttribute().getAttributeName();
//
//                getSelectSQLStatement().addTableName(sourceTableName);
//                getSelectSQLStatement().addTableName(targetTableName);
//
//                if(dbAttribute.isMultiAssociation()) {
//                    log.debug("addJoin ... is dbAttribute.isMultiAssociation = true");
//                    String associationTableName = AssociationTable.makeAssociationTableName(dbAttributeContainer, dbAttribute);
//
//                    getSelectSQLStatement().addTableName(associationTableName);
//                    getSelectSQLStatement().addJoin(sourceTableName,  sourcePrimaryKey, associationTableName, /*AssociationTable.SOURCE*/dbAttributeContainer.getAttributeContainer().getClassName() + "_" + sourcePrimaryKey);
//                    getSelectSQLStatement().addJoin(associationTableName, /*AssociationTable.TARGET*/targetPrimaryKey, targetTableName, targetPrimaryKey );
//                }
//                else {
//                    log.debug("addJoin ... is dbAttribute.isMultiAssociation = false");
//                    getSelectSQLStatement().addJoin(sourceTableName, dbAttribute.getAttributeName(), targetTableName, targetPrimaryKey);
//                }
//                return true;
//            }
//        }
//        log.error("addJoin: something wrong with the name or attributeName = " + attributeName +
//                ", container=" + dbAttributeContainer + ", thread="+ Thread.currentThread().getName(), new Exception("addJoin"));
//        return false;
//    }
//
//    public void addConstrain(Class sourceClass, String attributeName, int comparator, int value) {
//        getSelectSQLStatement().addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
//    public void addConstrain(Class sourceClass, String attributeName, int comparator, String value) {
//        getSelectSQLStatement().addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
////    public void whereIn(Class sourceClass, String attributeName, String[] values) {
////        getSelectSQLStatement().whereIn(makeAttributeIdentifier(sourceClass, attributeName), values);
////    }
//
//
//    public void addConstrain(Class sourceClass, String attributeName, int comparator, Calendar value) {
//        getSelectSQLStatement().addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
//    public void addConstrain(Class sourceClass, String attributeName, int comparator, double value) {
//        getSelectSQLStatement().addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
//    public void isNull(Class sourceClass, String attributeName) {
//        getSelectSQLStatement().isNull(makeAttributeIdentifier(sourceClass, attributeName));
//    }
//
//    public void isNotNull(Class sourceClass, String attributeName) {
//        getSelectSQLStatement().isNotNull(makeAttributeIdentifier(sourceClass, attributeName));
//    }
//
//    public void addTable(Class tableClass) {
//        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(tableClass);
//        if(dbAttributeContainer != null) {
//            getSelectSQLStatement().addTableName(dbAttributeContainer.getTableName());
//        }
//    }
//
//    public String makeAttributeIdentifier(Class sourceClass, String attributeName) {
//        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
//        if(dbAttributeContainer != null) {
//            DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
//            getSelectSQLStatement().addTableName(dbAttributeContainer.getTableName());
////            log.debug("makeAttributeIdentifier : 1: dbAttributeContainer " + dbAttributeContainer);
////            log.debug("makeAttributeIdentifier : 2: dbAttributeContainer.getTableName() " + dbAttributeContainer.getTableName());
////            log.debug("makeAttributeIdentifier : 3: dbAttribute " + dbAttribute);
////            log.debug("makeAttributeIdentifier : 4: dbAttribute.getAttributeName() " + dbAttribute.getAttributeName());
//            return dbAttributeContainer.getTableName()+"."+dbAttribute.getAttributeName();
//        }
//        return null;
//    }
//
//    public void setSource(Class sourceClass) {
//        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
//        if(dbAttributeContainer != null) {
//            getSelectSQLStatement().addTableName(dbAttributeContainer.getTableName());
//            //getSelectSQLStatement().addAttributeName(dbAttributeContainer.getTableName(), dbAttributeContainer.getPrimaryKeyAttribute().getAttributeName());
//        }
//    }
//
//
//}

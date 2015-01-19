package dk.lessismore.nojpa.db.statements;

import dk.lessismore.nojpa.resources.*;
import dk.lessismore.nojpa.db.statements.mysql.*;

/**
 * This is an factory which can make instances of different sql statements,
 * like forinstance update/insert statements. The produced sql statements will
 * be instances which will make sql statements optimesed for a specific database.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class SQLStatementFactory {


    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SQLStatementFactory.class);



    public static enum DatabaseInstance {
        MYSQL, H2, ORACLE
    }


    public static DatabaseInstance getDatabaseInstance() {
        return databaseInstance;
    }

    static DatabaseInstance databaseInstance = DatabaseInstance.MYSQL;

    public static void setDriverName(String driverName) {
        log.debug("We will connect to database with DriverName("+ driverName +")");
        if(driverName == null || driverName.contains("mysql")){
            databaseInstance = DatabaseInstance.MYSQL;
        } else if(driverName.contains("org.h2")){
           databaseInstance = DatabaseInstance.H2;
        } else if(driverName.contains("oracle")){
            databaseInstance = DatabaseInstance.ORACLE;
        } else {
            System.out.println("We don't know driverName : " + driverName);
            databaseInstance = DatabaseInstance.MYSQL;
        }
    }




    public SQLStatementFactory() {

    }

    public static InsertSQLStatement getInsertSQLStatement() {
        if(databaseInstance == DatabaseInstance.H2) {
            return new dk.lessismore.nojpa.db.statements.h2.H2InsertStatement();
        } else if(databaseInstance == DatabaseInstance.ORACLE) {
            return new dk.lessismore.nojpa.db.statements.oracle.OracleInsertStatement();
        } else {
            return new MySqlInsertStatement();
        }

    }

    public static UpdateSQLStatement getUpdateSQLStatement() {
        if(databaseInstance == DatabaseInstance.H2) {
            return new dk.lessismore.nojpa.db.statements.h2.H2UpdateStatement();
        } else if(databaseInstance == DatabaseInstance.ORACLE) {
            return new dk.lessismore.nojpa.db.statements.oracle.OracleUpdateStatement();
        } else {
            return new MySqlUpdateStatement();
        }
    }

    public static DeleteSQLStatement getDeleteSQLStatement() {
        if(databaseInstance == DatabaseInstance.H2) {
            return new dk.lessismore.nojpa.db.statements.h2.H2DeleteStatement();
        } else if(databaseInstance == DatabaseInstance.ORACLE) {
            return new dk.lessismore.nojpa.db.statements.oracle.OracleDeleteStatement();
        } else {
            return new MySqlDeleteStatement();
        }
    }

    public static SelectSQLStatement getSelectSQLStatement() {
        if(databaseInstance == DatabaseInstance.H2) {
            return new dk.lessismore.nojpa.db.statements.h2.H2SelectStatement();
        } else if(databaseInstance == DatabaseInstance.ORACLE) {
            return new dk.lessismore.nojpa.db.statements.oracle.OracleSelectStatement();
        } else {
            return new MySqlSelectStatement();
        }
    }

    public static CreateSQLStatement getCreateSQLStatement() {
        log.debug("getCreateSQLStatement() :: log = " + databaseInstance);
        if(databaseInstance == DatabaseInstance.H2) {
            return new dk.lessismore.nojpa.db.statements.h2.H2CreateStatement();
        } else if(databaseInstance == DatabaseInstance.ORACLE) {
            return new dk.lessismore.nojpa.db.statements.oracle.OracleCreateStatement();
        } else {
            return new MySqlCreateStatement();
        }
    }

    public static DropSQLStatement getDropSQLStatement() {
        log.debug("getDropSQLStatement() :: log = " + databaseInstance);
        if(databaseInstance == DatabaseInstance.H2) {
            return new dk.lessismore.nojpa.db.statements.h2.H2DropStatement();
        } else if(databaseInstance == DatabaseInstance.ORACLE) {
            return new dk.lessismore.nojpa.db.statements.oracle.OracleDropStatement();
        } else {
            return new MySqlDropStatement();
        }
    }

    public static LeafExpression getLeafExpression() {
        if(databaseInstance == DatabaseInstance.H2) {
            return new dk.lessismore.nojpa.db.statements.h2.H2LeafExpression();
        } else if(databaseInstance == DatabaseInstance.ORACLE) {
            return new dk.lessismore.nojpa.db.statements.oracle.OracleLeafExpression();
        } else {
            return new MySqlLeafExpression();
        }
    }

    public static ContainerExpression getContainerExpression() {
        if(databaseInstance == DatabaseInstance.H2) {
            return new dk.lessismore.nojpa.db.statements.h2.H2ContainerExpression();
        } else if(databaseInstance == DatabaseInstance.ORACLE) {
            return new dk.lessismore.nojpa.db.statements.oracle.OracleContainerExpression();
        } else {
            return new MySqlContainerExpression();
        }
    }
}

package dk.lessismore.nojpa.db.statements.oracle;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public interface OracleDataProxyInterface {

    void readRow(ResultSet rs, ResultSetMetaData metaData, Object objectToFill) throws Exception;

}

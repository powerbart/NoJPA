package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.DbDataType;
import dk.lessismore.nojpa.db.statements.CreateSQLStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class MySqlCreateStatement extends MySqlStatement implements CreateSQLStatement {

    private static final Logger log = LoggerFactory.getLogger(MySqlCreateStatement.class);

    private List<String> attributes = null;
    private List<String> primaryKeys = null;
    private Map<String, String> namesToIndex = Collections.emptyMap();
    private int key_block_size = -1;

    public List<String> getAttributes() {
        if (attributes == null) {
            attributes = new LinkedList<>();
        }
        return attributes;
    }

    public void setNamesToIndex(Map<String, String> namesToIndex) {
        this.namesToIndex = namesToIndex;
    }

    @Override
    public void addCompressed(int key_block_size) {
        this.key_block_size = key_block_size;
    }

    public List<String> getPrimaryKeys() {
        if (primaryKeys == null) {
            primaryKeys = new LinkedList<>();
        }
        return primaryKeys;
    }

    public void addAttribute(String attributeName, DbDataType dataType, int[] properties) {

        String attribute = attributeName + " " + dataType;
        for (int i = 0; i < properties.length; i++) {
            attribute += " " + CreateSQLStatement.propertiesAsString[properties[i]];
        }
        getAttributes().add(attribute);
    }

    public void addAttribute(String attributeName, DbDataType dataType) {

        String attribute = attributeName + " " + dataType;
        getAttributes().add(attribute);
    }

    public void addPrimaryKey(String attributeName) {
        getPrimaryKeys().add(attributeName);
    }

    public String makeStatement() {

        if (getTableNames().isEmpty()) {
            throw new RuntimeException("Carnt make create statement without tablename");
        }
        if (getAttributes().isEmpty()) {
            throw new RuntimeException("Carnt make create statement without attributes");
        }

        StringBuilder statement = new StringBuilder();
        statement.append("create table ").append(getTableNames().get(0)).append(" (");
        statement.append(makeList(getAttributes().iterator()));
        if (!getPrimaryKeys().isEmpty()) {
            statement.append(",");
            statement.append("\n\t");
            statement.append("primary key (");
            Iterator iterator = getPrimaryKeys().iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                if (i > 0) {
                    statement.append(", ");
                }
                statement.append((String) iterator.next());
            }
            statement.append(")");
        }
        statement.append("\n\n");

        for (String index : namesToIndex.keySet()) {
            String indexName = namesToIndex.get(index);
            statement.append(", INDEX " + indexName + " ( " + index + ")");
        }

        // TODO set charset and collation as parameter
        // TODO http://stackoverflow.com/questions/2876789/case-insensitive-for-sql-like-wildcard-statement
        statement.append("\n) ");

        if(key_block_size > -1) {
            statement.append(" ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=" + key_block_size + " ");// CHARACTER SET utf8 COLLATE utf8_bin");
        }
        statement.append("ENGINE=MyISAM");// CHARACTER SET utf8 COLLATE utf8_bin");
        return statement.toString();
    }


}

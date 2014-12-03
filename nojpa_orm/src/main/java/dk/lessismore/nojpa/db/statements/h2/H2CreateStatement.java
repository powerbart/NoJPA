package dk.lessismore.nojpa.db.statements.h2;

import dk.lessismore.nojpa.db.DbDataType;
import dk.lessismore.nojpa.db.statements.CreateSQLStatement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class H2CreateStatement extends H2Statement implements CreateSQLStatement {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(H2CreateStatement.class);

    private List<String> attributes = null;
    private List<String> primaryKeys = null;
    private String[] namesToIndex = null;

    public List<String> getAttributes() {
        if (attributes == null) {
            attributes = new LinkedList<String>();
        }
        return attributes;
    }

    public void addIndex(String[] namesToIndex) {
        this.namesToIndex = namesToIndex;
    }

    public List<String> getPrimaryKeys() {
        if (primaryKeys == null) {
            primaryKeys = new LinkedList<String>();
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
        for (int i = 0; namesToIndex != null && i < namesToIndex.length; i++) {
            String indexName = "_";
            String indexFullName = (getTableNames().get(0) + "," + namesToIndex[i]);//;
            //log.debug("makeStatement: indexFullName = " + indexFullName);
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

//            statement.append(", INDEX " + indexName + " ( " + namesToIndex[i] + ")");
        }
        statement.append("\n)");// CHARACTER SET utf8 COLLATE utf8_bin");
//        statement.append("\n) ENGINE=MyISAM");// CHARACTER SET utf8 COLLATE utf8_bin");
        return statement.toString();
    }

    public static void main(String[] args) {


        System.out.println( "asd, asd".replaceAll(" |,", "p") );

        
//        MySqlCreateStatement c = new MySqlCreateStatement();
//        c.addTableName("skodTable");
//
//        c.addAttribute("tablet", new DbDataType(DbDataType.DB_VARCHAR, 250));
//        c.addAttribute("skod", new DbDataType(DbDataType.DB_CHAR, 50), new int[]{CreateSQLStatement.PROPERTY_NOT_NULL});
//        c.addAttribute("knald", new DbDataType(DbDataType.DB_INT));
//        c.addPrimaryKey("knald");
//        c.addPrimaryKey("tablet");
        //System.out.println(c.makeStatement());
    }
}

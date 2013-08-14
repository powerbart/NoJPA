package dk.lessismore.reusable_v4.db.pooling;

import java.sql.*;
/**
 * Insert the type's description here.
 * Creation date: (22-04-2002 12:02:28)
 * @author: Administrator
 */
public interface SQLCommandBeanInterface {

    public boolean addColumn(String tableName, String columnName, String sql);
    public boolean addColumns(String tableName, String sql);
    public boolean createTable(String tableName, String sql);
    public boolean deleteColumn(String tableName, String columnName, String sql);
    public boolean deleteColumns(String tableName, String sql);
    public boolean deleteRow(String tableName, String sql);
    public boolean deleteRows(String tableName, String sql);
    public boolean deleteTable(String tableName, String sql);
    public ResultSet getRow(String tableName, String sql);
    public ResultSet getRows(String tableName, String sql);
    public boolean insertRow(String tableName, String sql);
    public boolean insertRows(String tableName, String sql);
    public boolean updateRow(String tableName, String sql);
    public boolean updateRows(String tableName, String sql);
}

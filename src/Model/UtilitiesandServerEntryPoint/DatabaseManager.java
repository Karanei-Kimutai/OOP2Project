package Model.UtilitiesandServerEntryPoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static{
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            //Load database properties from an external file
            Properties properties=new Properties();
            FileInputStream inputStream=new FileInputStream("credentialsConfiguration.properties");
            properties.load(inputStream);
            DB_URL=properties.getProperty("databaseURL");
            DB_USER=properties.getProperty("databaseUsername");
            DB_PASSWORD=properties.getProperty("databasePassword");
        }catch(ClassNotFoundException e){
            System.err.println("FATAL: MySQL JDBC Driver not found. Please add mysql-connector-java.jar to your classpath.");
            throw new RuntimeException("MySQL JDBC Driver not found", e );
        }catch(IOException e){
            System.err.println("FATAL: Could not read database credentials configuration file.");
            throw new RuntimeException("Could not get database credentials");
        }
    }

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);
    }

    public static void closeQuietly(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) {rs.close();} } catch (SQLException e) { /* ignore */ }
        try { if (stmt != null){stmt.close();} } catch (SQLException e) { /* ignore */ }
        try { if (conn != null && conn.getAutoCommit()) conn.close(); } // Only close if auto-commit is true (not part of external tx)
        catch (SQLException e) { /* ignore */ }
    }
    public static void closeQuietly(Statement stmt,Connection conn){
        closeQuietly(null,stmt,conn);
    }
    public static void closeConnectionQuietly(Statement stmt,Connection conn){// For connections managed externally
        try {
            if (conn != null) {
                conn.close();
            }
        }catch(SQLException e){
            /* ignore */
        }
    }

}

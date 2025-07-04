package Model.UtilitiesandServerEntryPoint;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/drink_enterprise_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER="root";
    private static final String DB_PASSWORD="karanei2006";

    static{
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(ClassNotFoundException e){
            System.err.println("FATAL: MySQL JDBC Driver not found. Please add mysql-connector-java.jar to your classpath.");
            throw new RuntimeException("MySQL JDBC Driver not found", e );
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

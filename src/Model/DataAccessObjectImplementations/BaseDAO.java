package Model.DataAccessObjectImplementations;

import Model.UtilitiesandServerEntryPoint.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAO {
    protected Connection getConnection(Connection... existingConns) throws SQLException{
        if(existingConns!=null && existingConns.length>0 &&existingConns[0]!=null){
            return existingConns[0];//Use existing connection if provided(for transactions)
        }
        else{
            return DatabaseManager.getConnection();
        }
    }
    protected void closeResources(ResultSet rs, Statement stmt, Connection conn, Connection... existingConns){
        if(existingConns!=null && existingConns.length>0 && existingConns[0]!=null){
            //If using an existing connection, only close rs and stmt, not the connection itself
            try{
                if(rs!=null){
                    rs.close();
                }
            } catch (SQLException e) {
                /*ignore*/
            }
            try{
                if(stmt!=null){
                    stmt.close();
                }
            }catch(SQLException e){
                /*ignore*/
            }
        }else{
            DatabaseManager.closeQuietly(rs,stmt,conn);
        }
    }
    protected void closeResources(Statement stmt,Connection conn,Connection... existingConns){
        closeResources(null,stmt,conn,existingConns);
    }
}

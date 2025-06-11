package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IUserDAO;
import Model.DataEntities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAOImplementation extends BaseDAO implements IUserDAO {
    @Override
    public void add(User user, Connection... conns) throws SQLException{
        String sql="INSERT INTO users(username,hashed_password,role) VALUES(?,?,?)";
        Connection conn=null;
        PreparedStatement pstmt=null;
        try{
         conn=getConnection(conns);
         pstmt=conn.prepareStatement(sql);
         pstmt.setString(1,user.getUsername());
         pstmt.setString(2,user.getHashedPassword());
         pstmt.setString(3,user.getRole().name());
         pstmt.executeUpdate();
        }finally {
            closeResources(pstmt,conn,conns);
        }

    }
    @Override
    public Optional<User> findByUserName(String username,Connection... conns) throws SQLException{
        String sql="SELECT username, hashed_password, role FROM users WHERE username=?";
        Connection conn=null;
        PreparedStatement pstmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,username);
            rs=pstmt.executeQuery();
            if(rs.next()){
                return Optional.of(new User(rs.getString("username"), rs.getString("hashed_password"), User.UserRole.valueOf(rs.getString("role"))));
            }
        }finally {
            closeResources(rs,pstmt,conn,conns);
        }
        return Optional.empty();
    }
}

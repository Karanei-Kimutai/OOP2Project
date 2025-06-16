package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IUserDAO;
import Model.DataEntities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImplementation extends BaseDAO implements IUserDAO {
    @Override
    public void add(User user, Connection... conns) throws SQLException {
        String sql = "INSERT INTO users (username, hashed_password, role, branch_id) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getHashedPassword());
            pstmt.setString(3, user.getRole().name());
            // Admin role has a null branch_id
            if (user.getBranchId() == null) {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(4, user.getBranchId());
            }
            pstmt.executeUpdate();
        } finally {
            closeResources(pstmt, conn, conns);
        }
    }

    @Override
    public Optional<User> findByUsername(String username, Connection... conns) throws SQLException {
        String sql = "SELECT username, hashed_password, role, branch_id FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new User(rs.getString("username"), rs.getString("hashed_password"), User.UserRole.valueOf(rs.getString("role")), rs.getString("branch_id")));
            }
        } finally {
            closeResources(rs, pstmt, conn, conns);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll(Connection... conns) throws SQLException {
        String sql = "SELECT username, hashed_password, role, branch_id FROM users";
        List<User> users = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection(conns);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        null, // Never send hash back to client
                        User.UserRole.valueOf(rs.getString("role")),
                        rs.getString("branch_id")
                ));
            }
        } finally {
            closeResources(rs, stmt, conn, conns);
        }
        return users;
    }

    @Override
    public void delete(String username, Connection... conns) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            closeResources(pstmt, conn, conns);
        }
    }
}


package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IBranchDAO;
import Model.DataEntities.Branch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BranchDAOImplementation extends BaseDAO implements IBranchDAO {
    @Override
    public void add(Branch branch, Connection... conns) throws SQLException{
        String sql="INSERT INTO branches (branch_id, name, location) VALUES (?, ?, ?)";
        Connection conn=null;
        PreparedStatement psmt=null;
        try{
            conn=getConnection(conns);
            psmt=conn.prepareStatement(sql);
            psmt.setString(1,branch.getId());
            psmt.setString(2,branch.getName());
            psmt.setString(3,branch.getLocation());
            psmt.executeUpdate();
        }finally {
            closeResources(psmt,conn,conns);
        }
    }
    @Override
    public Optional<Branch> findById(String branchId,Connection... conns) throws SQLException{
        String sql = "SELECT branch_id, name, location FROM branches WHERE branch_id = ?";
        Connection conn=null;
        PreparedStatement psmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            psmt=conn.prepareStatement(sql);
            psmt.setString(1,branchId);
            rs=psmt.executeQuery();
            if(rs.next()){
                return Optional.of(new Branch(rs.getString("branch_id"), rs.getString("name"), rs.getString("location")));
            }
        }finally {
            closeResources(rs,psmt,conn,conns);
        }
        return Optional.empty();
    }
    @Override
    public List<Branch> findAll(Connection... conns) throws SQLException{
        String sql= "SELECT branch_id, name, location FROM branches";
        List<Branch> branches = new ArrayList<>();
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            stmt=conn.createStatement();
            rs= stmt.executeQuery(sql);
            while(rs.next()){
                branches.add(new Branch(rs.getString("branch_id"), rs.getString("name"), rs.getString("location")));
            }
        }finally {
            closeResources(rs,stmt,conn,conns);
        }
        return branches;
    }
}

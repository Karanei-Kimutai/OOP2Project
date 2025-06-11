package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IStockItemDAO;
import Model.DataEntities.StockItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StockItemDAOImplementation extends BaseDAO implements IStockItemDAO {
    @Override
    public void saveOrUpdate(StockItem stockItem, Connection... conns) throws SQLException{
        String sql="INSERT INTO stock_items (branch_id, drink_id, quantity, minimum_threshold) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), minimum_threshold = VALUES(minimum_threshold)";
        Connection conn=null;
        PreparedStatement psmt=null;
        try{
            conn=getConnection(conns);
            psmt=conn.prepareStatement(sql);
            psmt.setString(1,stockItem.getBranchId());
            psmt.setString(2,stockItem.getDrinkId());
            psmt.setInt(3,stockItem.getQuantity());
            psmt.executeUpdate();
        }finally {
            closeResources(psmt,conn,conns);
        }
    }
    @Override
    public Optional<StockItem> findByBranchAndDrink(String branchId,String drinkId,Connection... conns)throws SQLException{
        String sql = "SELECT branch_id, drink_id, quantity, minimum_threshold FROM stock_items WHERE branch_id = ? AND drink_id = ?";
        Connection conn=null;
        PreparedStatement psmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            psmt=conn.prepareStatement(sql);
            psmt.setString(1,branchId);
            psmt.setString(2,drinkId);
            rs=psmt.executeQuery();
            if(rs.next()){
                return Optional.of(new StockItem(rs.getString("branch_id"), rs.getString("drink_id"), rs.getInt("quantity"), rs.getInt("minimum_threshold")));
            }
        }finally {
             closeResources(rs,psmt,conn,conns);
        }
        return Optional.empty();
    }
    @Override
    public List<StockItem> findByBranch(String branchId,Connection... conns) throws SQLException{
        String sql="SELECT branch_id, drink_id,quantity,minimum_threshold FROM stock_items WHERE branch_id=?";
        List<StockItem> items=new ArrayList<>();
        Connection conn=null;
        PreparedStatement psmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            psmt=conn.prepareStatement(sql);
            psmt.setString(1,branchId);
            rs=psmt.executeQuery();
            while(rs.next()){
                items.add(new StockItem(rs.getString("branch_id"), rs.getString("drink_id"), rs.getInt("quantity"), rs.getInt("minimum_threshold")));
            }
        }finally {
            closeResources(rs,psmt,conn,conns);
        }
        return items;
    }
    @Override
    public List<StockItem> findAllLowStock(Connection... conns) throws SQLException{
        String sql = "SELECT si.branch_id, si.drink_id, si.quantity, si.minimum_threshold " + // d.name as drink_name, b.name as branch_name " +
                      "FROM stock_items si " +
                      // "JOIN drinks d ON si.drink_id = d.drink_id " + // Join to get names directly if needed
                      // "JOIN branches b ON si.branch_id = b.branch_id " +
                      "WHERE si.quantity < si.minimum_threshold AND si.minimum_threshold > 0";
        List<StockItem> items=new ArrayList<>();
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            stmt=conn.createStatement();
            rs=stmt.executeQuery(sql);
            while(rs.next()){
                items.add(new StockItem(rs.getString("branch_id"), rs.getString("drink_id"), rs.getInt("quantity"), rs.getInt("minimum_threshold")));
            }
        }finally {
            closeResources(rs,stmt,conn,conns);
        }
        return items;
    }
    @Override
    public void updateStockQuantity(String branchId,String drinkId, int newQuantity,Connection... conns) throws SQLException{
        // This is effectively covered by saveOrUpdate if threshold is also known/set.
        // If only quantity update is needed and threshold must be preserved:
        String sql = "UPDATE stock_items SET quantity = ? WHERE branch_id = ? AND drink_id = ?";
        Connection conn=null;
        PreparedStatement psmt=null;
        try{
            conn=getConnection(conns);
            psmt=conn.prepareStatement(sql);
            psmt.setInt(1,newQuantity);
            psmt.setString(2,branchId);
            psmt.setString(3,drinkId);
            int affectedRows=psmt.executeUpdate();
            if(affectedRows==0){
                throw new SQLException("Stock item not found for update or quantity unchanged: " + branchId + "/" + drinkId);
            }
        }finally {
            closeResources(psmt,conn,conns);
        }
    }
}

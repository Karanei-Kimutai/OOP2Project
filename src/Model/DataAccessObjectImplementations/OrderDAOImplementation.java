package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IOrderDAO;
import Model.DataEntities.Order;
import Model.DataEntities.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImplementation extends BaseDAO implements IOrderDAO {
    @Override
    public void saveOrderHeader(Order order, Connection conn) throws SQLException {//Explicit Connection for transaction
     String sql="INSERT INTO orders(order_id, customer_id, branch_id, order_timestamp, total_amount) VALUES (?,?,?,?,?)";
        PreparedStatement pstmt=null;
        try{
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,order.getOrderId());
            pstmt.setString(2,order.getCustomerId());
            pstmt.setString(3,order.getBranchId());
            pstmt.setTimestamp(4, Timestamp.valueOf(order.getOrderTimestamp()));
            pstmt.setDouble(5,order.getTotalAmount());
            pstmt.executeUpdate();
        }finally {
            if(pstmt!=null){pstmt.close();}
        }
    }

    @Override
    public void saveOrderItems(List<OrderItem> items, Connection conn) throws SQLException {
        String sql="INSERT INTO order_items(order_id,drink_id,quantity,price_at_time_of_order, item_total) VALUES (?,?,?,?,?)";
        PreparedStatement pstmt=null;
        try{
            pstmt=conn.prepareStatement(sql);
            for(OrderItem item:items){
                pstmt.setString(1,item.getOrderIdFk());// Ensure OrderIdFk is set in items
                pstmt.setString(2,item.getDrinkId());
                pstmt.setInt(3,item.getQuantity());
                pstmt.setDouble(4,item.getPriceAtTimeOfOrder());
                pstmt.setDouble(5,item.getItemTotal());
                pstmt.addBatch();
            }
        }finally {
          if(pstmt!=null){
              pstmt.close();
          }
        }

    }
    private OrderItem mapRowToOrderItem(ResultSet rs) throws SQLException{
        return new OrderItem(rs.getInt("order_item_id"), rs.getString("order_id"), rs.getString("drink_id"), null, rs.getInt("quantity"), rs.getDouble("price_at_time_of_order"), rs.getDouble("item_total"));
    }
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        return new Order(rs.getString("order_id"), rs.getString("customer_id"), rs.getString("branch_id"), rs.getTimestamp("order_timestamp").toLocalDateTime(), new ArrayList<>(), rs.getDouble("total_amount"));
    }


    @Override
    public Optional<Order> findbyId(String orderId, Connection... conns) throws SQLException {
        String sql="SELECT * FROM orders WHERE order_id=?";
        Connection conn=null;
        PreparedStatement pstmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,orderId);
            rs=pstmt.executeQuery();
            if(rs.next()){
                Order order=mapRowToOrder(rs);
                order.setItems(findItemsByOrderId(orderId,conn));
                return Optional.of(order);
            }
        }finally {
          closeResources(rs,pstmt,conn,conns);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByBranchId(String branchId, Connection... conns) throws SQLException {
        String sql="SELECT * FROM orders WHERE branch_id=?";
        List<Order> orders=new ArrayList<>();
        Connection conn=null;
        PreparedStatement pstmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,branchId);
            rs=pstmt.executeQuery();
            while(rs.next()){
                Order order=mapRowToOrder(rs);
                order.setItems(findItemsByOrderId(order.getOrderId(),conn));
                orders.add(order);
            }
        }finally {
           closeResources(rs,pstmt,conn,conns);
        }
        return orders;
    }

    @Override
    public List<Order> findByCustomerId(String customerId, Connection... conns) throws SQLException {
        String sql="SELECT * FROM orders WHERE customer_id=?";
        List<Order> orders=new ArrayList<>();
        Connection conn=null;
        PreparedStatement pstmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,customerId);
            rs=pstmt.executeQuery();
            while(rs.next()){
                Order order=mapRowToOrder(rs);
                order.setItems(findItemsByOrderId(order.getOrderId(),conn));
                orders.add(order);
            }
        }finally {
            closeResources(rs,pstmt,conn,conns);
        }
        return orders;
    }

    @Override
    public List<Order> findAll(Connection... conns) throws SQLException {
        String sql="SELECT * FROM orders";
        List<Order> orders=new ArrayList<>();
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            stmt=conn.createStatement();
            rs=stmt.executeQuery(sql);
            while(rs.next()){
                Order order=mapRowToOrder(rs);
                order.setItems(findItemsByOrderId(order.getOrderId(),conn));
                orders.add(order);
            }
        }finally{

        }
        return List.of();
    }

    @Override
    public List<OrderItem> findItemsByOrderId(String orderId, Connection... conns) throws SQLException {
        String sql="SELECT oi.* FROM order_items oi WHERE oi.order_id=?";// d.name as drink_name JOIN drinks d ON oi.drink_id = d.drink_id
        List<OrderItem> items=new ArrayList<>();
        Connection conn=null;
        PreparedStatement pstmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,orderId);
            rs=pstmt.executeQuery();
            while(rs.next()){
                items.add(mapRowToOrderItem(rs)); //Drink name needs to be populated by service
            }
        }finally {
            closeResources(rs,pstmt,conn,conns);
        }
        return items;
    }
}

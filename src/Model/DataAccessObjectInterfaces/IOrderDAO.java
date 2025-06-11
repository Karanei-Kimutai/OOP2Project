package Model.DataAccessObjectInterfaces;

import Model.DataEntities.Order;
import Model.DataEntities.OrderItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IOrderDAO {
    void saveOrderHeader(Order order, Connection conn) throws SQLException; // Requires explicit connection for transaction
    void saveOrderItems(List<OrderItem> items, Connection conn) throws SQLException;// Requires explicit connection for transaction
    Optional<Order> findbyId(String orderId,Connection...conn) throws SQLException;
    List<Order> findByBranchId(String branchId,Connection...conn) throws SQLException;
    List<Order> findByCustomerId(String customerId,Connection...conn) throws SQLException;
    List<Order> findAll(Connection... conn) throws SQLException;
    List<OrderItem> findItemsByOrderId(String orderId,Connection... conn) throws SQLException;
}


package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IOrderDAO;
import Model.DataEntities.Order;
import Model.DataEntities.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImplementation extends BaseDAO implements IOrderDAO {

    public void saveOrderHeader(Order order, Connection connection) throws SQLException {
        String sql = "INSERT INTO orders(order_id, customer_id, branch_id, order_timestamp, total_amount) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, order.getOrderId());
            statement.setString(2, order.getCustomerId());
            statement.setString(3, order.getBranchId());
            statement.setTimestamp(4, Timestamp.valueOf(order.getOrderTimestamp()));
            statement.setDouble(5, order.getTotalAmount());
            statement.executeUpdate();
        } finally {
            if (statement != null) statement.close();
        }
    }

    public void saveOrderItems(List<OrderItem> items, Connection connection) throws SQLException {
        String sql = "INSERT INTO order_items(order_id, drink_id, quantity, price_at_time_of_order, item_total) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (OrderItem item : items) {
                statement.setString(1, item.getOrderIdFk());
                statement.setString(2, item.getDrinkId());
                statement.setInt(3, item.getQuantity());
                statement.setDouble(4, item.getPriceAtTimeOfOrder());
                statement.setDouble(5, item.getItemTotal());
                statement.addBatch();
            }
            statement.executeBatch();
        } finally {
            if (statement != null) statement.close();
        }
    }

    private Order mapRowToOrder(ResultSet resultSet) throws SQLException {
        return new Order(
                resultSet.getString("order_id"),
                resultSet.getString("customer_id"),
                resultSet.getString("branch_id"),
                resultSet.getTimestamp("order_timestamp").toLocalDateTime(),
                new ArrayList<>(),
                resultSet.getDouble("total_amount")
        );
    }

    private OrderItem mapRowToOrderItem(ResultSet resultSet) throws SQLException {
        return new OrderItem(
                resultSet.getInt("order_item_id"),
                resultSet.getString("order_id"),
                resultSet.getString("drink_id"),
                null,
                resultSet.getInt("quantity"),
                resultSet.getDouble("price_at_time_of_order"),
                resultSet.getDouble("item_total")
        );
    }

    public Optional<Order> findbyId(String id, Connection... optionalConnection) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(optionalConnection);
            statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapRowToOrder(resultSet));
            }
        } finally {
            closeResources(resultSet, statement, connection, optionalConnection);
        }

        return Optional.empty();
    }

    public List<OrderItem> findItemsByOrderId(String orderId, Connection... optionalConnection) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(optionalConnection);
            statement = connection.prepareStatement(sql);
            statement.setString(1, orderId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                items.add(mapRowToOrderItem(resultSet));
            }
        } finally {
            closeResources(resultSet, statement, connection, optionalConnection);
        }

        return items;
    }

    public List<Order> findByBranchId(String branchId, Connection... optionalConnection) throws SQLException {
        String sql = "SELECT * FROM orders WHERE branch_id = ?";
        List<Order> orders = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(optionalConnection);
            statement = connection.prepareStatement(sql);
            statement.setString(1, branchId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                orders.add(mapRowToOrder(resultSet));
            }
        } finally {
            closeResources(resultSet, statement, connection, optionalConnection);
        }

        return orders;
    }

    public List<Order> findByCustomerId(String customerId, Connection... optionalConnection) throws SQLException {
        String sql = "SELECT * FROM orders WHERE customer_id = ?";
        List<Order> orders = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(optionalConnection);
            statement = connection.prepareStatement(sql);
            statement.setString(1, customerId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                orders.add(mapRowToOrder(resultSet));
            }
        } finally {
            closeResources(resultSet, statement, connection, optionalConnection);
        }

        return orders;
    }

    public List<Order> findAll(Connection... optionalConnection) throws SQLException {
        String sql = "SELECT * FROM orders";
        List<Order> orders = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(optionalConnection);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                orders.add(mapRowToOrder(resultSet));
            }
        } finally {
            closeResources(resultSet, statement, connection, optionalConnection);
        }

        return orders;
    }
}

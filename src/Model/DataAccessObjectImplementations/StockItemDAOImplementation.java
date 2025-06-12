package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IStockItemDAO;
import Model.DataEntities.StockItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StockItemDAOImplementation extends BaseDAO implements IStockItemDAO {

    private static final String SAVE_OR_UPDATE_SQL =
            "INSERT INTO stock_items (branch_id, drink_id, quantity, minimum_threshold) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "quantity = VALUES(quantity), " +
                    "minimum_threshold = VALUES(minimum_threshold)";

    private static final String FIND_BY_PK_SQL =
            "SELECT branch_id, drink_id, quantity, minimum_threshold " +
                    "FROM stock_items WHERE branch_id = ? AND drink_id = ?";

    private static final String FIND_BY_BRANCH_SQL =
            "SELECT branch_id, drink_id, quantity, minimum_threshold " +
                    "FROM stock_items WHERE branch_id = ?";

    private static final String FIND_LOW_STOCK_SQL =
            "SELECT si.branch_id, si.drink_id, si.quantity, si.minimum_threshold " +
                    "FROM stock_items si " +
                    "WHERE si.quantity < si.minimum_threshold AND si.minimum_threshold > 0";

    private static final String UPDATE_QUANTITY_SQL =
            "UPDATE stock_items SET quantity = ? WHERE branch_id = ? AND drink_id = ?";

    @Override
    public void saveOrUpdate(StockItem stockItem, Connection... conns) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(SAVE_OR_UPDATE_SQL);
            pstmt.setString(1, stockItem.getBranchId());
            pstmt.setString(2, stockItem.getDrinkId());
            pstmt.setInt(3, stockItem.getQuantity());
            pstmt.setInt(4, stockItem.getMinimumThreshold());
            pstmt.executeUpdate();
        } finally {
            closeResources(pstmt, conn, conns);
        }
    }

    @Override
    public Optional<StockItem> findByBranchAndDrink(String branchId, String drinkId, Connection... conns) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(FIND_BY_PK_SQL);
            pstmt.setString(1, branchId);
            pstmt.setString(2, drinkId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new StockItem(
                        rs.getString("branch_id"),
                        rs.getString("drink_id"),
                        rs.getInt("quantity"),
                        rs.getInt("minimum_threshold")
                ));
            }
        } finally {
            closeResources(rs, pstmt, conn, conns);
        }

        return Optional.empty();
    }

    @Override
    public List<StockItem> findByBranch(String branchId, Connection... conns) throws SQLException {
        List<StockItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(FIND_BY_BRANCH_SQL);
            pstmt.setString(1, branchId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                items.add(new StockItem(
                        rs.getString("branch_id"),
                        rs.getString("drink_id"),
                        rs.getInt("quantity"),
                        rs.getInt("minimum_threshold")
                ));
            }
        } finally {
            closeResources(rs, pstmt, conn, conns);
        }

        return items;
    }

    @Override
    public List<StockItem> findAllLowStock(Connection... conns) throws SQLException {
        List<StockItem> items = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection(conns);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(FIND_LOW_STOCK_SQL);

            while (rs.next()) {
                items.add(new StockItem(
                        rs.getString("branch_id"),
                        rs.getString("drink_id"),
                        rs.getInt("quantity"),
                        rs.getInt("minimum_threshold")
                ));
            }
        } finally {
            closeResources(rs, stmt, conn, conns);
        }

        return items;
    }

    @Override
    public void updateStockQuantity(String branchId, String drinkId, int newQuantity, Connection... conns) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(UPDATE_QUANTITY_SQL);
            pstmt.setInt(1, newQuantity);
            pstmt.setString(2, branchId);
            pstmt.setString(3, drinkId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Stock item not found for update or quantity unchanged: " + branchId + "/" + drinkId);
            }
        } finally {
            closeResources(pstmt, conn, conns);
        }
    }
}


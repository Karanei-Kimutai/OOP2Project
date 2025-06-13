package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IDrinkDAO;
import Model.DataEntities.Drink;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DrinkDAOImplementation extends BaseDAO implements IDrinkDAO {
    private static final String INSERT_SQL = "INSERT INTO drinks (drink_id, name, brand, price) VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT drink_id, name, brand, price FROM drinks WHERE drink_id = ?";
    private static final String FIND_ALL_SQL = "SELECT drink_id, name, brand, price FROM drinks";
    private static final String UPDATE_SQL = "UPDATE drinks SET name = ?, brand = ?, price = ? WHERE drink_id = ?";

    @Override
    public void add(Drink drink, Connection... conns) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(INSERT_SQL);
            pstmt.setString(1, drink.getId());
            pstmt.setString(2, drink.getName());
            pstmt.setString(3, drink.getBrand());
            pstmt.setDouble(4, drink.getPrice());
            pstmt.executeUpdate();
        } finally {
            closeResources(pstmt, conn, conns);
        }
    }

    @Override
    public Optional<Drink> findById(String drinkId, Connection... conns) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(FIND_BY_ID_SQL);
            pstmt.setString(1, drinkId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Drink(
                        rs.getString("drink_id"),
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getDouble("price")
                ));
            }
        } finally {
            closeResources(rs, pstmt, conn, conns);
        }
        return Optional.empty();
    }

    @Override
    public List<Drink> findAll(Connection... conns) throws SQLException {
        List<Drink> drinks = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection(conns);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(FIND_ALL_SQL);
            while (rs.next()) {
                drinks.add(new Drink(
                        rs.getString("drink_id"),
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getDouble("price")
                ));
            }
        } finally {
            closeResources(rs, stmt, conn, conns);
        }
        return drinks;
    }

    @Override
    public void update(Drink drink, Connection... conns) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection(conns);
            pstmt = conn.prepareStatement(UPDATE_SQL);
            pstmt.setString(1, drink.getName());
            pstmt.setString(2, drink.getBrand());
            pstmt.setDouble(3, drink.getPrice());
            pstmt.setString(4, drink.getId()); // The WHERE clause parameter
            pstmt.executeUpdate();
        } finally {
            closeResources(pstmt, conn, conns);
        }
    }
}
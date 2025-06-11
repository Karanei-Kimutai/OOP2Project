package Model.DataAccessObjectImplementations;

import Model.DataAccessObjectInterfaces.IDrinkDAO;
import Model.DataEntities.Drink;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DrinkDAOImplementation extends BaseDAO implements IDrinkDAO {
    @Override
    public void add(Drink drink, Connection... conns) throws SQLException{
        String sql="INSERT INTO drinks(drink_id,name,brand,price) VALUES (?,?,?,?)";
        Connection conn=null;
        PreparedStatement pstmt=null;
        try{
            conn=getConnection(conns);
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,drink.getId());
            pstmt.setString(2,drink.getName());
            pstmt.setString(3,drink.getBrand());
            pstmt.setDouble(4,drink.getPrice());
            pstmt.executeUpdate();
        }finally {
            closeResources(pstmt,conn,conns);
        }
    }

    @Override
    public Optional<Drink> findById(String drinkId,Connection... conns) throws SQLException{
        String sql="SELECT drink_id, name, brand, price FROM drinks WHERE drink_id=?";
        Connection conn=null;
        PreparedStatement psmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            psmt=conn.prepareStatement(sql);
            psmt.setString(1,drinkId);
            rs=psmt.executeQuery();
            if(rs.next()){
                return Optional.of(new Drink(rs.getString("drink_id"),rs.getString("name"),rs.getString("brand"),rs.getDouble("price")));
            }
        }finally {
            closeResources(rs,psmt,conn,conns);
        }
        return Optional.empty();
    }
    @Override
    public List<Drink> findAll(Connection... conns) throws SQLException{
        String sql = "SELECT drink_id, name, brand, price FROM drinks";
        List<Drink> drinks=new ArrayList<>();
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        try{
            conn=getConnection(conns);
            stmt=conn.createStatement();
            rs=stmt.executeQuery(sql);
            while(rs.next()){
                drinks.add(new Drink(rs.getString("drink_id"), rs.getString("name"), rs.getString("brand"), rs.getDouble("price")));
            }
        }finally {
            closeResources(rs,stmt,conn,conns);
        }
        return drinks;
    }

    @Override
    public void update(Drink drink,Connection... conns) throws SQLException{
        String sql = "UPDATE drinks SET name = ?, brand = ?, price = ? WHERE drink_id = ?";
        Connection conn=null;
        PreparedStatement psmt=null;
        try{
            conn=getConnection(conns);
            psmt=conn.prepareStatement(sql);
            psmt.setString(1,drink.getName());
            psmt.setString(2,drink.getBrand());
            psmt.setDouble(3,drink.getPrice());
            psmt.setString(4,drink.getId());
            psmt.executeUpdate();
        }finally {
            closeResources(psmt,conn,conns);
        }
    }
}

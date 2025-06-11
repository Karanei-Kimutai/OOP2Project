package Model.DataAccessObjectInterfaces;

import Model.DataEntities.Drink;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IDrinkDAO {
    void add(Drink drink, Connection... conn) throws SQLException;
    Optional<Drink> findById(String drinkId, Connection... conn) throws SQLException;
    List<Drink> findAll(Connection... conn) throws SQLException;
    void update(Drink drink,Connection... conn) throws SQLException;
    //Optional
   // void delete(String drinkId) throws SQLException;

}

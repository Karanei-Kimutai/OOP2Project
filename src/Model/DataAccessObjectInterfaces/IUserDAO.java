package Model.DataAccessObjectInterfaces;

import Model.DataEntities.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IUserDAO {
    void add(User user, Connection... conn) throws SQLException;
    Optional<User> findByUsername(String username, Connection... conn) throws SQLException;
    List<User> findAll(Connection... conn) throws SQLException; // NEW
    void delete(String username, Connection... conn) throws SQLException; // NEW
}


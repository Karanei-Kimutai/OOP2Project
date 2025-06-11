package Model.DataAccessObjectInterfaces;

import Model.DataEntities.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface IUserDAO {
    void add(User user, Connection... conn) throws SQLException;
    Optional<User> findByUserName(String username,Connection... conn) throws SQLException;

}


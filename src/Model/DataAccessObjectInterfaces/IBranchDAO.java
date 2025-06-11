package Model.DataAccessObjectInterfaces;

import Model.DataEntities.Branch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IBranchDAO {
    void add (Branch branch, Connection... conn) throws SQLException;
    Optional<Branch> findById(String branchId,Connection... conn) throws SQLException;
    List<Branch> findAll(Connection... conn) throws SQLException;

}

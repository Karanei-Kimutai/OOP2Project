package Model.DataAccessObjectInterfaces;

import Model.DataEntities.StockItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IStockItemDAO {
    void saveOrUpdate(StockItem stockItem, Connection... conn) throws SQLException;
    Optional<StockItem> findByBranchAndDrink(String branchId,String drinkId,Connection... conn) throws SQLException;
    List<StockItem> findByBranch(String branchId,Connection... conn) throws SQLException;
    List<StockItem> findAllLowStock(Connection... conn) throws SQLException;// Simplified, enrichment happens in service
    void updateStockQuantity(String branchId,String drinkId,int newQuantity,Connection...conn) throws SQLException;

}

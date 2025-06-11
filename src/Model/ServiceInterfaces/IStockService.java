package Model.ServiceInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface IStockService extends Remote {
    void setStockLevel(String branchId,String drinkId,int quantity) throws RemoteException,Exception;
    int getStockLevel(String branchId,String drinkId) throws RemoteException,Exception;
    void setStockThreshold(String branchId,String drinkId,int threshold) throws RemoteException,Exception;
    Map<String,Integer> getStockForBranch(String branchId) throws RemoteException,Exception; // DrinkID -> Quantity
    void transferStock(String sourceBranchId,String destinationBranchId,String drinkId,int quantity) throws RemoteException,Exception;
    void processSaleTransactionally(String branchId, Map<String, Integer> itemsSold, Connection conn) throws RemoteException, Exception; // For use within OrderService transaction
    List<String> checkLowStockLevelsGlobally() throws RemoteException, Exception;
}

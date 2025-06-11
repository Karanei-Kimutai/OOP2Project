package Model.ServiceInterfaces;

import Model.DataEntities.Order;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface IOrderService extends Remote {
    Order placeOrder(String customerId, String branchId, Map<String, Integer> itemsToOrder) throws RemoteException, Exception; // itemsToOrder: DrinkID -> Quantity
    Order getOrderById(String orderId) throws RemoteException,Exception;
    List<Order> getOrdersByBranch(String branchId) throws RemoteException,Exception;
    List<Order> getOrdersByCustomer(String customerId) throws RemoteException, Exception;

}

package Model.ServiceImplementations;

import Model.DataAccessObjectInterfaces.IBranchDAO;
import Model.DataAccessObjectInterfaces.IDrinkDAO;
import Model.DataAccessObjectInterfaces.IOrderDAO;
import Model.DataEntities.Drink;
import Model.DataEntities.Order;
import Model.DataEntities.OrderItem;
import Model.ServiceInterfaces.IOrderService;
import Model.ServiceInterfaces.IStockService;
import Model.UtilitiesandServerEntryPoint.DatabaseManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderServiceImplementation extends UnicastRemoteObject implements IOrderService {
    private final IOrderDAO orderDAO;
    private final IStockService stockService;
    private final IDrinkDAO drinkDAO;
    private final IBranchDAO branchDAO;
    public OrderServiceImplementation(IOrderDAO orderDAO, IStockService stockService, IDrinkDAO drinkDAO, IBranchDAO branchDAO) throws RemoteException{
        super();
        this.orderDAO=orderDAO;
        this.stockService=stockService;
        this.drinkDAO=drinkDAO;
        this.branchDAO=branchDAO;
    }
    @Override
    public Order placeOrder(String customerId, String branchId, Map<String, Integer> itemsToOrder) throws RemoteException, Exception {
        if(customerId==null||customerId.isEmpty()){
            throw new IllegalArgumentException("Customer ID required");
        }
        if(!branchDAO.findById(branchId).isPresent()){
            throw new Exception("Branch "+branchId+" not found.");
        }
        if(itemsToOrder==null||itemsToOrder.isEmpty()){
            throw new IllegalArgumentException("Order must have items");
        }
        Connection connection=null;
        String orderId="ORD-"+ UUID.randomUUID().toString().substring(0,12).toUpperCase();
        List<OrderItem> orderItemsList=new ArrayList<>();
        double calculatedTotal=0;
        try{
             for(Map.Entry<String,Integer> entry:itemsToOrder.entrySet()){// Prepare items & total (read-only)
                Drink drink=drinkDAO.findById(entry.getKey()).orElseThrow(()->new Exception("Drink "+entry.getKey()+" not found."));
                if(entry.getValue()<=0){
                    throw new IllegalArgumentException("Quantity for "+entry.getKey()+" must be positive.");
                }
                OrderItem orderItem=new OrderItem(drink.getId(),drink.getName(),entry.getValue(),drink.getPrice());
                orderItem.setOrderIdFk(orderId);// Set FK for items before saving
                 orderItemsList.add(orderItem);
                 calculatedTotal+=orderItem.getItemTotal();
             }
             connection=DatabaseManager.getConnection();
             connection.setAutoCommit(false);//Start the transaction
            stockService.processSaleTransactionally(branchId,itemsToOrder,connection);//Decrement stock within this transaction
            Order newOrder=new Order(orderId,customerId,branchId, LocalDateTime.now(),orderItemsList,calculatedTotal);
            orderDAO.saveOrderHeader(newOrder,connection);
            connection.commit();//TRANSACTION COMMIT
            System.out.println("Order placed and committed:"+orderId);
            newOrder.setItems(orderItemsList);
            return newOrder;
        }catch (Exception e){
            if(connection!=null){
                try{
                    connection.rollback();
                } catch (SQLException ex) {

                }
            }
            throw e instanceof RemoteException ? e:new RemoteException("Error placing order: "+e.getMessage(), e);
        }finally {
             if(connection!=null){
                 try{
                     connection.setAutoCommit(true);
                     connection.close();
                 } catch (SQLException e) {

                 }
             }
        }
    }

    private Order enrichOrder(Order order) throws RemoteException,Exception{
        if(order==null){
            return null;
        }
        List<OrderItem> items=orderDAO.findItemsByOrderId(order.getOrderId());
        for(OrderItem item:items){
            Drink drink=drinkDAO.findById(item.getDrinkId()).orElse(new Drink(item.getDrinkId(),"Unknown","N/A",0));
            item.setDrinkName(drink.getName());
        }
        order.setItems(items);
        return order;
    }
    @Override
    public Order getOrderById(String orderId) throws RemoteException, Exception {
        try{
            return enrichOrder(orderDAO.findbyId(orderId).orElseThrow(()->new Exception("Order "+orderId+" not found.")));
        }catch (SQLException e){
            throw new RemoteException("DB error finding order.",e);
        }
    }

    @Override
    public List<Order> getOrdersByBranch(String branchId) throws RemoteException, Exception {
        try{
            List<Order> orders=orderDAO.findByBranchId(branchId);
            List<Order> enriched=new ArrayList<>();
            for(Order o:orders){
                enriched.add(enrichOrder(o));
            }
            return enriched;
        }catch (SQLException e){
            throw new RemoteException("DB error finding orders by branch.",e);
        }
    }

    @Override
    public List<Order> getOrdersByCustomer(String customerId) throws RemoteException, Exception {
        try{
            List<Order> orders=orderDAO.findByCustomerId(customerId);
            List<Order> enriched=new ArrayList<>();
            for(Order o:orders){
                enriched.add(enrichOrder(o));
            }
            return enriched;
        }catch(SQLException e){
            throw new RemoteException("DB error finding orders by customer.",e);
        }
    }
}

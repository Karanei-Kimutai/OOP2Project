package Model.ServiceImplementations;

import Model.DataAccessObjectInterfaces.IBranchDAO;
import Model.DataAccessObjectInterfaces.IDrinkDAO;
import Model.DataAccessObjectInterfaces.IStockItemDAO;
import Model.DataEntities.Branch;
import Model.DataEntities.Drink;
import Model.DataEntities.StockItem;
import Model.ServiceInterfaces.IStockService;
import Model.UtilitiesandServerEntryPoint.DatabaseManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StockServiceImplementation extends UnicastRemoteObject implements IStockService {
    private final IStockItemDAO stockItemDAO;
    private final IBranchDAO branchDAO;
    private final IDrinkDAO drinkDAO;
    public StockServiceImplementation(IStockItemDAO siDAO, IBranchDAO bDAO, IDrinkDAO dDAO) throws RemoteException{
        super();
        this.stockItemDAO=siDAO;
        this.branchDAO=bDAO;
        this.drinkDAO=dDAO;
    }
    private void validateBranchAndDrink(String branchID,String drinkID,Connection... conns) throws Exception, SQLException {
        if(!branchDAO.findById(branchID,conns).isPresent()){
            throw new Exception("Branch "+branchID+" not found");
        }
        if(!drinkDAO.findById(drinkID,conns).isPresent()){
            throw new Exception("Drink "+drinkID+" not found");
        }
    }
    @Override
    public void setStockLevel(String branchId, String drinkId, int quantity) throws RemoteException, Exception {
        if(quantity<0){
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        try{
            validateBranchAndDrink(branchId,drinkId);
            StockItem item= stockItemDAO.findByBranchAndDrink(branchId,drinkId).orElse(new StockItem(branchId,drinkId,0,0));
            item.setQuantity(quantity);
            stockItemDAO.saveOrUpdate(item);
            System.out.println("Stock level set for "+drinkId+" at "+branchId+" to "+quantity);
        }catch(SQLException e){
            throw new RemoteException("DB error setting stock level",e);
        }
    }

    @Override
    public int getStockLevel(String branchId, String drinkId) throws RemoteException, Exception {
        try{
            validateBranchAndDrink(branchId,drinkId);
            return stockItemDAO.findByBranchAndDrink(branchId,drinkId).map(StockItem::getQuantity).orElse(0);
        }catch (SQLException e){
            throw new RemoteException("DB error getting stock levels",e);
        }
    }

    @Override
    public void setStockThreshold(String branchId, String drinkId, int threshold) throws RemoteException, Exception {
        if(threshold<0){
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        try{
            validateBranchAndDrink(branchId,drinkId);
            StockItem item=stockItemDAO.findByBranchAndDrink(branchId,drinkId).orElse(new StockItem(branchId,drinkId,0,0));
            item.setMinimumThreshold(threshold);
            stockItemDAO.saveOrUpdate(item);
            System.out.println("Threshold set for "+drinkId+" at "+branchId+" to "+threshold);
        }catch(SQLException e){
           throw new RemoteException("DB error setting threshold",e);
        }
    }

    @Override
    public Map<String, Integer> getStockForBranch(String branchId) throws RemoteException, Exception {
        try{
            if(!branchDAO.findById(branchId).isPresent()){
                throw new Exception("Branch "+branchId+" not found");
            }
            return stockItemDAO.findByBranch(branchId).stream().collect(Collectors.toMap(StockItem::getDrinkId, StockItem::getQuantity));
        }catch(SQLException e){
            throw new RemoteException("DB error getting stock for branch.",e);
        }
    }

    @Override
    public void transferStock(String sourceBranchId, String destinationBranchId, String drinkId, int quantity) throws RemoteException, Exception {
        if(quantity<=0){
            throw new IllegalArgumentException("Transfer quantity must be positive.");
        }
        if(sourceBranchId.equals(destinationBranchId)){
            throw new IllegalArgumentException("Source/Destination branches are the same");
        }
        Connection conn=null;
        try{
            conn= DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            validateBranchAndDrink(sourceBranchId,drinkId,conn);
            if(!branchDAO.findById(destinationBranchId,conn).isPresent()) {
                throw new Exception("Destination branch " + destinationBranchId + " not found");
            }
            StockItem source=stockItemDAO.findByBranchAndDrink(sourceBranchId,drinkId,conn).orElseThrow(()->new Exception("Stock for "+drinkId+" at "+sourceBranchId+" not found"));
            if(source.getQuantity()<quantity){
                throw new Exception("Insufficient stock at "+sourceBranchId+". Available: "+source.getQuantity());
            }
            source.setQuantity(source.getQuantity()-quantity);
            stockItemDAO.saveOrUpdate(source,conn);
            StockItem destination=stockItemDAO.findByBranchAndDrink(destinationBranchId,drinkId,conn).orElse(new StockItem(destinationBranchId,drinkId,0,source.getMinimumThreshold())); // Keep threshold from source or default
            destination.setQuantity(destination.getQuantity()+quantity);
            stockItemDAO.saveOrUpdate(destination,conn);
            stockItemDAO.saveOrUpdate(destination,conn);
            conn.commit();
            System.out.println("Transferred "+quantity+" of "+drinkId+" from "+sourceBranchId+" to "+destinationBranchId);
        }catch (Exception e){
            if(conn!=null){
                try{
                    conn.rollback();
                }catch (SQLException ex){
                    throw e instanceof RemoteException? e:new RemoteException("Error transferring stock",e );
                }
            }
        }finally {
            if(conn!=null){
                try{
                    conn.setAutoCommit(true);
                    conn.close();
                }catch(SQLException ex){

                }
            }
        }
    }

    @Override
    public void processSaleTransactionally(String branchId, Map<String, Integer> itemsSold, Connection conn) throws RemoteException, Exception {
        // This method MUST be called within an existing transaction (conn provided)
        if(conn==null){
            throw new IllegalArgumentException("Connection cannot be null for transactional sale processing");
        }
        try{
            if(!branchDAO.findById(branchId,conn).isPresent()) {
                throw new Exception("Branch " + branchId + " not found.");
            }
            for(Map.Entry<String,Integer> entry:itemsSold.entrySet()){
                String drinkID=entry.getKey();
                int quantitySold=entry.getValue();
                if(!drinkDAO.findById(drinkID,conn).isPresent()){
                    throw new Exception("Drink "+drinkID+" not found");
                }
                if(quantitySold<=0){
                    throw new IllegalArgumentException("Quantity sold for "+drinkID+" must be positive.");
                }
                StockItem stock=stockItemDAO.findByBranchAndDrink(branchId,drinkID,conn).orElseThrow(()->new Exception("Stock for "+drinkID+" at "+branchId+" not found."));
                if(stock.getQuantity()<quantitySold){
                    throw new Exception("Insufficient stock for "+drinkID+" at "+branchId+". Available stock: "+stock.getQuantity()+", Required: "+quantitySold);
                }
                stock.setQuantity(stock.getQuantity()-quantitySold);
                stockItemDAO.saveOrUpdate(stock,conn);
            }
            System.out.println("Stock decremented transactionally at "+branchId);
        }catch (SQLException e){
            throw new RemoteException("DB error processing the stock part of the sale.",e);

        }
    }

    @Override
    public List<String> checkLowStockLevelsGlobally() throws RemoteException, Exception {
        List<String> warnings=new ArrayList<>();
        try{
            List<StockItem> lowItems=stockItemDAO.findAllLowStock();
            for(StockItem item: lowItems){
                Branch b=branchDAO.findById(item.getBranchId()).orElse(new Branch(item.getBranchId(),"Unknown","N/A"));
                Drink d=drinkDAO.findById(item.getDrinkId()).orElse(new Drink(item.getDrinkId(),"Unknown","N/A",0));
                warnings.add(String.format("LOW STOCK: Branch '%s' (%s) - Drink '%s' (%s). Qty: %d, Threshold: %d", b.getName(), b.getId(), d.getName(), d.getId(), item.getQuantity(), item.getMinimumThreshold()));
            }
        }catch(SQLException e){
            throw new RemoteException("DB error checking low stock",e);
        }
        if(warnings.isEmpty()){
            warnings.add("All stock levels are currently above threshold or no thresholds are set.");
        }
        return warnings;
    }
}

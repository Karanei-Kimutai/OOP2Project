package Model.ServiceImplementations;

import Model.DataAccessObjectInterfaces.IDrinkDAO;
import Model.DataAccessObjectInterfaces.IStockItemDAO;
import Model.DataEntities.Drink;
import Model.DataEntities.StockItem;
import Model.ServiceInterfaces.IDrinkService;
import Model.UtilitiesandServerEntryPoint.DatabaseManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EmptyStackException;
import java.util.List;

public class DrinkServiceImplementation extends UnicastRemoteObject implements IDrinkService {
    private final IDrinkDAO drinkDAO;
    private final IStockItemDAO stockItemDAO;
    public static final String HQ_BRANCH_ID_CONST="Nairobi";//Central definition
    public DrinkServiceImplementation(IDrinkDAO dDAO, IStockItemDAO siDAO) throws RemoteException{
        super();
        this.drinkDAO=dDAO;
        this.stockItemDAO=siDAO;
    }

    @Override
    public void addDrink(Drink drink) throws RemoteException, Exception {
        if(drink==null||drink.getId()==null||drink.getId().isEmpty()){
            throw new IllegalArgumentException("Drink/ID required.");
        }
        Connection conn=null;
        try{
            conn= DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            if(drinkDAO.findById(drink.getId(),conn).isPresent()){
                throw new Exception("Drink ID "+drink.getId()+" exists");
            }
            drinkDAO.add(drink,conn);
            if(drink.getInitialStock()>0) {//Add initial stock to HQ
                stockItemDAO.saveOrUpdate(new StockItem(HQ_BRANCH_ID_CONST, drink.getId(), drink.getInitialStock(), 0), conn);
            }
            conn.commit();
            System.out.println("Drink added: " +drink.getName());
        }catch(SQLException e){
            throw new RemoteException("DB error finding drink",e);
        }finally {
            if(conn!=null){
                try{
                    conn.setAutoCommit(true);
                    conn.close();
                }catch (SQLException ex){

                }
            }
        }
    }

    @Override
    public Drink getDrinkById(String id) throws RemoteException, Exception {
        try{
            return drinkDAO.findById(id).orElseThrow(()->new Exception("Drink ID "+id+" not found"));
        }catch (SQLException e){
            throw new RemoteException("DB error finding drink.",e);
        }
    }

    @Override
    public List<Drink> getAllDrinks() throws RemoteException, Exception {
        try{
            return drinkDAO.findAll();
        }catch (SQLException e){
            throw new RemoteException("DB error getting drinks.",e);
        }
    }

    @Override
    public void updateDrink(Drink drink) throws RemoteException, Exception {
        if(drink==null||drink.getId()==null){
            throw new IllegalArgumentException("Drink/ID for update required.");
        }
        try{
            if(!drinkDAO.findById(drink.getId()).isPresent()){
                throw new Exception("Drink ID "+drink.getName()+" not found for update.");
            }
            drinkDAO.update(drink);
            System.out.println("Drink updated: "+drink.getName());
        }catch (SQLException e){
            throw new RemoteException("DB error updating drink.",e);
        }
    }
}

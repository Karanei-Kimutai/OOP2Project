package Model.ServiceInterfaces;

import Model.DataEntities.Drink;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IDrinkService extends Remote {
    void addDrink(Drink drink) throws RemoteException, Exception;
    Drink getDrinkById(String drinkId) throws RemoteException, Exception;
    List<Drink> getAllDrinks() throws RemoteException,Exception;
    void updateDrink(Drink drink) throws RemoteException,Exception;

}
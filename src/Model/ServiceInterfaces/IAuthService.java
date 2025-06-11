package Model.ServiceInterfaces;

import Model.DataEntities.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAuthService extends Remote {
    User login(String username,String password) throws RemoteException, Exception;
    void addUser(String username,String password, User.UserRole role) throws RemoteException, Exception;
}

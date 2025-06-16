package Model.ServiceInterfaces;

import Model.DataEntities.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IAuthService extends Remote {
    User login(String username, String password) throws RemoteException, Exception;
    void addUser(String username, String password, User.UserRole role, String branchId) throws RemoteException, Exception; // Modified
    List<User> getAllUsers() throws RemoteException, Exception; // NEW
    void removeUser(String username) throws RemoteException, Exception; // NEW
}

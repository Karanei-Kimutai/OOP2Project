package Model.ServiceImplementations;

import Model.DataAccessObjectInterfaces.IUserDAO;
import Model.DataEntities.User;
import Model.ServiceInterfaces.IAuthService;
import Model.UtilitiesandServerEntryPoint.PasswordUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Optional;

public class AuthServiceImplementation extends UnicastRemoteObject implements IAuthService {
    private final IUserDAO userDAO;
    protected AuthServiceImplementation(IUserDAO userDAO) throws RemoteException,Exception {
        super();
        this.userDAO=userDAO;
    }
    @Override
    public User login(String username,String password) throws RemoteException,Exception{
        try{
            Optional<User> userOpt=userDAO.findByUserName(username);
            if(!userOpt.isPresent()){
                throw new Exception("Invalid username or password.");
            }
            User user=userOpt.get();
            if(!PasswordUtil.checkPassword(password,user.getHashedPassword())){
                throw new Exception("Invalid username or password");
            }
            System.out.println("User logged in: "+username);
            return new User(user.getUsername(),null,user.getRole());
        }catch (SQLException e){
            throw new RemoteException("DB error during login",e);
        }
    }

    @Override
    public void addUser(String username, String password, User.UserRole role) throws RemoteException, Exception {
     if(username==null||username.isEmpty()||password==null||password.isEmpty()){
         throw new IllegalArgumentException("Username/Password required");
     }
     try{
         if(userDAO.findByUserName(username).isPresent()){
             throw new IllegalArgumentException("Username '" + username + "' already exists.");
         }
         String hashedPassword=PasswordUtil.hashPassword(password);
         userDAO.add(new User(username,hashedPassword,role));
         System.out.println("User added: "+username);
     }catch(SQLException e){
         throw new RemoteException("DB error adding user.",e);
     }
    }
}

package Model.ServiceImplementations;

import Model.DataAccessObjectInterfaces.IUserDAO;
import Model.DataEntities.User;
import Model.ServiceInterfaces.IAuthService;
import Model.UtilitiesandServerEntryPoint.PasswordUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AuthServiceImplementation extends UnicastRemoteObject implements IAuthService {
    private final IUserDAO userDAO;
    public AuthServiceImplementation(IUserDAO userDAO) throws RemoteException { super(); this.userDAO = userDAO; }

    @Override
    public User login(String username, String password) throws RemoteException, Exception {
        try {
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (!userOpt.isPresent()) {
                throw new Exception("Invalid username or password.");
            }
            User user = userOpt.get();
            if (!PasswordUtil.checkPassword(password, user.getHashedPassword())) {
                throw new Exception("Invalid username or password.");
            }
            System.out.println("User logged in: " + username);
            // Return user object without the hashed password for security
            return new User(user.getUsername(), null, user.getRole(), user.getBranchId());
        } catch (SQLException e) {
            throw new RemoteException("DB error during login.", e);
        }
    }

    @Override
    public void addUser(String username, String password, User.UserRole role, String branchId) throws RemoteException, Exception {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        // A branch is required for all roles except ADMIN
        if (role != User.UserRole.ADMIN && (branchId == null || branchId.isEmpty())) {
            throw new IllegalArgumentException("Branch ID is required for non-admin users.");
        }
        try {
            if (userDAO.findByUsername(username).isPresent()) {
                throw new Exception("Username '" + username + "' already exists.");
            }
            String hashedPassword = PasswordUtil.hashPassword(password);
            // Admins are not tied to a specific branch, so their branchId can be null.
            String finalBranchId = (role == User.UserRole.ADMIN) ? null : branchId;
            userDAO.add(new User(username, hashedPassword, role, finalBranchId));
            System.out.println("User added: " + username);
        } catch (SQLException e) {
            throw new RemoteException("DB error adding user.", e);
        }
    }

    @Override
    public List<User> getAllUsers() throws RemoteException, Exception {
        try {
            return userDAO.findAll();
        } catch (SQLException e) {
            throw new RemoteException("DB error getting all users.", e);
        }
    }

    @Override
    public void removeUser(String username) throws RemoteException, Exception {
        // Business rule: prevent deletion of the main admin user
        if ("admin".equalsIgnoreCase(username)) {
            throw new Exception("Cannot remove the default admin user.");
        }
        try {
            userDAO.delete(username);
            System.out.println("User removed: " + username);
        } catch (SQLException e) {
            throw new RemoteException("DB error removing user.", e);
        }
    }
}

package Model.DataEntities;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID=105L;
    private String username;
    private String hashedPassword; // This will store the BCrypt hash
    private UserRole role;

    public enum UserRole{
        ADMIN, BRANCH_MANAGER,STAFF
        // CUSTOMER_PLACEHOLDER was for client-side enum, not for actual user roles in DB/server
    }

    public User(String username,String hashedPassword, UserRole role){
        this.username=username;
        this.hashedPassword=hashedPassword;
        this.role=role;
    }

    public String getUsername(){return username;}
    public String getHashedPassword(){return hashedPassword;}
    public UserRole getRole(){return role;}

    @Override
    public String toString() {
        return "User{username='" + username + "', role=" + role + "}";
    }
}

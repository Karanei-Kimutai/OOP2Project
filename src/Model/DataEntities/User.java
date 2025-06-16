package Model.DataEntities;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 105L;
    private String username;
    private String hashedPassword; // This will store the BCrypt hash
    private UserRole role;
    private String branchId; // NEW: Link user to a branch

    public enum UserRole { ADMIN, BRANCH_MANAGER, STAFF }

    public User(String username, String hashedPassword, UserRole role, String branchId) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
        this.branchId = branchId;
    }
    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public UserRole getRole() { return role; }
    public String getBranchId() { return branchId; } // NEW getter

    @Override
    public String toString() {
        return "User{username='" + username + "', role=" + role + ", branchId='" + branchId + "'}";
    }
}
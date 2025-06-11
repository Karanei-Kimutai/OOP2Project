package Model.UtilitiesandServerEntryPoint;
import org.mindrot.jbcrypt.BCrypt;
public class PasswordUtil {
    public static String hashPassword(String plainTextPassword){
    return BCrypt.hashpw(plainTextPassword,BCrypt.gensalt(12));
    }
    public static boolean checkPassword(String plainTextPassword,String hashedPasswordFromDB){
        if(plainTextPassword==null||hashedPasswordFromDB==null){return false;}
        try{
            return BCrypt.checkpw(plainTextPassword,hashedPasswordFromDB);
        }catch(IllegalArgumentException e){
            System.err.println("Password check failed:Invalid hash format for stored password. "+e.getMessage());
            return false;
        }
    }
}

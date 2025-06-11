package Model.ServiceInterfaces;

import Model.DataEntities.Branch;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IBranchService extends Remote {
    void addBranch(Branch branch) throws RemoteException, Exception;
    Branch getBranchById(String branchId) throws RemoteException,Exception;
    List<Branch> getAllBranches() throws RemoteException,Exception;

}

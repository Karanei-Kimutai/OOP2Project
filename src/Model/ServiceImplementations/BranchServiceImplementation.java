package Model.ServiceImplementations;

import Model.DataAccessObjectInterfaces.IBranchDAO;
import Model.DataEntities.Branch;
import Model.ServiceInterfaces.IBranchService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.EmptyStackException;
import java.util.List;

public class BranchServiceImplementation extends UnicastRemoteObject implements IBranchService{
    private final IBranchDAO branchDAO;
    public BranchServiceImplementation(IBranchDAO bDAO) throws RemoteException{
        super();
        this.branchDAO=bDAO;
    }
    @Override
    public void addBranch(Branch branch) throws RemoteException, Exception {
        if(branch==null||branch.getId()==null||branch.getId().isEmpty()){
            throw new IllegalArgumentException("Branch/ID required");
        }
        try{
            if(branchDAO.findById(branch.getId()).isPresent()){
                throw new Exception("Branch ID "+branch.getId()+" exists.");
            }
            branchDAO.add(branch);
            System.out.println("Branch added: "+branch.getName());
        }catch(SQLException e){
            throw new RemoteException("DB error adding branch",e);
        }
    }

    @Override
    public Branch getBranchById(String id) throws RemoteException, Exception {
        try{
            return branchDAO.findById(id).orElseThrow(()->new Exception("Branch ID "+id+" not found"));
        }catch(SQLException e){
            throw new RemoteException("DB error finding branch",e);
        }
    }

    @Override
    public List<Branch> getAllBranches() throws RemoteException, Exception {
        try{
            return branchDAO.findAll();
        } catch (SQLException e) {
            throw new RemoteException("DB error getting branches.", e);
        }
    }
}

package Model.ServiceImplementations;

import Model.DataAccessObjectInterfaces.IBranchDAO;
import Model.DataAccessObjectInterfaces.IOrderDAO;
import Model.DataEntities.Branch;
import Model.DataEntities.Order;
import Model.ServiceInterfaces.IReportingService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportingServiceImplementation extends UnicastRemoteObject implements IReportingService {
    private final IOrderDAO orderDAO;
    private final IBranchDAO branchDAO;
    protected ReportingServiceImplementation(IOrderDAO orderDAO,IBranchDAO branchDAO) throws RemoteException{
        super();
        this.orderDAO=orderDAO;
        this.branchDAO=branchDAO;
    }
    @Override
    public List<String> getCustomersByBranch(String branchId) throws RemoteException, Exception {
        try{
            if(!branchDAO.findById(branchId).isPresent()){
                throw new Exception("Branch "+branchId+" not found.");
            }
            return orderDAO.findByBranchId(branchId).stream().map(Order::getCustomerId).distinct().collect(Collectors.toList());
        }catch(SQLException e){
            throw new RemoteException("DB error getting customers by branch.", e);
        }
    }

    @Override
    public double getTotalSalesForBranch(String branchId) throws RemoteException, Exception {
        try{
            if(!branchDAO.findById(branchId).isPresent()){
                throw new Exception("Branch "+branchId+" not found.");
            }
            return orderDAO.findByBranchId(branchId).stream().mapToDouble(Order::getTotalAmount).sum();
        }catch (SQLException e){
            throw new RemoteException("DB error getting branch sales.", e);
        }
    }

    @Override
    public double getTotalBusinessSales() throws RemoteException, Exception {
        try{
            return orderDAO.findAll().stream().mapToDouble(Order::getTotalAmount).sum();
        } catch (SQLException e) {
            throw new RemoteException("DB error getting total business sales.",e);
        }
    }

    @Override
    public Map<String, String> generateBranchSalesReport(String branchId) throws RemoteException, Exception {
        try{
            Branch branch=branchDAO.findById(branchId).orElseThrow(()->new Exception("Branch "+branchId+" not found."));
            List<Order> orders=orderDAO.findByBranchId(branchId);
            Map<String,String> report=new HashMap<>();
            report.put("Branch ID",branch.getId());
            report.put("Branch Name",branch.getName());
            report.put("Total Sales",String.format("%.2f", orders.stream().mapToDouble(Order::getTotalAmount).sum()));
            report.put("Number of orders",String.valueOf(orders.size()));
            report.put("Distinct Customers",String.valueOf(orders.stream().map(Order::getCustomerId).distinct().count()));
            return report;
        }catch (SQLException e){
            throw new RemoteException("DB error generating branch report",e);
        }
    }

    @Override
    public Map<String, String> generateOverallBusinessReport() throws RemoteException, Exception {
        try{
            List<Branch> branches=branchDAO.findAll();
            List<Order> orders=orderDAO.findAll();
            Map<String,String> report=new HashMap<>();
            report.put("Overall Total Sales",String.format("%.2f", orders.stream().mapToDouble(Order::getTotalAmount).sum()));
            report.put("Overall Number of Orders",String.valueOf(orders.size()));
            report.put("Number of Branches",String.valueOf(branches.size()));
            StringBuilder stringBuilder=new StringBuilder();
            for(Branch b: branches){
                double sales=orders.stream().filter(o->o.getBranchId().equals(b.getId())).mapToDouble(Order::getTotalAmount).sum();
                stringBuilder.append(b.getName()).append(" (").append(b.getId()).append(" ):").append(String.format("%.2f", sales)).append("; ");
            }
            report.put("Sales by Branch",stringBuilder.toString());
            return report;
        }catch(SQLException e){
            throw new RemoteException("DB error generating overall report.",e);
        }
    }
}

package Model.ServiceInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface IReportingService extends Remote {
    List<String> getCustomersByBranch(String branchId) throws RemoteException,Exception;  // List of customer IDs/names
    double getTotalSalesForBranch(String branchId) throws RemoteException,Exception;
    double getTotalBusinessSales() throws RemoteException,Exception;
    Map<String, String> generateBranchSalesReport(String branchId) throws RemoteException, Exception; // Detailed report
    Map<String, String> generateOverallBusinessReport() throws RemoteException, Exception; // Detailed overall report

}

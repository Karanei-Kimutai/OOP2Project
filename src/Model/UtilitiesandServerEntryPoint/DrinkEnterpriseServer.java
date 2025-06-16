package Model.UtilitiesandServerEntryPoint;

import Model.DataAccessObjectImplementations.*;
import Model.DataAccessObjectInterfaces.*;
import Model.DataEntities.Branch;
import Model.DataEntities.Drink;
import Model.DataEntities.User;
import Model.ServiceImplementations.*;
import Model.ServiceInterfaces.IAuthService;
import Model.ServiceInterfaces.IBranchService;
import Model.ServiceInterfaces.IDrinkService;
import Model.ServiceInterfaces.IStockService;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class DrinkEnterpriseServer {

    public static final int RMI_PORT = 1099;
    public static final String HQ_BRANCH_ID_SVR = DrinkServiceImplementation.HQ_BRANCH_ID_CONST;

    public static void main(String[] args) {
        try {
            System.out.println("Starting Drink Enterprise RMI Server (JDBC Backend with BCrypt)...");

            // Start RMI registry
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("RMI Registry created/found on port " + RMI_PORT);

            // Initialize DAO implementations
            IDrinkDAO drinkDAO = new DrinkDAOImplementation();
            IBranchDAO branchDAO = new BranchDAOImplementation();
            IStockItemDAO stockItemDAO = new StockItemDAOImplementation();
            IUserDAO userDAO = new UserDAOImplementation();
            IOrderDAO orderDAO = new OrderDAOImplementation();

            // Initialize service implementations
            DrinkServiceImplementation drinkService = new DrinkServiceImplementation(drinkDAO, stockItemDAO);
            BranchServiceImplementation branchService = new BranchServiceImplementation(branchDAO);
            StockServiceImplementation stockService = new StockServiceImplementation(stockItemDAO, branchDAO, drinkDAO);
            OrderServiceImplementation orderService = new OrderServiceImplementation(orderDAO, stockService, drinkDAO, branchDAO);
            ReportingServiceImplementation reportingService = new ReportingServiceImplementation(orderDAO, branchDAO);
            AuthServiceImplementation authService = new AuthServiceImplementation(userDAO);

            // Set up initial data
            setupInitialData(drinkService, branchService, stockService, authService, userDAO, branchDAO, drinkDAO);

            // Register services
            Naming.rebind("rmi://localhost:" + RMI_PORT + "/AuthService", authService);
            Naming.rebind("rmi://localhost:" + RMI_PORT + "/DrinkService", drinkService);
            Naming.rebind("rmi://localhost:" + RMI_PORT + "/BranchService", branchService);
            Naming.rebind("rmi://localhost:" + RMI_PORT + "/StockService", stockService);
            Naming.rebind("rmi://localhost:" + RMI_PORT + "/OrderService", orderService);
            Naming.rebind("rmi://localhost:" + RMI_PORT + "/ReportingService", reportingService);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void setupInitialData(IDrinkService drinkService, IBranchService branchService, IStockService stockService, IAuthService authService, IUserDAO userDAO, IBranchDAO branchDAO, IDrinkDAO drinkDAO) {
        System.out.println("Setting up initial data (if necessary)...");

        try {
            // Add HQ branch first
            if (!branchDAO.findById(HQ_BRANCH_ID_SVR).isPresent()) {
                branchService.addBranch(new Branch(HQ_BRANCH_ID_SVR, "NairobiHQ", "Nairobi"));
            }

            // Add other branches
            if (!branchDAO.findById("Nakuru").isPresent()) {
                branchService.addBranch(new Branch("Nakuru", "NakuruBranch", "Nakuru"));
            }
            if (!branchDAO.findById("Mombasa").isPresent()) {
                branchService.addBranch(new Branch("Mombasa", "MombasaBranch", "Mombasa"));
            }

            // Add drinks (addDrink in service now handles initial HQ stock)
            if (!drinkDAO.findById("Coca-Cola500ml").isPresent()) {
                drinkService.addDrink(new Drink("Coca-Cola500ml", "Coca-Cola 500ml", "Coca-Cola", 60.00, 1000000));
            }
            if (!drinkDAO.findById("Pepsi500ml").isPresent()) {
                drinkService.addDrink(new Drink("Pepsi500ml", "Pepsi 500ml", "PepsiCo", 55.00, 800000));
            }
            if (!drinkDAO.findById("Fanta300ml").isPresent()) {
                drinkService.addDrink(new Drink("Fanta300ml", "Fanta 300ml", "Coca-Cola", 45.00, 700000));
            }

            // Initial stock transfer (idempotent-ish)
            transferInitial(stockService, "Nakuru", "Coca-Cola500ml", 100000, 20000);
            transferInitial(stockService, "Nakuru", "Pepsi500ml",  80000, 15000);
            transferInitial(stockService, "Mombasa", "Coca-Cola500ml", 150000, 25000);

            // Add users with branch assignments
            if (!userDAO.findByUsername("Admin").isPresent()) {
                authService.addUser("Admin", "password", User.UserRole.ADMIN, null); // Admin has no branch
            }
            if (!userDAO.findByUsername("NakuruManager").isPresent()) {
                authService.addUser("NakuruManager", "password", User.UserRole.BRANCH_MANAGER, "Nakuru"); // Assigned to Nakuru
            }

            System.out.println("Initial data setup complete.");
        } catch (Exception e) {
            System.err.println("Error during initial data setup: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private static void transferInitial(IStockService stockService, String branchId, String drinkId, int quantity, int threshold) {
        try {
            if (stockService.getStockLevel(branchId, drinkId) < quantity) {
                stockService.transferStock(HQ_BRANCH_ID_SVR, branchId, drinkId, quantity);
            }
            stockService.setStockThreshold(branchId, drinkId, threshold);
        } catch (Exception e) {
            System.err.println("Warn: Initial stock transfer/threshold for " + drinkId + " to " + branchId + " failed: " + e.getMessage());
        }
    }
}


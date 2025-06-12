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

    private static void setupInitialData(
            IDrinkService drinkService,
            IBranchService branchService,
            IStockService stockService,
            IAuthService authService,
            IUserDAO userDAO,
            IBranchDAO branchDAO,
            IDrinkDAO drinkDAO
    ) {
        System.out.println("Setting up initial data (if necessary)...");

        try {
            // Add HQ branch
            if (!branchDAO.findById(HQ_BRANCH_ID_SVR).isPresent()) {
                branchService.addBranch(new Branch(HQ_BRANCH_ID_SVR, "Nairobi HQ", "Nairobi"));
            }

            // Add initial drinks
            if (!drinkDAO.findById("DK001").isPresent()) {
                drinkService.addDrink(new Drink("DK001", "Coca-Cola 500ml", "Coca-Cola", 60.00, 1000));
            }
            if (!drinkDAO.findById("DK002").isPresent()) {
                drinkService.addDrink(new Drink("DK002", "Pepsi 500ml", "PepsiCo", 55.00, 800));
            }
            if (!drinkDAO.findById("DK003").isPresent()) {
                drinkService.addDrink(new Drink("DK003", "Fanta 300ml", "Coca-Cola", 45.00, 700));
            }

            // Add other branches
            if (!branchDAO.findById("NKR01").isPresent()) {
                branchService.addBranch(new Branch("NKR01", "Nakuru Central", "Nakuru"));
            }
            if (!branchDAO.findById("MSA01").isPresent()) {
                branchService.addBranch(new Branch("MSA01", "Mombasa Island", "Mombasa"));
            }

            // Perform initial stock transfers
            transferInitial(stockService, "NKR01", "DK001", 100, 20);
            transferInitial(stockService, "NKR01", "DK002", 80, 15);
            transferInitial(stockService, "MSA01", "DK001", 150, 25);

            // Add users
            if (!userDAO.findByUserName("Admin").isPresent()) {
                authService.addUser("Admin", "password", User.UserRole.ADMIN);
            }
            if (!userDAO.findByUserName("NakuruManager").isPresent()) {
                authService.addUser("NakuruManager", "password", User.UserRole.BRANCH_MANAGER);
            }

            System.out.println("Initial data setup complete.");

        } catch (Exception e) {
            System.err.println("Error during initial data setup: " + e.getMessage());
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


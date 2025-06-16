package View.UIRunners;

import Model.DataEntities.Branch;
import Model.DataEntities.Drink;
import Model.DataEntities.Order;
import Model.DataEntities.User;
import Model.ServiceInterfaces.*;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CLIRunner implements Runnable{
    private IAuthService authService;
    private IDrinkService drinkService;
    private IBranchService branchService;
    private IStockService stockService;
    private IOrderService orderService;
    private IReportingService reportingService;
    private User loggedInUser=null;
    private volatile boolean cliRunning=true;

    public CLIRunner(IAuthService authService,IDrinkService drinkService,IBranchService branchService, IStockService stockService,IOrderService orderService,IReportingService reportingService){
        this.authService=authService;
        this.drinkService=drinkService;
        this.branchService=branchService;
        this.stockService=stockService;
        this.orderService=orderService;
        this.reportingService=reportingService;
    }


    @Override
    public void run() {
        System.out.println("CLI Initialized on thread: "+ Thread.currentThread().getName());
        Scanner scanner=new Scanner(System.in);
        while(cliRunning){
            try{
                if(loggedInUser==null){
                    showLoginMenu(scanner);
                }else showMainMenu(scanner);
                Thread.sleep(100);
            }catch (InterruptedException e){
                cliRunning=false;
                Thread.currentThread().interrupt();
            }catch (Exception e){
                System.err.println("CLI Error: "+ e.getMessage());
                if(!(e instanceof RemoteException)){
                    e.printStackTrace(); //Avoid stack trace for common RMI issues already handled
                }
            }
        }
        scanner.close();
        System.out.println("CLI Runner finished");

    }

    private void showLoginMenu(Scanner scanner) {
        System.out.println("\n--- CLI Login ---\n1. Login\n0. Exit CLI");
        System.out.print("Opt: ");
        String option = scanner.nextLine().strip();
        if (option.equals("1")) {
            handleLogin(scanner);
        } else if (option.equals("0")) {
            cliRunning = false;
        } else {
            System.out.println("Invalid.");
        }
    }

    private void handleLogin(Scanner scanner) {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().strip();
            System.out.print("Password: ");
            String password = scanner.nextLine().strip();
            loggedInUser = authService.login(username, password);
            System.out.println("CLI Login OK: " + loggedInUser.getUsername());
        } catch (Exception exception) {
            System.err.println("CLI Login Fail: " + exception.getMessage());
            loggedInUser = null;
        }
    }

    private void showMainMenu(Scanner scanner) {
        System.out.println("\n--- CLI Menu (" + loggedInUser.getUsername() + ") ---\n1. Drinks\n2. Branches\n3. Stock Lvl\n4. Order\n5. Low Stock\n6. Branch Report\n7. Overall Report");
        if (loggedInUser.getRole() == User.UserRole.ADMIN) {
            System.out.println("A1. Add Drink\nA2. Add Branch\nA3. Set Stock\nA4. Set Threshold\nA5. Transfer\nA6. Add User");
        }
        System.out.println("9. Logout\n0. Exit CLI");
        System.out.print("Opt: ");
        String option = scanner.nextLine().strip().toUpperCase();
        try {
            switch (option) {
                case "1":
                    drinkService.getAllDrinks().forEach(System.out::println);
                    break;
                case "2":
                    branchService.getAllBranches().forEach(System.out::println);
                    break;
                case "3":
                    System.out.print("BranchID: ");
                    String branchId = scanner.nextLine().strip();
                    System.out.print("DrinkID: ");
                    String drinkId = scanner.nextLine().strip();
                    System.out.println("Stock: " + stockService.getStockLevel(branchId, drinkId));
                    break;
                case "4":
                    placeOrderCLI(scanner);
                    break;
                case "5":
                    stockService.checkLowStockLevelsGlobally().forEach(System.out::println);
                    break;
                case "6":
                    System.out.print("BranchID for report: ");
                    String reportBranchId = scanner.nextLine().strip();
                    reportingService.generateBranchSalesReport(reportBranchId).forEach((drink, sales) -> System.out.println(drink + ": " + sales));
                    break;
                case "7":
                    reportingService.generateOverallBusinessReport().forEach((metric, value) -> System.out.println(metric + ": " + value));
                    break;
                case "A1":
                    if (isAdmin()) {
                        System.out.print("DrinkID,DrinkName,Brand,Price,HQStock (commas): ");
                        String[] drinkParts = scanner.nextLine().strip().split(",");
                        drinkService.addDrink(new Drink(
                                drinkParts[0].strip(),
                                drinkParts[1].strip(),
                                drinkParts[2].strip(),
                                Double.parseDouble(drinkParts[3].strip()),
                                Integer.parseInt(drinkParts[4].strip())
                        ));
                        System.out.println("Drink added.");
                    }
                    break;
                case "A2":
                    if (isAdmin()) {
                        System.out.print("BranchID,Name,Location (commas): ");
                        String[] branchParts = scanner.nextLine().strip().split(",");
                        branchService.addBranch(new Branch(
                                branchParts[0].strip(),
                                branchParts[1].strip(),
                                branchParts[2].strip()
                        ));
                        System.out.println("Branch added.");
                    }
                    break;
                case "A3":
                    if (isAdmin()) {
                        System.out.print("BranchID,DrinkID,Quantity (commas): ");
                        String[] stockLevelParts = scanner.nextLine().strip().split(",");
                        stockService.setStockLevel(
                                stockLevelParts[0].strip(),
                                stockLevelParts[1].strip(),
                                Integer.parseInt(stockLevelParts[2].strip())
                        );
                        System.out.println("Stock set.");
                    }
                    break;
                case "A4":
                    if (isAdmin()) {
                        System.out.print("BranchID,DrinkID,Threshold (commas): ");
                        String[] thresholdParts = scanner.nextLine().strip().split(",");
                        stockService.setStockThreshold(
                                thresholdParts[0].strip(),
                                thresholdParts[1].strip(),
                                Integer.parseInt(thresholdParts[2].strip())
                        );
                        System.out.println("Threshold set.");
                    }
                    break;
                case "A5":
                    if (isAdmin()) {
                        System.out.print("FromBranch,ToBranch,DrinkID,Quantity (commas): ");
                        String[] transferParts = scanner.nextLine().strip().split(",");
                        stockService.transferStock(
                                transferParts[0].strip(),
                                transferParts[1].strip(),
                                transferParts[2].strip(),
                                Integer.parseInt(transferParts[3].strip())
                        );
                        System.out.println("Stock transferred.");
                    }
                    break;
                case "A6":
                    if (isAdmin()) {
                        System.out.print("Username,Password,Role(ADMIN/BRANCH_MANAGER/STAFF) (commas): ");
                        String[] userParts = scanner.nextLine().strip().split(",");
                        authService.addUser(
                                userParts[0].strip(),
                                userParts[1].strip(),
                                User.UserRole.valueOf(userParts[2].strip().toUpperCase()),
                                userParts[3].strip()
                        );
                        System.out.println("User added.");
                    }
                    break;
                case "9":
                    loggedInUser = null;
                    System.out.println("CLI Logged out.");
                    break;
                case "0":
                    cliRunning = false;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        } catch (Exception exception) {
            System.err.println("CLI Action Error: " + exception.getMessage());
        }
    }

    private boolean isAdmin() {
        return loggedInUser != null && loggedInUser.getRole() == User.UserRole.ADMIN;
    }

    private void placeOrderCLI(Scanner scanner) {
        try {
            System.out.print("CustomerID: ");
            String customerId = scanner.nextLine().strip();
            System.out.print("BranchID: ");
            String branchId = scanner.nextLine().strip();
            Map<String, Integer> orderedItems = new HashMap<>();
            while (true) {
                System.out.print("DrinkID (or done): ");
                String drinkId = scanner.nextLine().strip();
                if (drinkId.equalsIgnoreCase("done")) {
                    break;
                }
                System.out.print("Qty: ");
                int quantity = Integer.parseInt(scanner.nextLine().strip());
                orderedItems.put(drinkId, quantity);
            }
            if (orderedItems.isEmpty()) {
                System.out.println("No items.");
                return;
            }
            Order order = orderService.placeOrder(customerId, branchId, orderedItems);
            System.out.println("Order Placed: " + order.getOrderId());
        } catch (Exception exception) {
            System.err.println("Order Error: " + exception.getMessage());
        }
    }


}

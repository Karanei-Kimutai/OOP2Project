package Controller.ClientEntryPoint;

import Model.ServiceInterfaces.*;
import Model.UtilitiesandServerEntryPoint.DrinkEnterpriseServer;
import View.UIRunners.CLIRunner;

import java.rmi.Naming;
import java.util.Scanner;

public class DrinkEnterpriseClient {
    private static final String RMI_HOST="localhost";//Change this to the IP address of the server when compiling the jar file
    private static final int RMI_PORT= DrinkEnterpriseServer.RMI_PORT;
    private static IAuthService authService;
    private static IDrinkService drinkService;
    private static IBranchService branchService;
    private static IStockService stockService;
    private static IOrderService orderService;
    private static IReportingService reportingService;

    public static void main(String[] args){
        try{
            System.out.println("Client: Connecting to RMI Server...");
            authService=(IAuthService) Naming.lookup("rmi://" + RMI_HOST + ":" + RMI_PORT + "/AuthService");
            drinkService=(IDrinkService) Naming.lookup("rmi://" + RMI_HOST + ":" + RMI_PORT + "/DrinkService");
            branchService=(IBranchService) Naming.lookup("rmi://" + RMI_HOST + ":" + RMI_PORT + "/BranchService");
            stockService=(IStockService) Naming.lookup("rmi://" + RMI_HOST + ":" + RMI_PORT + "/StockService");
            orderService=(IOrderService) Naming.lookup("rmi://" + RMI_HOST + ":" + RMI_PORT + "/OrderService");
            reportingService=(IReportingService) Naming.lookup("rmi://" + RMI_HOST + ":" + RMI_PORT + "/ReportingService");
            System.out.println("Client:Connected to all services.");

            Scanner mainScanner=new Scanner(System.in);
            System.out.println("Drink Enterprise Client");
            System.out.println("1.Command Line Interface(CLI)");
            System.out.println("2. Graphical User Interface(GUI)");
            System.out.println("Choose an option: ");

            String choice=mainScanner.nextLine().strip();
            while(!choice.equals("0") && !choice.equals("1") && !choice.equals("2")){
                System.out.println("Invalid option.");
                System.out.println("Choose an option: ");
                choice=mainScanner.nextLine();
            }
            boolean startCLI=choice.equals("1");
            boolean startGUI=choice.equals("2");
            if(choice.equals("0")){
                System.out.println("Exiting application.");
                System.exit(0);
            }

            if(startCLI){
                new Thread(new CLIRunner(authService,drinkService,branchService,stockService,orderService,reportingService),"CLI-Thread").start();
                System.out.println("CLI thread started.");

            }
            if(startGUI){

            }
        } catch (Exception e) {
            System.err.println("Client main exception: "+e);
            e.printStackTrace();
        }
    }
}

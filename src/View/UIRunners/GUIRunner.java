package View.UIRunners;

import Model.ServiceInterfaces.*;
import View.GUIClasses.MainFrame;

import javax.swing.*;

public class GUIRunner implements Runnable{
    // Service stubs for interacting with the server
    private final IAuthService authService;
    private final IDrinkService drinkService;
    private final IBranchService branchService;
    private final IStockService stockService;
    private final IOrderService orderService;
    private final IReportingService reportingService;

    public GUIRunner(IAuthService authService, IDrinkService drinkService, IBranchService branchService,
                     IStockService stockService, IOrderService orderService, IReportingService reportingService) {
        this.authService = authService;
        this.drinkService = drinkService;
        this.branchService = branchService;
        this.stockService = stockService;
        this.orderService = orderService;
        this.reportingService = reportingService;
    }

    @Override
    public void run() {
        System.out.println("GUI Initializing on thread: " + Thread.currentThread().getName());
        // Use SwingUtilities.invokeLater to ensure all GUI code runs on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply a modern Look and Feel (Nimbus) for better aesthetics
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // If Nimbus is not available, the default L&F will be used.
                System.err.println("Nimbus Look and Feel not found, using default. Error: " + e.getMessage());
            }

            // Create and display the main application window
            MainFrame mainFrame = new MainFrame(authService, drinkService, branchService,
                    stockService, orderService, reportingService);
            mainFrame.setVisible(true);
        });
        System.out.println("GUIRunner.run() finished scheduling GUI on EDT.");
    }
}

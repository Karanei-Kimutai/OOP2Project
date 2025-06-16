package View.GUIClasses;


import Model.DataEntities.User;
import Model.ServiceInterfaces.*;

import javax.swing.*;
import java.awt.*;

//SWING CLASS MAINFRAME
public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private LoginPanel loginPanel;
    private AppPanel appPanel;

    // References to all the RMI service stubs
    private final IAuthService authService;
    private final IDrinkService drinkService;
    private final IBranchService branchService;
    private final IStockService stockService;
    private final IOrderService orderService;
    private final IReportingService reportingService;

    public MainFrame(IAuthService authService, IDrinkService drinkService, IBranchService branchService,
                     IStockService stockService, IOrderService orderService, IReportingService reportingService) {
        this.authService = authService;
        this.drinkService = drinkService;
        this.branchService = branchService;
        this.stockService = stockService;
        this.orderService = orderService;
        this.reportingService = reportingService;

        setTitle("Drink Enterprise Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 800);
        setLocationRelativeTo(null); // Center the window

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this, this.authService);
        mainPanel.add(loginPanel, "LOGIN_PANEL");

        add(mainPanel);
    }

    /**
     * Switches the view to the login panel and removes the old app panel.
     */
    public void showLoginPanel() {
        cardLayout.show(mainPanel, "LOGIN_PANEL");
        if (appPanel != null) {
            mainPanel.remove(appPanel);
            appPanel = null;
        }
    }

    /**
     * Creates and switches the view to the main application panel after a successful login.
     * @param loggedInUser The user who has successfully logged in.
     */
    public void showAppPanel(User loggedInUser) {
        appPanel = new AppPanel(this, loggedInUser, authService, drinkService, branchService,
                stockService, orderService, reportingService);
        mainPanel.add(appPanel, "APP_PANEL");
        cardLayout.show(mainPanel, "APP_PANEL");
    }

    /**
     * A public method to trigger a data refresh across relevant tabs in the AppPanel.
     * For instance, after an admin adds a new drink or branch.
     */
    public void refreshDataViews() {
        if (appPanel != null) {
            appPanel.refreshAllDataPanels();
        }
    }
}

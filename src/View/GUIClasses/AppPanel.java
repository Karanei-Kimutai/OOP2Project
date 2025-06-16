package View.GUIClasses;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import Model.DataEntities.Branch;
import Model.DataEntities.Drink;
import Model.DataEntities.Order;
import Model.DataEntities.User;
import Model.ServiceInterfaces.*;


public class AppPanel extends JPanel {
    private final MainFrame mainFrame;
    private final User loggedInUser;

    // Panels that need to be refreshed
    private final JPanel viewDrinksPanel;
    private final JPanel viewBranchesPanel;
    private final JPanel placeOrderPanel;
    private final JPanel reportsPanel;

    // Services
    private final IDrinkService drinkService;
    private final IBranchService branchService;
    private final IStockService stockService;
    private final IOrderService orderService;
    private final IReportingService reportingService;
    private final IAuthService authService;

    public AppPanel(MainFrame mainFrame, User user, IAuthService authService, IDrinkService drinkService, IBranchService branchService,
                    IStockService stockService, IOrderService orderService, IReportingService reportingService) {
        this.mainFrame = mainFrame;
        this.loggedInUser = user;
        this.authService = authService;
        this.drinkService = drinkService;
        this.branchService = branchService;
        this.stockService = stockService;
        this.orderService = orderService;
        this.reportingService = reportingService;

        setLayout(new BorderLayout(10, 10));

        // Create and add top panel with welcome message and logout button
        add(createTopPanel(), BorderLayout.NORTH);

        // Create and add the main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        viewDrinksPanel = createViewDrinksPanel();
        tabbedPane.addTab("Drinks", null, viewDrinksPanel, "View all available drinks");

        viewBranchesPanel = createViewBranchesPanel();
        tabbedPane.addTab("Branches", null, viewBranchesPanel, "View all business branches");

        placeOrderPanel = createPlaceOrderPanel();
        tabbedPane.addTab("Place Order", null, placeOrderPanel, "Create a new customer order");

        reportsPanel = createReportsPanel();
        tabbedPane.addTab("Reports", null, reportsPanel, "View sales and stock reports");

        if (loggedInUser.getRole() == User.UserRole.ADMIN) {
            JPanel adminConsolePanel = createAdminConsolePanel();
            tabbedPane.addTab("Admin Console", null, adminConsolePanel, "Administrative functions");
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshAllDataPanels() {
        if(viewDrinksPanel != null && viewDrinksPanel.getClientProperty("refreshButton") instanceof JButton) {
            SwingUtilities.invokeLater(((JButton)viewDrinksPanel.getClientProperty("refreshButton"))::doClick);
        }
        if(viewBranchesPanel != null && viewBranchesPanel.getClientProperty("refreshButton") instanceof JButton) {
            SwingUtilities.invokeLater(((JButton)viewBranchesPanel.getClientProperty("refreshButton"))::doClick);
        }
        if(placeOrderPanel != null && placeOrderPanel.getClientProperty("refreshAction") instanceof Runnable) {
            SwingUtilities.invokeLater((Runnable)placeOrderPanel.getClientProperty("refreshAction"));
        }
        if(reportsPanel != null && reportsPanel.getClientProperty("refreshAction") instanceof Runnable) {
            SwingUtilities.invokeLater((Runnable)reportsPanel.getClientProperty("refreshAction"));
        }
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + loggedInUser.getUsername() + " (" + loggedInUser.getRole() + ")", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.addActionListener(e -> mainFrame.showLoginPanel());
        topPanel.add(logoutButton, BorderLayout.EAST);

        return topPanel;
    }

    // --- Panel Creation Methods ---

    private JPanel createViewDrinksPanel() { /* Unchanged from previous version */
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        String[] columnNames = {"ID", "Name", "Brand", "Price"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable drinksTable = new JTable(tableModel);
        drinksTable.setFillsViewportHeight(true);
        drinksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        drinksTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        drinksTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        drinksTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        drinksTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        panel.add(new JScrollPane(drinksTable), BorderLayout.CENTER);
        JButton refreshButton = new JButton("Refresh");
        JLabel statusLabel = new JLabel(" ", SwingConstants.LEFT);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(refreshButton, BorderLayout.EAST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.putClientProperty("refreshButton", refreshButton);

        refreshButton.addActionListener(e -> {
            statusLabel.setText("Loading drinks...");
            tableModel.setRowCount(0);
            new SwingWorker<List<Drink>, Void>() {
                @Override protected List<Drink> doInBackground() throws Exception { return drinkService.getAllDrinks(); }
                @Override protected void done() {
                    try {
                        List<Drink> drinksList = get();
                        if (drinksList.isEmpty()) {
                            statusLabel.setText("No drinks found in the catalog.");
                        } else {
                            for(Drink drink : drinksList) {
                                tableModel.addRow(new Object[]{drink.getId(), drink.getName(), drink.getBrand(), String.format("%.2f", drink.getPrice())});
                            }
                            statusLabel.setText(drinksList.size() + " drinks loaded.");
                        }
                    } catch (Exception ex) {
                        String err = "Error loading drinks: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                        statusLabel.setText(err);
                        JOptionPane.showMessageDialog(panel, err, "Load Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });
        SwingUtilities.invokeLater(refreshButton::doClick);
        return panel;
    }

    private JPanel createViewBranchesPanel() { /* Unchanged from previous version */
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        String[] columnNames = {"ID", "Name", "Location"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable branchesTable = new JTable(tableModel);
        branchesTable.setFillsViewportHeight(true);
        branchesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        branchesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        branchesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        branchesTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        panel.add(new JScrollPane(branchesTable), BorderLayout.CENTER);
        JButton refreshButton = new JButton("Refresh");
        JLabel statusLabel = new JLabel(" ", SwingConstants.LEFT);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(refreshButton, BorderLayout.EAST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.putClientProperty("refreshButton", refreshButton);

        refreshButton.addActionListener(e -> {
            statusLabel.setText("Loading branches...");
            tableModel.setRowCount(0);
            new SwingWorker<List<Branch>, Void>() {
                @Override protected List<Branch> doInBackground() throws Exception { return branchService.getAllBranches(); }
                @Override protected void done() {
                    try {
                        List<Branch> branches = get();
                        if (branches.isEmpty()) {
                            statusLabel.setText("No branches found.");
                        } else {
                            branches.forEach(b -> tableModel.addRow(new Object[]{b.getId(), b.getName(), b.getLocation()}));
                            statusLabel.setText(branches.size() + " branches loaded.");
                        }
                    } catch (Exception ex) {
                        String err = "Error loading branches: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                        statusLabel.setText(err);
                        JOptionPane.showMessageDialog(panel, err, "Load Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });
        SwingUtilities.invokeLater(refreshButton::doClick);
        return panel;
    }

    private JPanel createPlaceOrderPanel() { /* Unchanged from previous version */
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel topSelectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.insets = new Insets(5,5,5,5); gbcTop.anchor = GridBagConstraints.WEST;
        JTextField customerIdField = new JTextField("CUST-" + System.currentTimeMillis()%10000, 15);
        JComboBox<Branch> branchComboBox = new JComboBox<>();
        gbcTop.gridx=0; gbcTop.gridy=0; topSelectionPanel.add(new JLabel("Customer ID:"), gbcTop);
        gbcTop.gridx=1; topSelectionPanel.add(customerIdField, gbcTop);
        gbcTop.gridx=0; gbcTop.gridy=1; topSelectionPanel.add(new JLabel("Branch:"), gbcTop);
        gbcTop.gridx=1; gbcTop.fill = GridBagConstraints.HORIZONTAL; topSelectionPanel.add(branchComboBox, gbcTop);
        JPanel middlePanel = new JPanel(new GridLayout(1,2,10,0));
        JPanel availableDrinksOuterPanel = new JPanel(new BorderLayout());
        availableDrinksOuterPanel.setBorder(BorderFactory.createTitledBorder("Available Drinks"));
        DefaultListModel<Drink> drinksListModel = new DefaultListModel<>();
        JList<Drink> availableDrinksList = new JList<>(drinksListModel);
        availableDrinksOuterPanel.add(new JScrollPane(availableDrinksList), BorderLayout.CENTER);
        JPanel drinkControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1,1,100,1));
        JButton addToOrderButton = new JButton("Add to Order");
        drinkControlPanel.add(new JLabel("Qty:")); drinkControlPanel.add(quantitySpinner); drinkControlPanel.add(addToOrderButton);
        availableDrinksOuterPanel.add(drinkControlPanel, BorderLayout.SOUTH);
        middlePanel.add(availableDrinksOuterPanel);
        JPanel currentOrderPanel = new JPanel(new BorderLayout());
        currentOrderPanel.setBorder(BorderFactory.createTitledBorder("Current Order Items"));
        String[] orderCartCols = {"Drink Name", "Qty", "Price", "Total"};
        DefaultTableModel orderCartModel = new DefaultTableModel(orderCartCols, 0){public boolean isCellEditable(int r,int c){return false;}};
        JTable orderCartTable = new JTable(orderCartModel);
        currentOrderPanel.add(new JScrollPane(orderCartTable), BorderLayout.CENTER);
        JButton removeFromOrderButton = new JButton("Remove Selected");
        currentOrderPanel.add(removeFromOrderButton, BorderLayout.SOUTH);
        middlePanel.add(currentOrderPanel);
        JPanel bottomActionPanel = new JPanel(new BorderLayout(10,5));
        JLabel orderTotalLabel = new JLabel("Order Total: 0.00", SwingConstants.RIGHT);
        orderTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton submitOrderButton = new JButton("Submit Order");
        submitOrderButton.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel placeOrderStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitPanel.add(submitOrderButton);
        bottomActionPanel.add(orderTotalLabel, BorderLayout.CENTER);
        bottomActionPanel.add(submitPanel, BorderLayout.EAST);
        bottomActionPanel.add(placeOrderStatusLabel, BorderLayout.SOUTH);
        panel.add(topSelectionPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);
        panel.add(bottomActionPanel, BorderLayout.SOUTH);
        Map<String,Integer> currentOrderItemsMap = new HashMap<>();
        Map<String,Drink> currentOrderDrinkObjects = new HashMap<>();
        Runnable refreshAction = () -> {
            branchComboBox.removeAllItems();
            drinksListModel.clear();
            new SwingWorker<Void,Void>(){
                List<Branch> branches; List<Drink> drinks;
                protected Void doInBackground() throws Exception { branches = branchService.getAllBranches(); drinks = drinkService.getAllDrinks(); return null; }
                protected void done() { try{ get(); branches.forEach(branchComboBox::addItem); drinks.forEach(drinksListModel::addElement); } catch(Exception e){JOptionPane.showMessageDialog(panel,"Error loading initial data: "+e.getMessage(),"Error",0);}}
            }.execute();
        };
        panel.putClientProperty("refreshAction", refreshAction);
        refreshAction.run();
        addToOrderButton.addActionListener(e -> {
            Drink selectedDrink = availableDrinksList.getSelectedValue();
            int quantity = (Integer) quantitySpinner.getValue();
            if (selectedDrink != null && quantity > 0) {
                currentOrderItemsMap.put(selectedDrink.getId(), currentOrderItemsMap.getOrDefault(selectedDrink.getId(), 0) + quantity);
                currentOrderDrinkObjects.putIfAbsent(selectedDrink.getId(), selectedDrink);
                updateOrderCartTable(orderCartModel, currentOrderItemsMap, currentOrderDrinkObjects, orderTotalLabel);
                quantitySpinner.setValue(1);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a drink and specify a valid quantity.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        removeFromOrderButton.addActionListener(e -> {
            int selectedRow = orderCartTable.getSelectedRow();
            if (selectedRow >= 0) {
                String drinkName = (String) orderCartModel.getValueAt(selectedRow, 0);
                String drinkIdToRemove = null;
                for(Map.Entry<String, Drink> entry : currentOrderDrinkObjects.entrySet()){
                    if(entry.getValue().getName().equals(drinkName)){
                        drinkIdToRemove = entry.getKey(); break;
                    }
                }
                if(drinkIdToRemove != null){
                    currentOrderItemsMap.remove(drinkIdToRemove);
                    currentOrderDrinkObjects.remove(drinkIdToRemove);
                    updateOrderCartTable(orderCartModel, currentOrderItemsMap, currentOrderDrinkObjects, orderTotalLabel);
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an item from the order to remove.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        submitOrderButton.addActionListener(e -> {
            String custId = customerIdField.getText();
            Branch selectedBranch = (Branch) branchComboBox.getSelectedItem();
            if (custId.isEmpty() || selectedBranch == null || currentOrderItemsMap.isEmpty()) {
                placeOrderStatusLabel.setText("Customer ID, Branch, and Items are required.");
                placeOrderStatusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(panel, "Customer ID, Branch selection, and at least one item are required to submit an order.", "Order Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            placeOrderStatusLabel.setText("Submitting order...");
            placeOrderStatusLabel.setForeground(Color.BLUE);
            new SwingWorker<Order, Void>() {
                @Override protected Order doInBackground() throws Exception {
                    return orderService.placeOrder(custId, selectedBranch.getId(), currentOrderItemsMap);
                }
                @Override protected void done() {
                    try {
                        Order placedOrder = get();
                        placeOrderStatusLabel.setText("Order placed successfully! ID: " + placedOrder.getOrderId());
                        placeOrderStatusLabel.setForeground(new Color(0,128,0));
                        JOptionPane.showMessageDialog(panel, "Order Submitted!\nID: " + placedOrder.getOrderId() + "\nTotal: " + String.format("%.2f", placedOrder.getTotalAmount()), "Order Confirmation", JOptionPane.INFORMATION_MESSAGE);
                        currentOrderItemsMap.clear();
                        currentOrderDrinkObjects.clear();
                        updateOrderCartTable(orderCartModel, currentOrderItemsMap, currentOrderDrinkObjects, orderTotalLabel);
                        customerIdField.setText("CUST-" + System.currentTimeMillis()%10000);
                    } catch (Exception ex) {
                        String err = "Order submission failed: " + (ex.getCause()!=null ? ex.getCause().getMessage() : ex.getMessage());
                        placeOrderStatusLabel.setText(err);
                        placeOrderStatusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(panel, err, "Order Submission Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });
        return panel;
    }

    private void updateOrderCartTable(DefaultTableModel cartModel, Map<String, Integer> itemsMap, Map<String, Drink> drinkObjects, JLabel totalLabel) {
        cartModel.setRowCount(0);
        double currentTotal = 0;
        for (Map.Entry<String, Integer> entry : itemsMap.entrySet()) {
            Drink drink = drinkObjects.get(entry.getKey());
            if (drink != null) {
                double itemTotal = drink.getPrice() * entry.getValue();
                cartModel.addRow(new Object[]{
                        drink.getName(),
                        entry.getValue(),
                        String.format("%.2f", drink.getPrice()),
                        String.format("%.2f", itemTotal)
                });
                currentTotal += itemTotal;
            }
        }
        totalLabel.setText("Order Total: " + String.format("%.2f", currentTotal));
    }

    private JPanel createReportsPanel() { /* Unchanged from previous version */
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST;
        JComboBox<Object> branchReportComboBox = new JComboBox<>();
        JButton getBranchSalesButton = new JButton("Get Branch Sales Report");
        JButton getOverallSalesButton = new JButton("Get Overall Business Report");
        JTextArea reportResultsArea = new JTextArea(15, 60);
        reportResultsArea.setEditable(false);
        reportResultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportResultsArea);

        Runnable refreshAction = () -> {
            branchReportComboBox.removeAllItems();
            branchReportComboBox.addItem("--- Select Branch ---");
            new SwingWorker<List<Branch>, Void>() {
                protected List<Branch> doInBackground() throws Exception { return branchService.getAllBranches(); }
                protected void done() { try { List<Branch> branches = get(); branches.forEach(branchReportComboBox::addItem); } catch (Exception e) { reportResultsArea.setText("Error loading branches for report selection.");}}
            }.execute();
        };
        panel.putClientProperty("refreshAction", refreshAction);
        refreshAction.run();

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Select Branch:"), gbc);
        gbc.gridx=1; gbc.gridy=0; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(branchReportComboBox, gbc);
        gbc.gridx=2; gbc.gridy=0; gbc.fill = GridBagConstraints.NONE; panel.add(getBranchSalesButton, gbc);

        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=3; gbc.anchor=GridBagConstraints.CENTER; panel.add(getOverallSalesButton, gbc);

        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=3; gbc.fill=GridBagConstraints.BOTH; gbc.weightx=1; gbc.weighty=1;
        panel.add(scrollPane, gbc);

        getBranchSalesButton.addActionListener(e -> {
            Object selectedItem = branchReportComboBox.getSelectedItem();
            if (selectedItem instanceof Branch) {
                Branch selectedBranch = (Branch) selectedItem;
                reportResultsArea.setText("Fetching sales for " + selectedBranch.getName() + "...");
                new SwingWorker<Map<String, String>, Void>() {
                    protected Map<String, String> doInBackground() throws Exception { return reportingService.generateBranchSalesReport(selectedBranch.getId()); }
                    protected void done() {
                        try {
                            Map<String,String> report = get();
                            StringBuilder sb = new StringBuilder("Branch Sales Report for: "+selectedBranch.getName()+"\n=====================================\n");
                            report.forEach((key,v) -> sb.append(String.format("%-25s: %s\n", key, v)));
                            reportResultsArea.setText(sb.toString());
                        } catch(Exception ex) {
                            reportResultsArea.setText("Error fetching branch report: " + (ex.getCause()!=null ? ex.getCause().getMessage() : ex.getMessage()));
                        }
                    }
                }.execute();
            } else {
                reportResultsArea.setText("Please select a valid branch to generate a report.");
            }
        });

        getOverallSalesButton.addActionListener(e -> {
            reportResultsArea.setText("Fetching overall business sales report...");
            new SwingWorker<Map<String, String>, Void>() {
                protected Map<String, String> doInBackground() throws Exception { return reportingService.generateOverallBusinessReport(); }
                protected void done() {
                    try {
                        Map<String,String> report = get();
                        StringBuilder sb = new StringBuilder("Overall Business Sales Report\n=====================================\n");
                        report.forEach((key,v) -> sb.append(String.format("%-25s: %s\n", key, v)));
                        reportResultsArea.setText(sb.toString());
                    } catch(Exception ex) {
                        reportResultsArea.setText("Error fetching overall report: " + (ex.getCause()!=null ? ex.getCause().getMessage() : ex.getMessage()));
                    }
                }
            }.execute();
        });
        return panel;
    }

    private JPanel createAdminConsolePanel() { /* Unchanged from previous version */
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel adminPanel = new JPanel();
        adminPanel.setLayout(new BoxLayout(adminPanel, BoxLayout.Y_AXIS));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add Drink Panel
        JPanel addDrinkPanel = new JPanel(new GridBagLayout());
        addDrinkPanel.setBorder(BorderFactory.createTitledBorder("Add New Drink"));
        // ... (GBC and components for adding drink)
        adminPanel.add(addDrinkPanel);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Add Branch Panel
        JPanel addBranchPanel = new JPanel(new GridBagLayout());
        addBranchPanel.setBorder(BorderFactory.createTitledBorder("Add New Branch"));
        // ... (GBC and components for adding branch)
        adminPanel.add(addBranchPanel);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Manage Stock Panel
        JPanel manageStockPanel = new JPanel(new GridBagLayout());
        manageStockPanel.setBorder(BorderFactory.createTitledBorder("Manage Stock"));
        // ... (GBC and components for managing stock)
        adminPanel.add(manageStockPanel);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Add User Panel
        JPanel addUserPanel = new JPanel(new GridBagLayout());
        addUserPanel.setBorder(BorderFactory.createTitledBorder("Add System User"));
        // ... (GBC and components for adding user)
        adminPanel.add(addUserPanel);

        outerPanel.add(new JScrollPane(adminPanel), BorderLayout.CENTER);
        return outerPanel;
    }
}






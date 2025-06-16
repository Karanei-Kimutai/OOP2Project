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

    private final JPanel viewDrinksPanel;
    private final JPanel viewBranchesPanel;
    private final JPanel placeOrderPanel;
    private final JPanel reportsPanel;
    private final JPanel adminConsolePanel;

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
        add(createTopPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        viewDrinksPanel = createViewDrinksPanel();
        tabbedPane.addTab("Drinks", null, viewDrinksPanel, "View all available drinks");

        viewBranchesPanel = createViewBranchesPanel();
        tabbedPane.addTab("Branches", null, viewBranchesPanel, "View all business branches");

        placeOrderPanel = createPlaceOrderPanel();
        tabbedPane.addTab("Place Order", null, placeOrderPanel, "Create a new customer order");

        reportsPanel = createReportsPanel();
        tabbedPane.addTab("Reports", null, reportsPanel, "View sales and stock reports");

        if (loggedInUser.getRole() == User.UserRole.ADMIN) {
            adminConsolePanel = createAdminConsolePanel();
            tabbedPane.addTab("Admin Console", null, adminConsolePanel, "Administrative functions");
        } else {
            adminConsolePanel = null;
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
        if (adminConsolePanel != null && adminConsolePanel.getClientProperty("refreshAction") instanceof Runnable) {
            SwingUtilities.invokeLater((Runnable) adminConsolePanel.getClientProperty("refreshAction"));
        }
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + loggedInUser.getUsername() + " (" + loggedInUser.getRole() + ")", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.addActionListener(e -> mainFrame.showLoginPanel());
        topPanel.add(logoutButton, BorderLayout.EAST);

        return topPanel;
    }

    // --- Panel Creation Methods ---

    private JPanel createViewDrinksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"ID", "Name", "Brand", "Price"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable drinksTable = new JTable(tableModel);
        drinksTable.setFillsViewportHeight(true);
        drinksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        drinksTable.setRowHeight(25);
        drinksTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        drinksTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

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
                @Override
                protected List<Drink> doInBackground() throws Exception {
                    return drinkService.getAllDrinks();
                }

                @Override
                protected void done() {
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

    private JPanel createViewBranchesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"ID", "Name", "Location"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable branchesTable = new JTable(tableModel);
        branchesTable.setFillsViewportHeight(true);
        branchesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        branchesTable.setRowHeight(25);
        branchesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        branchesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

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
                @Override
                protected List<Branch> doInBackground() throws Exception {
                    return branchService.getAllBranches();
                }

                @Override
                protected void done() {
                    try {
                        List<Branch> branches = get();
                        if (branches.isEmpty()) {
                            statusLabel.setText("No branches found.");
                        } else {
                            branches.forEach(branch -> tableModel.addRow(new Object[]{branch.getId(), branch.getName(), branch.getLocation()}));
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

    private JPanel createPlaceOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Customer and Branch Selection
        JPanel topSelectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.insets = new Insets(5,5,5,5);
        gbcTop.anchor = GridBagConstraints.WEST;

        JTextField customerIdField = new JTextField("CUST-" + System.currentTimeMillis()%10000, 15);
        JComboBox<Branch> branchComboBox = new JComboBox<>();

        gbcTop.gridx = 0;
        gbcTop.gridy = 0;
        topSelectionPanel.add(new JLabel("Customer ID:"), gbcTop);
        gbcTop.gridx = 1;
        topSelectionPanel.add(customerIdField, gbcTop);
        gbcTop.gridx = 0;
        gbcTop.gridy = 1;
        topSelectionPanel.add(new JLabel("Branch:"), gbcTop);
        gbcTop.gridx = 1;
        gbcTop.fill = GridBagConstraints.HORIZONTAL;
        topSelectionPanel.add(branchComboBox, gbcTop);

        // Middle: Drink Selection and Order Cart
        JSplitPane middleSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        middleSplitPane.setResizeWeight(0.5);

        // Available Drinks Panel
        JPanel availableDrinksOuterPanel = new JPanel(new BorderLayout(5,5));
        availableDrinksOuterPanel.setBorder(BorderFactory.createTitledBorder("Available Drinks"));
        DefaultListModel<Drink> drinksListModel = new DefaultListModel<>();
        JList<Drink> availableDrinksList = new JList<>(drinksListModel);
        availableDrinksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableDrinksOuterPanel.add(new JScrollPane(availableDrinksList), BorderLayout.CENTER);

        JPanel drinkControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JTextField quantityField = new JTextField("1", 5); // Changed from JSpinner
        JButton addToOrderButton = new JButton("Add to Order");
        drinkControlPanel.add(new JLabel("Qty:"));
        drinkControlPanel.add(quantityField);
        drinkControlPanel.add(addToOrderButton);
        availableDrinksOuterPanel.add(drinkControlPanel, BorderLayout.SOUTH);
        middleSplitPane.setLeftComponent(availableDrinksOuterPanel);

        // Current Order Cart Panel
        JPanel currentOrderPanel = new JPanel(new BorderLayout(5,5));
        currentOrderPanel.setBorder(BorderFactory.createTitledBorder("Current Order Items"));
        String[] orderCartCols = {"Drink Name", "Qty", "Price", "Total"};
        DefaultTableModel orderCartModel = new DefaultTableModel(orderCartCols, 0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable orderCartTable = new JTable(orderCartModel);
        orderCartTable.setRowHeight(22);
        currentOrderPanel.add(new JScrollPane(orderCartTable), BorderLayout.CENTER);
        JButton removeFromOrderButton = new JButton("Remove Selected");
        currentOrderPanel.add(removeFromOrderButton, BorderLayout.SOUTH);
        middleSplitPane.setRightComponent(currentOrderPanel);

        // Bottom: Total and Submit
        JPanel bottomActionPanel = new JPanel(new BorderLayout(10,5));
        JLabel orderTotalLabel = new JLabel("Order Total: 0.00", SwingConstants.RIGHT);
        orderTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JButton submitOrderButton = new JButton("Submit Order");
        submitOrderButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitOrderButton.setBackground(new Color(34, 139, 34));
        submitOrderButton.setForeground(Color.WHITE);
        JLabel placeOrderStatusLabel = new JLabel(" ", SwingConstants.CENTER);

        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitPanel.add(submitOrderButton);

        bottomActionPanel.add(orderTotalLabel, BorderLayout.CENTER);
        bottomActionPanel.add(submitPanel, BorderLayout.EAST);
        bottomActionPanel.add(placeOrderStatusLabel, BorderLayout.SOUTH);

        panel.add(topSelectionPanel, BorderLayout.NORTH);
        panel.add(middleSplitPane, BorderLayout.CENTER);
        panel.add(bottomActionPanel, BorderLayout.SOUTH);

        // --- Logic for Place Order ---
        Map<String, Integer> currentOrderItemsMap = new HashMap<>();
        Map<String, Drink> currentOrderDrinkObjects = new HashMap<>();

        Runnable refreshAction = () -> {
            branchComboBox.removeAllItems();
            drinksListModel.clear();
            new SwingWorker<Void,Void>(){
                List<Branch> branches; List<Drink> drinks;
                protected Void doInBackground() throws Exception {
                    branches = branchService.getAllBranches();
                    drinks = drinkService.getAllDrinks();
                    return null;
                }
                protected void done(){
                    try {
                        get();
                        branches.forEach(branchComboBox::addItem);
                        drinks.forEach(drinksListModel::addElement);
                        // Auto-select branch for non-admins
                        if (loggedInUser.getRole() != User.UserRole.ADMIN && loggedInUser.getBranchId() != null) {
                            for (int i = 0; i < branchComboBox.getItemCount(); i++) {
                                if (branchComboBox.getItemAt(i).getId().equals(loggedInUser.getBranchId())) {
                                    branchComboBox.setSelectedIndex(i);
                                    break;
                                }
                            }
                            branchComboBox.setEnabled(false);
                        }
                    } catch(Exception e) {
                        JOptionPane.showMessageDialog(panel, "Error loading initial data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        };
        panel.putClientProperty("refreshAction", refreshAction);
        refreshAction.run();

        addToOrderButton.addActionListener(e -> {
            Drink selectedDrink = availableDrinksList.getSelectedValue();
            int quantity;
            try {
                quantity = Integer.parseInt(quantityField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid number for quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedDrink != null && quantity > 0) {
                currentOrderItemsMap.put(selectedDrink.getId(), currentOrderItemsMap.getOrDefault(selectedDrink.getId(), 0) + quantity);
                currentOrderDrinkObjects.putIfAbsent(selectedDrink.getId(), selectedDrink);
                updateOrderCartTable(orderCartModel, currentOrderItemsMap, currentOrderDrinkObjects, orderTotalLabel);
                quantityField.setText("1");
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
                        drinkIdToRemove = entry.getKey();
                        break;
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
            String customerId = customerIdField.getText();
            Branch selectedBranch = (Branch) branchComboBox.getSelectedItem();
            if (customerId.isEmpty() || selectedBranch == null || currentOrderItemsMap.isEmpty()) {
                placeOrderStatusLabel.setText("Customer ID, Branch, and Items are required.");
                placeOrderStatusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(panel, "Customer ID, Branch selection, and at least one item are required.", "Order Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            placeOrderStatusLabel.setText("Submitting order...");
            placeOrderStatusLabel.setForeground(Color.BLUE);
            submitOrderButton.setEnabled(false);

            new SwingWorker<Order, Void>() {
                @Override protected Order doInBackground() throws Exception {
                    return orderService.placeOrder(customerId, selectedBranch.getId(), currentOrderItemsMap);
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
                        customerIdField.setText("CUST-" + System.currentTimeMillis() % 10000);
                    } catch (Exception ex) {
                        String err = "Order submission failed: " + (ex.getCause()!=null ? ex.getCause().getMessage() : ex.getMessage());
                        placeOrderStatusLabel.setText(err);
                        placeOrderStatusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(panel, err, "Order Submission Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        submitOrderButton.setEnabled(true);
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
                cartModel.addRow(new Object[]{drink.getName(), entry.getValue(), String.format("%.2f", drink.getPrice()), String.format("%.2f", itemTotal)});
                currentTotal += itemTotal;
            }
        }
        totalLabel.setText("Order Total: " + String.format("%.2f", currentTotal));
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JComboBox<Object> branchReportComboBox = new JComboBox<>();
        JButton getBranchSalesButton = new JButton("Get Branch Sales Report");
        JButton getOverallSalesButton = new JButton("Get Overall Business Report");
        JTextArea reportResultsArea = new JTextArea(15, 60);
        reportResultsArea.setEditable(false);
        reportResultsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(reportResultsArea);

        Runnable refreshAction = () -> {
            branchReportComboBox.removeAllItems();
            branchReportComboBox.addItem("--- Select Branch ---");
            new SwingWorker<List<Branch>, Void>() {
                protected List<Branch> doInBackground() throws Exception { return branchService.getAllBranches(); }
                protected void done() {
                    try {
                        List<Branch> branches = get();
                        branches.forEach(branchReportComboBox::addItem);
                        if (loggedInUser.getRole() != User.UserRole.ADMIN && loggedInUser.getBranchId() != null) {
                            for (int i = 0; i < branchReportComboBox.getItemCount(); i++) {
                                if (branchReportComboBox.getItemAt(i) instanceof Branch) {
                                    Branch branch = (Branch) branchReportComboBox.getItemAt(i);
                                    if (branch.getId().equals(loggedInUser.getBranchId())) {
                                        branchReportComboBox.setSelectedItem(branch);
                                        break;
                                    }
                                }
                            }
                            branchReportComboBox.setEnabled(false);
                        }
                    } catch (Exception e) {
                        reportResultsArea.setText("Error loading branches.");
                    }
                }
            }.execute();
        };
        panel.putClientProperty("refreshAction", refreshAction);
        refreshAction.run();

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Select Branch:"), gbc);
        gbc.gridx=1; gbc.gridy=0; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(branchReportComboBox, gbc);
        gbc.gridx=2; gbc.gridy=0; gbc.fill = GridBagConstraints.NONE; panel.add(getBranchSalesButton, gbc);

        if (loggedInUser.getRole() == User.UserRole.ADMIN) {
            gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=3; gbc.anchor=GridBagConstraints.CENTER;
            panel.add(getOverallSalesButton, gbc);
        }

        gbc.gridwidth=1;
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=3; gbc.fill=GridBagConstraints.BOTH;
        gbc.weightx=1; gbc.weighty=1;
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
                            StringBuilder sb = new StringBuilder("Branch Sales Report for: " + selectedBranch.getName() + "\n=====================================\n");
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

    private JPanel createAdminConsolePanel() {
        JTabbedPane adminTabbedPane = new JTabbedPane();
        adminTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        adminTabbedPane.addTab("Add Item/Branch", createAddItemsPanel());
        adminTabbedPane.addTab("Manage Stock", createManageStockPanel());
        adminTabbedPane.addTab("Manage Users", createManageUsersPanel());
        adminTabbedPane.addTab("Low Stock Alerts", createLowStockAlertsPanel());

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(adminTabbedPane, BorderLayout.CENTER);

        wrapperPanel.putClientProperty("refreshAction", (Runnable)() -> {
            Component selectedComponent = adminTabbedPane.getSelectedComponent();
            if (selectedComponent instanceof JPanel) {
                JPanel selectedPanel = (JPanel) selectedComponent;
                if (selectedPanel.getClientProperty("refreshButton") instanceof JButton) {
                    ((JButton)selectedPanel.getClientProperty("refreshButton")).doClick();
                }
            }
        });

        return wrapperPanel;
    }

    private JPanel createAddItemsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add Drink Panel
        JPanel addDrinkPanel = new JPanel(new GridBagLayout());
        addDrinkPanel.setBorder(BorderFactory.createTitledBorder("Add New Drink"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField drinkIdField=new JTextField(15);
        JTextField drinkNameField=new JTextField(15);
        JTextField drinkBrandField=new JTextField(15);
        JTextField drinkPriceField=new JTextField(10);
        JTextField drinkInitialStockField=new JTextField(5);
        JButton addDrinkButton=new JButton("Add Drink");
        JLabel addDrinkStatusLabel=new JLabel(" ");
        addDrinkStatusLabel.setPreferredSize(new Dimension(250, 20));

        gbc.gridx = 0; gbc.gridy = 0; addDrinkPanel.add(new JLabel("Drink ID:"), gbc);
        gbc.gridx = 1; addDrinkPanel.add(drinkIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addDrinkPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; addDrinkPanel.add(drinkNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; addDrinkPanel.add(new JLabel("Brand:"), gbc);
        gbc.gridx = 1; addDrinkPanel.add(drinkBrandField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; addDrinkPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; addDrinkPanel.add(drinkPriceField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; addDrinkPanel.add(new JLabel("Initial HQ Stock:"), gbc);
        gbc.gridx = 1; addDrinkPanel.add(drinkInitialStockField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        addDrinkPanel.add(addDrinkButton, gbc);
        gbc.gridy = 6;
        addDrinkPanel.add(addDrinkStatusLabel, gbc);

        addDrinkButton.addActionListener(event -> {
            try {
                String id = drinkIdField.getText();
                String name = drinkNameField.getText();
                String brand = drinkBrandField.getText();
                double price = Double.parseDouble(drinkPriceField.getText());
                int stock = Integer.parseInt(drinkInitialStockField.getText());

                if (id.isEmpty() || name.isEmpty()) {
                    addDrinkStatusLabel.setText("ID and Name are required.");
                    addDrinkStatusLabel.setForeground(Color.RED);
                    return;
                }
                addDrinkStatusLabel.setText("Adding...");
                addDrinkStatusLabel.setForeground(Color.BLUE);

                new SwingWorker<Void, Void>() {
                    protected Void doInBackground() throws Exception {
                        drinkService.addDrink(new Drink(id, name, brand, price, stock));
                        return null;
                    }
                    protected void done() {
                        try {
                            get();
                            addDrinkStatusLabel.setText("Drink '" + name + "' added!");
                            addDrinkStatusLabel.setForeground(new Color(0, 128, 0));
                            drinkIdField.setText("");
                            drinkNameField.setText("");
                            drinkBrandField.setText("");
                            drinkPriceField.setText("");
                            drinkInitialStockField.setText("");
                            mainFrame.refreshDataViews();
                        } catch (Exception ex) {
                            String error = "Error: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                            addDrinkStatusLabel.setText(error);
                            addDrinkStatusLabel.setForeground(Color.RED);
                            JOptionPane.showMessageDialog(panel, error, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            } catch (NumberFormatException nfe) {
                addDrinkStatusLabel.setText("Price/Stock must be numbers.");
                addDrinkStatusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(panel, "Invalid Price or Stock.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(addDrinkPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Add Branch Panel
        JPanel addBranchPanel = new JPanel(new GridBagLayout());
        addBranchPanel.setBorder(BorderFactory.createTitledBorder("Add New Branch"));
        JTextField branchIdField=new JTextField(15), branchNameField=new JTextField(15), branchLocationField=new JTextField(15);
        JButton addBranchButton=new JButton("Add Branch");
        JLabel addBranchStatusLabel=new JLabel(" ");
        addBranchStatusLabel.setPreferredSize(new Dimension(250, 20));

        gbc.gridy=0; gbc.gridwidth=1; addBranchPanel.add(new JLabel("ID:"),gbc);
        gbc.gridx=1; addBranchPanel.add(branchIdField,gbc);
        gbc.gridx=0; gbc.gridy=1; addBranchPanel.add(new JLabel("Name:"),gbc);
        gbc.gridx=1; addBranchPanel.add(branchNameField,gbc);
        gbc.gridx=0; gbc.gridy=2; addBranchPanel.add(new JLabel("Location:"),gbc);
        gbc.gridx=1; addBranchPanel.add(branchLocationField,gbc);
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.CENTER;
        addBranchPanel.add(addBranchButton,gbc);
        gbc.gridy=4; addBranchPanel.add(addBranchStatusLabel,gbc);

        addBranchButton.addActionListener(e -> {
            try {
                String id=branchIdField.getText(), name=branchNameField.getText(), loc=branchLocationField.getText();
                if(id.isEmpty() || name.isEmpty()){addBranchStatusLabel.setText("ID/Name required.");addBranchStatusLabel.setForeground(Color.RED);return;}
                addBranchStatusLabel.setText("Adding..."); addBranchStatusLabel.setForeground(Color.BLUE);
                new SwingWorker<Void, Void>() {
                    protected Void doInBackground() throws Exception { branchService.addBranch(new Branch(id,name,loc)); return null; }
                    protected void done() {
                        try{
                            get();
                            addBranchStatusLabel.setText("Branch '"+name+"' added!");
                            addBranchStatusLabel.setForeground(new Color(0,128,0));
                            branchIdField.setText(""); branchNameField.setText(""); branchLocationField.setText("");
                            mainFrame.refreshDataViews();
                        } catch(Exception ex) {
                            String err="Error: "+(ex.getCause()!=null?ex.getCause().getMessage():ex.getMessage());
                            addBranchStatusLabel.setText(err);
                            addBranchStatusLabel.setForeground(Color.RED);
                            JOptionPane.showMessageDialog(panel,err,"Error",0);
                        }
                    }
                }.execute();
            } catch(Exception ex) { addBranchStatusLabel.setText("Unexpected error."); addBranchStatusLabel.setForeground(Color.RED); }
        });
        panel.add(addBranchPanel);
        return panel;
    }

    private JPanel createManageStockPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Manage Stock"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5); gbc.anchor=GridBagConstraints.WEST;

        JTextField branchIdField=new JTextField(10), drinkIdField=new JTextField(10);
        JTextField quantityField=new JTextField(5), thresholdField=new JTextField(5);
        JButton setStockButton=new JButton("Set Stock Level"), setThresholdButton=new JButton("Set Threshold");
        JLabel statusLabel=new JLabel(" "); statusLabel.setPreferredSize(new Dimension(300,20));

        gbc.gridx=0;gbc.gridy=0;panel.add(new JLabel("Branch ID:"),gbc);gbc.gridx=1;panel.add(branchIdField,gbc);
        gbc.gridx=2;gbc.gridy=0;panel.add(new JLabel("Drink ID:"),gbc);gbc.gridx=3;panel.add(drinkIdField,gbc);
        gbc.gridx=0;gbc.gridy=1;panel.add(new JLabel("Quantity:"),gbc);gbc.gridx=1;panel.add(quantityField,gbc);
        gbc.gridx=2;gbc.gridy=1;gbc.gridwidth=2;gbc.fill=GridBagConstraints.HORIZONTAL;panel.add(setStockButton,gbc);
        gbc.gridwidth=1;gbc.fill=GridBagConstraints.NONE;
        gbc.gridx=0;gbc.gridy=2;panel.add(new JLabel("Threshold:"),gbc);gbc.gridx=1;panel.add(thresholdField,gbc);
        gbc.gridx=2;gbc.gridy=2;gbc.gridwidth=2;gbc.fill=GridBagConstraints.HORIZONTAL;panel.add(setThresholdButton,gbc);
        gbc.gridwidth=1;gbc.fill=GridBagConstraints.NONE;
        gbc.gridx=0;gbc.gridy=3;gbc.gridwidth=4;gbc.anchor=GridBagConstraints.CENTER;panel.add(statusLabel,gbc);

        setStockButton.addActionListener(e -> {
            try {
                String branchId=branchIdField.getText(), drinkId=drinkIdField.getText();
                int quantity=Integer.parseInt(quantityField.getText());
                if(branchId.isEmpty()||drinkId.isEmpty())throw new IllegalArgumentException("IDs req.");
                statusLabel.setText("Setting level...");statusLabel.setForeground(Color.BLUE);
                new SwingWorker<Void,Void>() {
                    protected Void doInBackground()throws Exception{stockService.setStockLevel(branchId,drinkId,quantity);return null;}
                    protected void done(){try{get();statusLabel.setText("Level set for "+drinkId);statusLabel.setForeground(new Color(0,128,0));quantityField.setText("");}catch(Exception ex){String err="Err: "+(ex.getCause()!=null?ex.getCause().getMessage():ex.getMessage());statusLabel.setText(err);statusLabel.setForeground(Color.RED);JOptionPane.showMessageDialog(panel,err,"Error",0);}}
                }.execute();
            } catch(IllegalArgumentException ex) {
                statusLabel.setText("Err: "+ex.getMessage());statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(panel,"Invalid input: "+ex.getMessage(),"Error",0);
            }
        });
        setThresholdButton.addActionListener(e -> {
            try {
                String branchId=branchIdField.getText(), drinkId=drinkIdField.getText();
                int threshold=Integer.parseInt(thresholdField.getText());
                if(branchId.isEmpty()||drinkId.isEmpty())throw new IllegalArgumentException("IDs req.");
                statusLabel.setText("Setting threshold...");statusLabel.setForeground(Color.BLUE);
                new SwingWorker<Void,Void>() {
                    protected Void doInBackground()throws Exception{stockService.setStockThreshold(branchId,drinkId,threshold);return null;}
                    protected void done(){try{get();statusLabel.setText("Threshold set for "+drinkId);statusLabel.setForeground(new Color(0,128,0));thresholdField.setText("");}catch(Exception ex){String err="Err: "+(ex.getCause()!=null?ex.getCause().getMessage():ex.getMessage());statusLabel.setText(err);statusLabel.setForeground(Color.RED);JOptionPane.showMessageDialog(panel,err,"Error",0);}}
                }.execute();
            } catch(IllegalArgumentException ex) {
                statusLabel.setText("Err: "+ex.getMessage());statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(panel,"Invalid input: "+ex.getMessage(),"Error",0);
            }
        });
        return panel;
    }

    private JPanel createManageUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("Manage System Users"));

        String[]userTableColumns={"Username","Role","Branch ID"};
        DefaultTableModel userTableModel=new DefaultTableModel(userTableColumns,0){public boolean isCellEditable(int r,int c){return false;}};
        JTable userTable=new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton removeUserButton=new JButton("Remove Selected User");
        JButton refreshUsersButton=new JButton("Refresh List");
        JPanel bottomPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshUsersButton);
        bottomPanel.add(removeUserButton);

        panel.add(new JScrollPane(userTable),BorderLayout.CENTER);
        panel.add(bottomPanel,BorderLayout.SOUTH);
        panel.putClientProperty("refreshButton", refreshUsersButton);

        refreshUsersButton.addActionListener(e -> {
            userTableModel.setRowCount(0);
            new SwingWorker<List<User>,Void>(){
                protected List<User> doInBackground()throws Exception{return authService.getAllUsers();}
                protected void done(){try{get().forEach(u->userTableModel.addRow(new Object[]{u.getUsername(),u.getRole(),u.getBranchId()==null?"N/A":u.getBranchId()}));}catch(Exception ex){JOptionPane.showMessageDialog(panel,"Error loading users","Error",0);}}
            }.execute();
        });

        removeUserButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if(selectedRow >= 0){
                String username = (String)userTable.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to remove user '"+username+"'?","Confirm Deletion",JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION){
                    new SwingWorker<Void,Void>(){
                        protected Void doInBackground()throws Exception{authService.removeUser(username);return null;}
                        protected void done(){
                            try{get();JOptionPane.showMessageDialog(panel,"User removed.");refreshUsersButton.doClick();}
                            catch(Exception ex){JOptionPane.showMessageDialog(panel,"Error: "+(ex.getCause()!=null?ex.getCause().getMessage():ex.getMessage()),"Error",0);}
                        }
                    }.execute();
                }
            } else {
                JOptionPane.showMessageDialog(panel,"Please select a user to remove.","Error",2);
            }
        });

        JPanel addUserPanel=new JPanel(new GridBagLayout());
        addUserPanel.setBorder(BorderFactory.createTitledBorder("Add New User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.WEST;

        JTextField newUsernameField=new JTextField(15);
        JPasswordField newUserPasswordField=new JPasswordField(15);
        JComboBox<User.UserRole>roleComboBox=new JComboBox<>(User.UserRole.values());
        JComboBox<Object>branchComboBox=new JComboBox<>();
        branchComboBox.addItem("N/A (for ADMIN role)");
        new SwingWorker<List<Branch>,Void>(){protected List<Branch>doInBackground()throws Exception{return branchService.getAllBranches();}protected void done(){try{get().forEach(branchComboBox::addItem);}catch(Exception e){}}}.execute();
        JButton addUserButton=new JButton("Add User");
        JLabel addUserStatusLabel=new JLabel(" ");
        addUserStatusLabel.setPreferredSize(new Dimension(250,20));

        gbc.gridx=0;gbc.gridy=0;addUserPanel.add(new JLabel("Username:"),gbc);gbc.gridx=1;addUserPanel.add(newUsernameField,gbc);
        gbc.gridx=0;gbc.gridy=1;addUserPanel.add(new JLabel("Password:"),gbc);gbc.gridx=1;addUserPanel.add(newUserPasswordField,gbc);
        gbc.gridx=0;gbc.gridy=2;addUserPanel.add(new JLabel("Role:"),gbc);gbc.gridx=1;addUserPanel.add(roleComboBox,gbc);
        gbc.gridx=0;gbc.gridy=3;addUserPanel.add(new JLabel("Branch:"),gbc);gbc.gridx=1;addUserPanel.add(branchComboBox,gbc);
        gbc.gridx=0;gbc.gridy=4;gbc.gridwidth=2;gbc.anchor=GridBagConstraints.CENTER;addUserPanel.add(addUserButton,gbc);
        gbc.gridy=5;addUserPanel.add(addUserStatusLabel,gbc);

        addUserButton.addActionListener(e -> {
            String username=newUsernameField.getText();
            String password=new String(newUserPasswordField.getPassword());
            User.UserRole role=(User.UserRole)roleComboBox.getSelectedItem();
            Object branchSelection=branchComboBox.getSelectedItem();
            String branchId = null;
            if(branchSelection instanceof Branch) {
                branchId = ((Branch)branchSelection).getId();
            }
            if(username.isEmpty()||password.isEmpty()||role==null){addUserStatusLabel.setText("All fields are required.");addUserStatusLabel.setForeground(Color.RED);return;}
            if(role!=User.UserRole.ADMIN && branchId==null){addUserStatusLabel.setText("Branch must be selected for non-admins.");addUserStatusLabel.setForeground(Color.RED);return;}
            addUserStatusLabel.setText("Adding user...");addUserStatusLabel.setForeground(Color.BLUE);

            final String finalBranchId = branchId; // For use in lambda
            new SwingWorker<Void,Void>(){
                protected Void doInBackground()throws Exception{authService.addUser(username,password,role,finalBranchId);return null;}
                protected void done(){
                    try{get();addUserStatusLabel.setText("User '"+username+"' added!");addUserStatusLabel.setForeground(new Color(0,128,0));
                        newUsernameField.setText("");newUserPasswordField.setText("");roleComboBox.setSelectedIndex(0);branchComboBox.setSelectedIndex(0);
                        refreshUsersButton.doClick();
                    }catch(Exception ex){String err="Error: "+(ex.getCause()!=null?ex.getCause().getMessage():ex.getMessage());addUserStatusLabel.setText(err);addUserStatusLabel.setForeground(Color.RED);JOptionPane.showMessageDialog(panel,err,"Error",0);}
                }
            }.execute();
        });
        panel.add(addUserPanel,BorderLayout.EAST);

        SwingUtilities.invokeLater(refreshUsersButton::doClick);
        return panel;
    }

    private JPanel createLowStockAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("System-Wide Low Stock Alerts"));
        JTextArea alertsArea = new JTextArea(15, 60);
        alertsArea.setEditable(false);
        alertsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JButton refreshAlertsButton = new JButton("Check for Low Stock");
        panel.add(new JScrollPane(alertsArea), BorderLayout.CENTER);
        panel.add(refreshAlertsButton, BorderLayout.SOUTH);
        panel.putClientProperty("refreshButton", refreshAlertsButton); // For external refresh

        refreshAlertsButton.addActionListener(e -> {
            alertsArea.setText("Checking for low stock alerts across all branches...");
            refreshAlertsButton.setEnabled(false);
            new SwingWorker<List<String>, Void>() {
                @Override protected List<String> doInBackground() throws Exception { return stockService.checkLowStockLevelsGlobally(); }
                @Override protected void done() {
                    try {
                        List<String> alerts = get();
                        StringBuilder sb = new StringBuilder();
                        if (alerts.isEmpty() || (alerts.size() == 1 && alerts.get(0).startsWith("All stock levels"))) {
                            sb.append(alerts.get(0));
                        } else {
                            sb.append("WARNING: The following items are below their minimum threshold:\n\n");
                            alerts.forEach(alert -> sb.append(alert).append("\n"));
                        }
                        alertsArea.setText(sb.toString());
                    } catch (Exception ex) {
                        String err = "Error fetching alerts: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                        alertsArea.setText(err);
                        JOptionPane.showMessageDialog(panel, err, "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        refreshAlertsButton.setEnabled(true);
                    }
                }
            }.execute();
        });
        SwingUtilities.invokeLater(refreshAlertsButton::doClick);
        return panel;
    }
}



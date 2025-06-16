package View.GUIClasses;


import Model.DataEntities.User;
import Model.ServiceInterfaces.IAuthService;
import com.sun.tools.javac.Main;

import javax.swing.*;
import java.awt.*;


public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final IAuthService authService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JLabel statusLabel;

    public LoginPanel(MainFrame mainFrame, IAuthService authService) {
        this.mainFrame = mainFrame;
        this.authService = authService;

        // Use a wrapper panel with GridBagLayout to center the form panel
        setLayout(new GridBagLayout());

        // Form panel with its own GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Label
        JLabel titleLabel = new JLabel("Drink Enterprise System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(60, 63, 65));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.ipady = 20;
        formPanel.add(titleLabel, gbc);

        // Reset grid width and padding
        gbc.ipady = 0;
        gbc.gridwidth = 1;

        // Username field
        gbc.gridy = 1;
        formPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        usernameField.setText("admin"); // Default for easy testing

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);
        passwordField.setText("adminpass"); // Default for easy testing

        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 50;
        gbc.ipady = 10;
        formPanel.add(loginButton, gbc);

        // Status label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        // Add the form panel to the main panel, which will center it
        add(formPanel, new GridBagConstraints());

        // Event Listeners
        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin()); // Allow login on Enter key
    }

    /**
     * Handles the login logic, using a SwingWorker to call the RMI service
     * on a background thread to prevent the GUI from freezing.
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required.");
            statusLabel.setForeground(Color.RED);
            return;
        }

        statusLabel.setText("Logging in...");
        statusLabel.setForeground(Color.BLUE);
        loginButton.setEnabled(false); // Prevent multiple clicks

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                // This happens on a background thread
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                // This happens on the EDT after doInBackground is finished
                try {
                    User loggedInUser = get(); // Retrieves the result from doInBackground
                    statusLabel.setText("Login successful!");
                    mainFrame.showAppPanel(loggedInUser);
                } catch (Exception ex) {
                    // Extract the root cause message from the RMI exception
                    String errorMessage = "Login failed: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                    statusLabel.setText(errorMessage);
                    statusLabel.setForeground(Color.RED);
                } finally {
                    loginButton.setEnabled(true); // Re-enable the login button
                }
            }
        }.execute();
    }
    }

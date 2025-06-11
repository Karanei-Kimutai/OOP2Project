In this application, the Controller is not a separate set of classes but is integrated into the client-side code. It's the "glue" that connects the user's actions in the View to the business logic in the Model.

Embedded GUI Controllers: The ActionListeners and SwingWorkers inside the GUI classes (LoginPanel.java, AppPanel.java) act as the controller.

When a user clicks a button, the ActionListener is triggered.
It then calls the appropriate RMI service method (communicating with the Model).
The results are used to update the GUI components (the View).
Embedded CLI Controller: The while loop and switch statement inside CLIRunner.java serve as the controller for the command-line interface. It reads user input, calls the RMI service methods, and prints the results back to the console.
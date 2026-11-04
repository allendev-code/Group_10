
package com.my.rem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


public class DashboardFrame extends JFrame implements ActionListener {
    private JButton btnProperties, btnClients, btnTransactions, btnReports, btnUsers, btnLogout;
    private JLabel lblWelcome, lblInfo;
    private final RealEstateManager manager;
    private PropertyFrame propertyWindow;
    private ClientFrame clientWindow;
    private TransactionFrame transactionWindow;
    private ReportFrame reportWindow;
    private user_frame userWindow;

    public DashboardFrame(String role, String username) {
        manager = new RealEstateManager(role, username);
        boolean admin = manager.isAdmin();

        setTitle(admin ? "Admin Dashboard" : "Agent Dashboard");
        setSize(400, admin ? 400 : 350);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        lblWelcome = new JLabel(admin ? "Welcome, Administrator " + username + "!"
                                      : "Welcome, Agent " + username + "!");
        lblWelcome.setBounds(110, 20, 320, 25);
        add(lblWelcome);

        lblInfo = new JLabel(admin ? "Properties, clients, transactions, users and reports"
                                   : "View properties & clients, record sales, view sales report");
        lblInfo.setBounds(50, 45, 340, 25);
        add(lblInfo);

        btnProperties = new JButton(admin ? "Manage Properties" : "View Properties");
        btnProperties.setBounds(100, 85, 200, 35);
        add(btnProperties);

        btnClients = new JButton(admin ? "Manage Clients" : "View Clients");
        btnClients.setBounds(100, 130, 200, 35);
        add(btnClients);

        btnTransactions = new JButton("Transactions");
        btnTransactions.setBounds(100, 175, 200, 35);
        add(btnTransactions);

        btnReports = new JButton("Reports");
        btnReports.setBounds(100, 220, 200, 35);
        add(btnReports);

        btnUsers = new JButton("Manage Users");
        btnUsers.setBounds(100, 265, 200, 35);
        add(btnUsers);

        btnLogout = new JButton("Logout");
        btnLogout.setBounds(100, admin ? 310 : 265, 200, 35);
        add(btnLogout);

        if (!admin) {
            btnUsers.setVisible(false);
        }

        btnProperties.addActionListener(this);
        btnClients.addActionListener(this);
        btnTransactions.addActionListener(this);
        btnReports.addActionListener(this);
        btnUsers.addActionListener(this);
        btnLogout.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnProperties) {
            if (propertyWindow == null || !propertyWindow.isDisplayable()) {
                propertyWindow = new PropertyFrame(manager);
                propertyWindow.setVisible(true);
            } else {
                propertyWindow.toFront();
            }
        } else if (e.getSource() == btnClients) {
            if (clientWindow == null || !clientWindow.isDisplayable()) {
                clientWindow = new ClientFrame(manager);
                clientWindow.setVisible(true);
            } else {
                clientWindow.toFront();
            }
        } else if (e.getSource() == btnTransactions) {
            if (transactionWindow == null || !transactionWindow.isDisplayable()) {
                transactionWindow = new TransactionFrame(manager);
                transactionWindow.setVisible(true);
            } else {
                transactionWindow.toFront();
            }
        } else if (e.getSource() == btnReports) {
            if (reportWindow == null || !reportWindow.isDisplayable()) {
                reportWindow = new ReportFrame(manager);
                reportWindow.setVisible(true);
            } else {
                reportWindow.toFront();
            }
        } else if (e.getSource() == btnUsers) {
            if (!manager.isAdmin()) return;
            if (userWindow == null || !userWindow.isDisplayable()) {
                userWindow = new user_frame(manager);
                userWindow.setVisible(true);
            } else {
                userWindow.toFront();
            }
        } else if (e.getSource() == btnLogout) {
            JFrame[] windows = {propertyWindow, clientWindow, transactionWindow, reportWindow, userWindow};
            for (JFrame w : windows) {
                if (w != null) w.dispose();
            }
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }
}

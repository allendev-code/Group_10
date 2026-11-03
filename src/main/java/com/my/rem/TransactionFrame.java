
package com.my.rem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class TransactionFrame extends JFrame implements ActionListener {
    private final RealEstateManager manager;
    private DefaultTableModel propertyModel, clientModel, transModel;
    private JTable propertyTable, clientTable, transTable;
    private JScrollPane propertyScroll, clientScroll, transScroll;
    private JTextField txtPID, txtCID;
    private JLabel lbHeader, lbPID, lbCID, lbHint, lbHistory;
    private JButton btnRecord, btnCancel, btnRefresh;

    TransactionFrame(RealEstateManager manager) {
        this.manager = manager;
        String[] propColumns = {"Property ID", "Location", "Price", "Status"};
        propertyModel = new DefaultTableModel(propColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String[] clientColumns = {"Client ID", "Name"};
        clientModel = new DefaultTableModel(clientColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String[] transColumns = {"Trans. ID", "Property ID", "Location", "Client ID", "Client Name", "Amount (PHP)", "Date", "Recorded By"};
        transModel = new DefaultTableModel(transColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        setTitle("Property Sale Transactions");
        setSize(1000, 580);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        lbHeader = new JLabel("Record Property Sale");
        lbHeader.setBounds(10, 5, 250, 30);
        add(lbHeader);

        lbPID = new JLabel("Property ID:");
        lbPID.setBounds(10, 45, 100, 30);
        add(lbPID);

        txtPID = new JTextField();
        txtPID.setBounds(110, 45, 200, 30);
        add(txtPID);

        lbCID = new JLabel("Client ID:");
        lbCID.setBounds(10, 85, 100, 30);
        add(lbCID);

        txtCID = new JTextField();
        txtCID.setBounds(110, 85, 200, 30);
        add(txtCID);

        lbHint = new JLabel("Tip: you can also select a property / client row.");
        lbHint.setBounds(10, 125, 310, 20);
        add(lbHint);

        btnRecord = new JButton("Record Sale");
        btnRecord.setBounds(10, 160, 140, 30);
        add(btnRecord);

        btnCancel = new JButton("Cancel Sale");
        btnCancel.setBounds(170, 160, 140, 30);
        add(btnCancel);

        btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(10, 200, 300, 25);
        add(btnRefresh);

        propertyTable = new JTable(propertyModel);
        propertyScroll = new JScrollPane(propertyTable);
        propertyScroll.setBounds(330, 10, 400, 215);
        add(propertyScroll);

        clientTable = new JTable(clientModel);
        clientScroll = new JScrollPane(clientTable);
        clientScroll.setBounds(740, 10, 240, 215);
        add(clientScroll);

        lbHistory = new JLabel("Transaction History (select a row, then Cancel Sale - Admin only)");
        lbHistory.setBounds(10, 235, 500, 25);
        add(lbHistory);

        transTable = new JTable(transModel);
        transScroll = new JScrollPane(transTable);
        transScroll.setBounds(10, 265, 970, 265);
        add(transScroll);

        btnRecord.addActionListener(this);
        btnCancel.addActionListener(this);
        btnRefresh.addActionListener(this);

        if (!manager.isAdmin()) {
            btnCancel.setEnabled(false);
        }

        refreshAll();
    }

    private void refreshAll() {
        String error = manager.loadDataFromDatabase();
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        }

        propertyModel.setRowCount(0);
        for (Property p : manager.getProperties()) {
            propertyModel.addRow(new Object[]{p.getPropertyId(), p.getLocation(), String.format("%,.2f", p.getPrice()), p.getStatus()});
        }

        clientModel.setRowCount(0);
        for (Client c : manager.getClients()) {
            clientModel.addRow(new Object[]{c.getClientId(), c.getName()});
        }

        transModel.setRowCount(0);
        ArrayList<Transaction> list = manager.getTransactions();
        for (int i = list.size() - 1; i >= 0; i--) {
            Transaction t = list.get(i);
            Property p = manager.searchById(t.getPropertyId());
            Client c = manager.getClient(t.getClientId());
            transModel.addRow(new Object[]{t.getTransactionId(), t.getPropertyId(),
                p == null ? "(deleted)" : p.getLocation(), t.getClientId(),
                c == null ? "(deleted)" : c.getName(), String.format("%,.2f", t.getAmount()),
                t.getDate(), t.getRecordedBy()});
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRecord) {
            String PID = txtPID.getText().trim();
            String CID = txtCID.getText().trim();
            if (PID.isEmpty() && propertyTable.getSelectedRow() != -1) {
                PID = propertyModel.getValueAt(propertyTable.getSelectedRow(), 0).toString();
            }
            if (CID.isEmpty() && clientTable.getSelectedRow() != -1) {
                CID = clientModel.getValueAt(clientTable.getSelectedRow(), 0).toString();
            }
            if (!PID.isEmpty() && !CID.isEmpty()) {
                try {
                    int propertyId = Integer.parseInt(PID);
                    int clientId = Integer.parseInt(CID);

                    manager.recordSale(propertyId, clientId);
                    refreshAll();
                    txtPID.setText("");
                    txtCID.setText("");
                    JOptionPane.showMessageDialog(this, "Sale recorded, property marked as Sold and saved to database!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Property ID and Client ID must be whole numbers", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter the Property ID and Client ID (or select a row for each)", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnCancel) {
            if (!manager.isAdmin()) return;
            int selectedRow = transTable.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Cancel the selected sale? The property will become Available again.", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        int id = Integer.parseInt(transModel.getValueAt(selectedRow, 0).toString());
                        if (manager.cancelSale(id)) {
                            refreshAll();
                            JOptionPane.showMessageDialog(this, "Sale cancelled and property is Available again");
                        } else {
                            refreshAll();
                            JOptionPane.showMessageDialog(this, "Transaction no longer exists.", "Cancel Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a transaction from the history first", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnRefresh) {
            refreshAll();
        }
    }
}

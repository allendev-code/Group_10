
package com.my.rem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PropertyFrame extends JFrame implements ActionListener {
    private final RealEstateManager manager;
    private DefaultTableModel propertyModel;
    private JTable propertyTable;
    private JScrollPane propertyScroll;
    private JTextField txtPID, txtLocation, txtPrice;
    private JComboBox<String> cmbStatus;
    private JLabel lbHeader, lbPID, lbLocation, lbPrice, lbStatus, lbHint1, lbHint2;
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnRefresh;

    PropertyFrame(RealEstateManager manager) {
        this.manager = manager;
        String[] columns = {"Property ID", "Location", "Price (PHP)", "Status"};
        propertyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        setTitle(manager.isAdmin() ? "Manage Properties [Admin]" : "View Properties [Agent - View & Search]");
        setSize(950, 430);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        lbHeader = new JLabel("Property Information");
        lbHeader.setBounds(10, 5, 250, 30);
        add(lbHeader);

        lbPID = new JLabel("Property ID:");
        lbPID.setBounds(10, 45, 100, 30);
        add(lbPID);

        txtPID = new JTextField();
        txtPID.setBounds(110, 45, 270, 30);
        add(txtPID);

        lbLocation = new JLabel("Location:");
        lbLocation.setBounds(10, 85, 100, 30);
        add(lbLocation);

        txtLocation = new JTextField();
        txtLocation.setBounds(110, 85, 270, 30);
        add(txtLocation);

        lbPrice = new JLabel("Price:");
        lbPrice.setBounds(10, 125, 100, 30);
        add(lbPrice);

        txtPrice = new JTextField();
        txtPrice.setBounds(110, 125, 270, 30);
        add(txtPrice);

        lbStatus = new JLabel("Status:");
        lbStatus.setBounds(10, 165, 100, 30);
        add(lbStatus);

        cmbStatus = new JComboBox<>(new String[]{"Available", "Reserved"});
        cmbStatus.setBounds(110, 165, 270, 30);
        add(cmbStatus);

        btnAdd = new JButton("ADD");
        btnAdd.setBounds(10, 215, 85, 30);
        add(btnAdd);

        btnUpdate = new JButton("UPDATE");
        btnUpdate.setBounds(105, 215, 85, 30);
        add(btnUpdate);

        btnDelete = new JButton("DELETE");
        btnDelete.setBounds(200, 215, 85, 30);
        add(btnDelete);

        btnSearch = new JButton("SEARCH");
        btnSearch.setBounds(295, 215, 85, 30);
        add(btnSearch);

        btnRefresh = new JButton("Refresh / Show All");
        btnRefresh.setBounds(10, 260, 370, 30);
        add(btnRefresh);

        lbHint1 = new JLabel("Search by Property ID, Location (starts with) or Price.");
        lbHint1.setBounds(10, 305, 380, 20);
        add(lbHint1);

        lbHint2 = new JLabel("Update: select a row, then type the new Location/Price.");
        lbHint2.setBounds(10, 328, 380, 20);
        add(lbHint2);

        propertyTable = new JTable(propertyModel);
        propertyScroll = new JScrollPane(propertyTable);
        propertyScroll.setBounds(400, 10, 530, 370);
        add(propertyScroll);

        btnAdd.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);
        btnSearch.addActionListener(this);
        btnRefresh.addActionListener(this);

        if (!manager.isAdmin()) {
            btnAdd.setEnabled(false);
            btnUpdate.setEnabled(false);
            btnDelete.setEnabled(false);
        }

        refreshAll();
    }

    private void refreshAll() {
        String error = manager.loadDataFromDatabase();
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        }
        showProperties(manager.getProperties());
    }

    private void showProperties(ArrayList<Property> list) {
        propertyModel.setRowCount(0);
        for (Property p : list) {
            propertyModel.addRow(new Object[]{p.getPropertyId(), p.getLocation(), String.format("%,.2f", p.getPrice()), p.getStatus()});
        }
    }

    private void clearFields() {
        txtPID.setText("");
        txtLocation.setText("");
        txtPrice.setText("");
        cmbStatus.setSelectedIndex(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            if (!manager.isAdmin()) return;
            String PID = txtPID.getText().trim();
            String Location = txtLocation.getText().trim();
            String Price = txtPrice.getText().trim();

            if (!PID.isEmpty() && !Location.isEmpty() && !Price.isEmpty()) {
                try {
                    int id = Integer.parseInt(PID);
                    double price = Double.parseDouble(Price);
                    manager.addProperty(new Property(id, Location, price, cmbStatus.getSelectedItem().toString()));
                    showProperties(manager.getProperties());
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Property successfully added and saved to database!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Property ID must be a whole number and Price must be a number", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in the Property ID, Location and Price first", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnUpdate) {
            if (!manager.isAdmin()) return;
            int rowSelected = propertyTable.getSelectedRow();
            if (rowSelected != -1) {
                String Location = txtLocation.getText().trim();
                String Price = txtPrice.getText().trim();

                if (!Location.isEmpty() && !Price.isEmpty()) {
                    try {
                        int id = Integer.parseInt(propertyModel.getValueAt(rowSelected, 0).toString());
                        double price = Double.parseDouble(Price);
                        if (manager.updateProperty(id, Location, price, cmbStatus.getSelectedItem().toString())) {
                            showProperties(manager.getProperties());
                            clearFields();
                            JOptionPane.showMessageDialog(this, "Property info has been updated in the database");
                        } else {
                            showProperties(manager.getProperties());
                            JOptionPane.showMessageDialog(this, "Property no longer exists (it may have been deleted by another user).", "Update Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Price must be a number", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter the new Location and Price first", "ERROR Message", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a property first before updating", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnDelete) {
            if (!manager.isAdmin()) return;
            int indexSelected = propertyTable.getSelectedRow();
            if (indexSelected != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete the selected property?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        int id = Integer.parseInt(propertyModel.getValueAt(indexSelected, 0).toString());
                        if (manager.deleteProperty(id)) {
                            showProperties(manager.getProperties());
                            JOptionPane.showMessageDialog(this, "Property has been deleted from the database");
                        } else {
                            showProperties(manager.getProperties());
                            JOptionPane.showMessageDialog(this, "Property no longer exists (it may have been deleted by another user).", "Delete Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a property first before deleting", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnSearch) {
            String PID = txtPID.getText().trim();
            String Location = txtLocation.getText().trim();
            String Price = txtPrice.getText().trim();

            if (!PID.isEmpty() || !Location.isEmpty() || !Price.isEmpty()) {
                try {
                    manager.loadDataFromDatabase();
                    ArrayList<Property> found = new ArrayList<>();
                    if (!PID.isEmpty()) {
                        Property p = manager.searchById(Integer.parseInt(PID));
                        if (p != null) found.add(p);
                    } else if (!Location.isEmpty()) {
                        found = manager.searchByLocation(Location);
                    } else if (!Price.isEmpty()) {
                        found = manager.searchByPrice(Double.parseDouble(Price));
                    }

                    if (!found.isEmpty()) {
                        showProperties(found);
                        JOptionPane.showMessageDialog(this, found.size() + " match(es) found. Click Refresh / Show All to see every property.");
                    } else {
                        JOptionPane.showMessageDialog(this, "No matching property found!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Property ID must be a whole number and Price must be a number", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Enter a Property ID, Location, or Price to search", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnRefresh) {
            refreshAll();
        }
    }
}

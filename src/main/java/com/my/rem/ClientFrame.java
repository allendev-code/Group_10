
package com.my.rem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

    public class ClientFrame extends JFrame implements ActionListener {
    private final RealEstateManager manager;
    private DefaultTableModel clientModel;
    private JTable clientTable;
    private JScrollPane clientScroll;
    private JTextField txtCID, txtName, txtContact;
    private JLabel lbHeader, lbCID, lbName, lbContact, lbHint1, lbHint2;
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnRefresh;

    ClientFrame(RealEstateManager manager) {
        this.manager = manager;
        String[] columns = {"Client ID", "Name", "Contact Info", "Properties Owned (IDs)"};
        clientModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        setTitle(manager.isAdmin() ? "Manage Clients [Admin]" : "View Clients [Agent - View & Search]");
        setSize(950, 430);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        lbHeader = new JLabel("Client Information");
        lbHeader.setBounds(10, 5, 250, 30);
        add(lbHeader);

        lbCID = new JLabel("Client ID:");
        lbCID.setBounds(10, 45, 100, 30);
        add(lbCID);

        txtCID = new JTextField();
        txtCID.setBounds(110, 45, 270, 30);
        add(txtCID);

        lbName = new JLabel("Name:");
        lbName.setBounds(10, 85, 100, 30);
        add(lbName);

        txtName = new JTextField();
        txtName.setBounds(110, 85, 270, 30);
        add(txtName);

        lbContact = new JLabel("Contact Info:");
        lbContact.setBounds(10, 125, 100, 30);
        add(lbContact);

        txtContact = new JTextField();
        txtContact.setBounds(110, 125, 270, 30);
        add(txtContact);

        btnAdd = new JButton("ADD");
        btnAdd.setBounds(10, 180, 85, 30);
        add(btnAdd);

        btnUpdate = new JButton("UPDATE");
        btnUpdate.setBounds(105, 180, 85, 30);
        add(btnUpdate);

        btnDelete = new JButton("DELETE");
        btnDelete.setBounds(200, 180, 85, 30);
        add(btnDelete);

        btnSearch = new JButton("SEARCH");
        btnSearch.setBounds(295, 180, 85, 30);
        add(btnSearch);

        btnRefresh = new JButton("Refresh / Show All");
        btnRefresh.setBounds(10, 225, 370, 30);
        add(btnRefresh);

        lbHint1 = new JLabel("Search by Client ID (hash map lookup) or Name.");
        lbHint1.setBounds(10, 270, 380, 20);
        add(lbHint1);

        lbHint2 = new JLabel("Update: select a row, then type the new Name/Contact.");
        lbHint2.setBounds(10, 293, 380, 20);
        add(lbHint2);

        clientTable = new JTable(clientModel);
        clientScroll = new JScrollPane(clientTable);
        clientScroll.setBounds(400, 10, 530, 370);
        add(clientScroll);

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
        showClients(manager.getClients());
    }

    private void showClients(Collection<Client> list) {
        clientModel.setRowCount(0);
        for (Client c : list) {
            clientModel.addRow(new Object[]{c.getClientId(), c.getName(), c.getContactInfo(), c.getOwnedAsText()});
        }
    }

    private void clearFields() {
        txtCID.setText("");
        txtName.setText("");
        txtContact.setText("");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            if (!manager.isAdmin()) return;
            String CID = txtCID.getText().trim();
            String Name = txtName.getText().trim();
            String Contact = txtContact.getText().trim();

            if (!CID.isEmpty() && !Name.isEmpty() && !Contact.isEmpty()) {
                try {
                    int id = Integer.parseInt(CID);
                    manager.addClient(new Client(id, Name, Contact));
                    showClients(manager.getClients());
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Client successfully added and saved to database!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Client ID must be a whole number", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all client details first", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnUpdate) {
            if (!manager.isAdmin()) return;
            int rowSelected = clientTable.getSelectedRow();
            if (rowSelected != -1) {
                String Name = txtName.getText().trim();
                String Contact = txtContact.getText().trim();

                if (!Name.isEmpty() && !Contact.isEmpty()) {
                    try {
                        int id = Integer.parseInt(clientModel.getValueAt(rowSelected, 0).toString());
                        if (manager.updateClient(id, Name, Contact)) {
                            showClients(manager.getClients());
                            clearFields();
                            JOptionPane.showMessageDialog(this, "Client info has been updated in the database");
                        } else {
                            showClients(manager.getClients());
                            JOptionPane.showMessageDialog(this, "Client no longer exists.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter the new Name and Contact Info first", "ERROR Message", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a client first before updating", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnDelete) {
            if (!manager.isAdmin()) return;
            int indexSelected = clientTable.getSelectedRow();
            if (indexSelected != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete the selected client?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        int id = Integer.parseInt(clientModel.getValueAt(indexSelected, 0).toString());
                        if (manager.deleteClient(id)) {
                            showClients(manager.getClients());
                            JOptionPane.showMessageDialog(this, "Client has been deleted from the database");
                        } else {
                            showClients(manager.getClients());
                            JOptionPane.showMessageDialog(this, "Client no longer exists.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a client first before deleting", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnSearch) {
            String CID = txtCID.getText().trim();
            String Name = txtName.getText().trim();

            if (!CID.isEmpty() || !Name.isEmpty()) {
                try {
                    manager.loadDataFromDatabase();
                    ArrayList<Client> found = new ArrayList<>();
                    if (!CID.isEmpty()) {
                        Client c = manager.getClient(Integer.parseInt(CID));
                        if (c != null) found.add(c);
                    } else {
                        found = manager.searchClientsByName(Name);
                    }

                    if (!found.isEmpty()) {
                        showClients(found);
                        JOptionPane.showMessageDialog(this, found.size() + " match(es) found. Click Refresh / Show All to see every client.");
                    } else {
                        JOptionPane.showMessageDialog(this, "No matching client found!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Client ID must be a whole number", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Enter a Client ID or Name to search", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnRefresh) {
            refreshAll();
        }
    }
}

package RealEstate;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.swing.*;

public class PropertyFrame extends JFrame implements ActionListener {

    private String url = "jdbc:mysql://localhost:3306/realestate_db";
    private String dbUser = "root";
    private String dbPass = "";

    private LinkedList<Property> propertyList = new LinkedList<>();  //all properties from the database
    private LinkedList<Property> shownList = new LinkedList<>();     //what is shown in the JList
    private DefaultListModel<String> listModel;
    private JList<String> list;
    private JScrollPane scrollPane;

    private JLabel lblLocation, lblPrice, lblStatus, lblSearchLoc, lblMin, lblMax;
    private JTextField txtLocation, txtPrice, txtSearchLoc, txtMin, txtMax;
    private JComboBox<String> cmbStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnLoad, btnShowAll, btnSearchLoc, btnSearchPrice;

    public PropertyFrame() {
        listModel = new DefaultListModel<>();

        setSize(950, 600);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Input fields
        lblLocation = new JLabel("Location:");
        lblLocation.setBounds(20, 20, 80, 25);
        add(lblLocation);

        txtLocation = new JTextField();
        txtLocation.setBounds(105, 20, 200, 28);
        add(txtLocation);

        lblPrice = new JLabel("Price:");
        lblPrice.setBounds(320, 20, 50, 25);
        add(lblPrice);

        txtPrice = new JTextField();
        txtPrice.setBounds(375, 20, 120, 28);
        add(txtPrice);

        lblStatus = new JLabel("Status:");
        lblStatus.setBounds(510, 20, 55, 25);
        add(lblStatus);

        cmbStatus = new JComboBox<>(new String[]{"Available", "Reserved"});
        cmbStatus.setBounds(570, 20, 110, 28);
        add(cmbStatus);

        //Buttons
        btnAdd = new JButton("Add");
        btnAdd.setBounds(20, 60, 90, 30);
        add(btnAdd);

        btnUpdate = new JButton("Update");
        btnUpdate.setBounds(120, 60, 90, 30);
        add(btnUpdate);

        btnDelete = new JButton("Delete");
        btnDelete.setBounds(220, 60, 90, 30);
        add(btnDelete);

        btnClear = new JButton("Clear");
        btnClear.setBounds(320, 60, 90, 30);
        add(btnClear);

        btnLoad = new JButton("Load Selected");
        btnLoad.setBounds(420, 60, 130, 30);
        add(btnLoad);

        btnShowAll = new JButton("Show All");
        btnShowAll.setBounds(560, 60, 100, 30);
        add(btnShowAll);

        //Search
        lblSearchLoc = new JLabel("Location starts with:");
        lblSearchLoc.setBounds(20, 105, 130, 25);
        add(lblSearchLoc);

        txtSearchLoc = new JTextField();
        txtSearchLoc.setBounds(155, 105, 150, 28);
        add(txtSearchLoc);

        btnSearchLoc = new JButton("Search Location");
        btnSearchLoc.setBounds(310, 105, 135, 28);
        add(btnSearchLoc);

        lblMin = new JLabel("Price Min:");
        lblMin.setBounds(465, 105, 65, 25);
        add(lblMin);

        txtMin = new JTextField();
        txtMin.setBounds(530, 105, 80, 28);
        add(txtMin);

        lblMax = new JLabel("Max:");
        lblMax.setBounds(620, 105, 35, 25);
        add(lblMax);

        txtMax = new JTextField();
        txtMax.setBounds(655, 105, 80, 28);
        add(txtMax);

        btnSearchPrice = new JButton("Search Price");
        btnSearchPrice.setBounds(745, 105, 130, 28);
        add(btnSearchPrice);

        //JList
        list = new JList<>(listModel);
        list.setFont(new Font("Monospaced", Font.PLAIN, 13));
        scrollPane = new JScrollPane(list);
        scrollPane.setBounds(20, 150, 900, 395);
        add(scrollPane);

        //Add to ActionListener
        btnAdd.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);
        btnClear.addActionListener(this);
        btnLoad.addActionListener(this);
        btnShowAll.addActionListener(this);
        btnSearchLoc.addActionListener(this);
        btnSearchPrice.addActionListener(this);

        loadProperties();
        showList(propertyList);
    }

    //Get all properties from the database
    private void loadProperties() {
        propertyList = new LinkedList<>();
        try {
            Connection con = DriverManager.getConnection(url, dbUser, dbPass);
            PreparedStatement st = con.prepareStatement("SELECT property_id, location, price, status, owner_id FROM properties ORDER BY property_id");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                propertyList.add(new Property(rs.getInt("property_id"), rs.getString("location"),
                        rs.getDouble("price"), rs.getString("status"), rs.getInt("owner_id")));
            }
            con.close();
        } catch (SQLException sqlException) {
            JOptionPane.showMessageDialog(this, "Database error: " + sqlException.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            sqlException.printStackTrace();
        }
    }

    //Display a LinkedList in the JList
    private void showList(LinkedList<Property> items) {
        shownList = items;
        listModel.clear();
        for (Property p : items) {
            String owner = "-";
            if (p.getOwnerId() != 0) {
                owner = String.valueOf(p.getOwnerId());
            }
            listModel.addElement(String.format("ID: %-4d | %-25s | PHP %,15.2f | %-9s | Owner: %s",
                    p.getPropertyId(), p.getLocation(), p.getPrice(), p.getStatus(), owner));
        }
    }

    private void clearFields() {
        txtLocation.setText("");
        txtPrice.setText("");
        cmbStatus.setSelectedIndex(0);
        list.clearSelection();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            String location = txtLocation.getText().trim();
            if (location.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a location", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double price;
            try {
                price = Double.parseDouble(txtPrice.getText().trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price must be a valid number", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than zero", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Connection con = DriverManager.getConnection(url, dbUser, dbPass);
                PreparedStatement st = con.prepareStatement("INSERT INTO properties (location, price, status) VALUES (?,?,?)");
                st.setString(1, location);
                st.setDouble(2, price);
                st.setString(3, (String) cmbStatus.getSelectedItem());
                st.executeUpdate();
                con.close();
                JOptionPane.showMessageDialog(this, "Property has been successfully added");
                clearFields();
                loadProperties();
                showList(propertyList);
            } catch (SQLException sqlException) {
                JOptionPane.showMessageDialog(this, "Database error: " + sqlException.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                sqlException.printStackTrace();
            }

        } else if (e.getSource() == btnUpdate) {
            int index = list.getSelectedIndex();
            if (index == -1) {
                JOptionPane.showMessageDialog(this, "Please select a property first", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Property selected = shownList.get(index);
            String location = txtLocation.getText().trim();
            if (location.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a location", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double price;
            try {
                price = Double.parseDouble(txtPrice.getText().trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price must be a valid number", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than zero", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            //Sold status can only be changed through a transaction
            String status = (String) cmbStatus.getSelectedItem();
            if (selected.getStatus().equals("Sold")) {
                status = "Sold";
            }
            try {
                Connection con = DriverManager.getConnection(url, dbUser, dbPass);
                PreparedStatement st = con.prepareStatement("UPDATE properties SET location=?, price=?, status=? WHERE property_id=?");
                st.setString(1, location);
                st.setDouble(2, price);
                st.setString(3, status);
                st.setInt(4, selected.getPropertyId());
                st.executeUpdate();
                con.close();
                JOptionPane.showMessageDialog(this, "Property has been successfully updated");
                clearFields();
                loadProperties();
                showList(propertyList);
            } catch (SQLException sqlException) {
                JOptionPane.showMessageDialog(this, "Database error: " + sqlException.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                sqlException.printStackTrace();
            }

        } else if (e.getSource() == btnDelete) {
            int index = list.getSelectedIndex();
            if (index == -1) {
                JOptionPane.showMessageDialog(this, "Please select a property first", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Property selected = shownList.get(index);
            if (selected.getStatus().equals("Sold")) {
                JOptionPane.showMessageDialog(this, "A sold property cannot be deleted. Delete its transaction first.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int a = JOptionPane.showConfirmDialog(this, "Delete property #" + selected.getPropertyId() + "?");
            if (a == JOptionPane.YES_OPTION) {
                try {
                    Connection con = DriverManager.getConnection(url, dbUser, dbPass);
                    PreparedStatement st = con.prepareStatement("DELETE FROM properties WHERE property_id=?");
                    st.setInt(1, selected.getPropertyId());
                    st.executeUpdate();
                    con.close();
                    clearFields();
                    loadProperties();
                    showList(propertyList);
                } catch (SQLException sqlException) {
                    JOptionPane.showMessageDialog(this, "Database error: " + sqlException.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    sqlException.printStackTrace();
                }
            }

        } else if (e.getSource() == btnClear) {
            clearFields();

        } else if (e.getSource() == btnLoad) {
            int index = list.getSelectedIndex();
            if (index != -1) {
                Property selected = shownList.get(index);
                txtLocation.setText(selected.getLocation());
                txtPrice.setText(String.valueOf(selected.getPrice()));
                if (!selected.getStatus().equals("Sold")) {
                    cmbStatus.setSelectedItem(selected.getStatus());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a property first", "Input Error", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == btnShowAll) {
            loadProperties();
            showList(propertyList);

        } else if (e.getSource() == btnSearchLoc) {
            String key = txtSearchLoc.getText().trim();
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a location to search", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            //Binary search needs a sorted array, so sort by location first
            Property[] arr = propertyList.toArray(new Property[0]);
            Property.insertionSort(arr, "Location");
            LinkedList<Property> result = Property.binarySearchLocation(arr, key);
            showList(result);
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No property found for that location");
            }

        } else if (e.getSource() == btnSearchPrice) {
            double min, max;
            try {
                min = Double.parseDouble(txtMin.getText().trim().replace(",", ""));
                max = Double.parseDouble(txtMax.getText().trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Min and Max price must be valid numbers", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (min > max) {
                JOptionPane.showMessageDialog(this, "Min price cannot be greater than Max price", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            //Binary search needs a sorted array, so sort by price first
            Property[] arr = propertyList.toArray(new Property[0]);
            Property.insertionSort(arr, "Price");
            LinkedList<Property> result = Property.binarySearchPrice(arr, min, max);
            showList(result);
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No property found in that price range");
            }
        }
    }
}

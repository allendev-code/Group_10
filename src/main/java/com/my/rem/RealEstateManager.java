
package com.my.rem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;


public class RealEstateManager {

    public static final int SORT_PRICE_ASC = 1;
    public static final int SORT_PRICE_DESC = 2;
    public static final int SORT_LOCATION = 3;
    public static final int SORT_STATUS = 4;
    public static final int SORT_ID = 5;

    private ArrayList<Property> propertyList;
    private HashMap<Integer, Client> clientMap;
    private ArrayList<Transaction> transactionList;
    private String currentUserRole;
    private String currentUsername;

    public RealEstateManager(String role, String username) {
        this.currentUserRole = role;
        this.currentUsername = username;
        propertyList = new ArrayList<>();
        clientMap = new LinkedHashMap<>(); // a HashMap that keeps the ORDER BY client_id order
        transactionList = new ArrayList<>();
    }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(currentUserRole);
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public ArrayList<Property> getProperties() {
        return propertyList;
    }

    public Collection<Client> getClients() {
        return clientMap.values();
    }

    public ArrayList<Transaction> getTransactions() {
        return transactionList;
    }

    // ---------------------------------------------------------------- LOAD

    public String loadDataFromDatabase() {
        propertyList.clear();
        clientMap.clear();
        transactionList.clear();

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM properties ORDER BY property_id");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    propertyList.add(new Property(rs.getInt("property_id"), rs.getString("location"),
                            rs.getDouble("price"), rs.getString("status")));
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM clients ORDER BY client_id");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("client_id");
                    clientMap.put(id, new Client(id, rs.getString("name"), rs.getString("contact_info")));
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM transactions ORDER BY transaction_id");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("transaction_date");
                    if (date != null && date.length() > 19) date = date.substring(0, 19);
                    Transaction t = new Transaction(rs.getInt("transaction_id"), rs.getInt("property_id"),
                            rs.getInt("client_id"), date, rs.getDouble("amount"), rs.getString("username"));
                    transactionList.add(t);
                    Client owner = clientMap.get(t.getClientId());
                    if (owner != null) owner.addOwnedProperty(t.getPropertyId());
                }
            }
        } catch (SQLException e) {
            return "Database Load Error: " + e.getMessage();
        }
        return null;
    }

    // ---------------------------------------------------------------- PROPERTIES

    private void validateProperty(int id, String location, double price, String status) {
        if (id <= 0) {
            throw new IllegalArgumentException("Property ID must be greater than zero.");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty.");
        }
        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (!"Available".equals(status) && !"Reserved".equals(status)) {
            throw new IllegalArgumentException("Status must be Available or Reserved. 'Sold' is set by recording a sale.");
        }
    }

    public void addProperty(Property p) {
        validateProperty(p.getPropertyId(), p.getLocation(), p.getPrice(), p.getStatus());

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO properties (property_id, location, price, status) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, p.getPropertyId());
            pstmt.setString(2, p.getLocation());
            pstmt.setDouble(3, p.getPrice());
            pstmt.setString(4, p.getStatus());
            pstmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Property ID " + p.getPropertyId() + " already exists.");
        } catch (SQLException e) {
            throw new RuntimeException("Database error during insert: " + e.getMessage());
        }
        loadDataFromDatabase();
    }

    public boolean updateProperty(int id, String location, double price, String status) {
        validateProperty(id, location, price, status);
        int affected = 0;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT status FROM properties WHERE property_id = ?");
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                if ("Sold".equals(rs.getString("status"))) {
                    throw new IllegalArgumentException("Sold properties cannot be modified. Cancel the sale first.");
                }
                String sql = "UPDATE properties SET location = ?, price = ?, status = ? WHERE property_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, location);
                pstmt.setDouble(2, price);
                pstmt.setString(3, status);
                pstmt.setInt(4, id);
                affected = pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database update error: " + e.getMessage());
        }
        loadDataFromDatabase();
        return affected > 0;
    }

    public boolean deleteProperty(int id) {
        int affected;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM transactions WHERE property_id = ?");
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                throw new IllegalArgumentException("Cannot delete this property because it has a sales record.");
            }

            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM properties WHERE property_id = ?");
            pstmt.setInt(1, id);
            affected = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database delete error: " + e.getMessage());
        }
        loadDataFromDatabase();
        return affected > 0;
    }

    public Property searchById(int id) {
        for (Property p : propertyList) {
            if (p.getPropertyId() == id) return p;
        }
        return null;
    }

    // Binary search (lower bound) on a copy sorted by price: returns every property with that exact price.
    public ArrayList<Property> searchByPrice(double price) {
        ArrayList<Property> sorted = mergeSort(new ArrayList<>(propertyList), SORT_PRICE_ASC);
        ArrayList<Property> result = new ArrayList<>();

        int low = 0;
        int high = sorted.size() - 1;
        int first = sorted.size();
        while (low <= high) {
            int mid = (low + high) / 2;
            if (sorted.get(mid).getPrice() >= price) {
                first = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        for (int i = first; i < sorted.size() && Double.compare(sorted.get(i).getPrice(), price) == 0; i++) {
            result.add(sorted.get(i));
        }
        return result;
    }

    // Binary search (lower bound) on a copy sorted by location: returns every property whose location starts with the text.
    public ArrayList<Property> searchByLocation(String text) {
        ArrayList<Property> sorted = mergeSort(new ArrayList<>(propertyList), SORT_LOCATION);
        ArrayList<Property> result = new ArrayList<>();
        String key = text.trim().toLowerCase();

        int low = 0;
        int high = sorted.size() - 1;
        int first = sorted.size();
        while (low <= high) {
            int mid = (low + high) / 2;
            if (sorted.get(mid).getLocation().compareToIgnoreCase(key) >= 0) {
                first = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        for (int i = first; i < sorted.size() && sorted.get(i).getLocation().toLowerCase().startsWith(key); i++) {
            result.add(sorted.get(i));
        }
        return result;
    }

    // ---------------------------------------------------------------- CLIENTS

    private void validateClient(int id, String name, String contact) {
        if (id <= 0) {
            throw new IllegalArgumentException("Client ID must be greater than zero.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Client name cannot be empty.");
        }
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact info cannot be empty.");
        }
    }

    public void addClient(Client c) {
        validateClient(c.getClientId(), c.getName(), c.getContactInfo());

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO clients (client_id, name, contact_info) VALUES (?, ?, ?)");
            pstmt.setInt(1, c.getClientId());
            pstmt.setString(2, c.getName());
            pstmt.setString(3, c.getContactInfo());
            pstmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Client ID " + c.getClientId() + " already exists.");
        } catch (SQLException e) {
            throw new RuntimeException("Database error adding client: " + e.getMessage());
        }
        loadDataFromDatabase();
    }

    public boolean updateClient(int id, String name, String contact) {
        validateClient(id, name, contact);
        int affected;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE clients SET name = ?, contact_info = ? WHERE client_id = ?");
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setInt(3, id);
            affected = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating client: " + e.getMessage());
        }
        loadDataFromDatabase();
        return affected > 0;
    }

    public boolean deleteClient(int id) {
        int affected;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM transactions WHERE client_id = ?");
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                throw new IllegalArgumentException("Cannot delete this client because they have a transaction record.");
            }

            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM clients WHERE client_id = ?");
            pstmt.setInt(1, id);
            affected = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting client: " + e.getMessage());
        }
        loadDataFromDatabase();
        return affected > 0;
    }

    // Hash map lookup: O(1) access by client ID.
    public Client getClient(int id) {
        return clientMap.get(id);
    }

    public ArrayList<Client> searchClientsByName(String text) {
        ArrayList<Client> result = new ArrayList<>();
        String key = text.trim().toLowerCase();
        for (Client c : clientMap.values()) {
            if (c.getName().toLowerCase().contains(key)) result.add(c);
        }
        return result;
    }

    // ---------------------------------------------------------------- TRANSACTIONS

    public void recordSale(int propertyId, int clientId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT price, status FROM properties WHERE property_id = ? FOR UPDATE");
                ps.setInt(1, propertyId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new IllegalArgumentException("Property ID " + propertyId + " was not found.");
                }
                if ("Sold".equals(rs.getString("status"))) {
                    throw new IllegalArgumentException("Property ID " + propertyId + " is already sold.");
                }
                double price = rs.getDouble("price");

                PreparedStatement pc = conn.prepareStatement("SELECT client_id FROM clients WHERE client_id = ?");
                pc.setInt(1, clientId);
                ResultSet rc = pc.executeQuery();
                if (!rc.next()) {
                    throw new IllegalArgumentException("Client ID " + clientId + " was not found.");
                }

                PreparedStatement ins = conn.prepareStatement("INSERT INTO transactions (property_id, client_id, amount, username) VALUES (?, ?, ?, ?)");
                ins.setInt(1, propertyId);
                ins.setInt(2, clientId);
                ins.setDouble(3, price);
                ins.setString(4, currentUsername);
                ins.executeUpdate();

                PreparedStatement upd = conn.prepareStatement("UPDATE properties SET status = 'Sold' WHERE property_id = ?");
                upd.setInt(1, propertyId);
                upd.executeUpdate();
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during transaction: " + e.getMessage());
        }
        loadDataFromDatabase();
    }

    public boolean cancelSale(int transactionId) {
        boolean cancelled = false;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT property_id FROM transactions WHERE transaction_id = ? FOR UPDATE");
                ps.setInt(1, transactionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int propertyId = rs.getInt("property_id");

                    PreparedStatement del = conn.prepareStatement("DELETE FROM transactions WHERE transaction_id = ?");
                    del.setInt(1, transactionId);
                    del.executeUpdate();

                    PreparedStatement upd = conn.prepareStatement("UPDATE properties SET status = 'Available' WHERE property_id = ?");
                    upd.setInt(1, propertyId);
                    upd.executeUpdate();
                    conn.commit();
                    cancelled = true;
                }
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error cancelling sale: " + e.getMessage());
        }
        loadDataFromDatabase();
        return cancelled;
    }

    // ---------------------------------------------------------------- SORTING

    private int compare(Property a, Property b, int mode) {
        switch (mode) {
            case SORT_PRICE_ASC:
                return Double.compare(a.getPrice(), b.getPrice());
            case SORT_PRICE_DESC:
                return Double.compare(b.getPrice(), a.getPrice());
            case SORT_LOCATION:
                return a.getLocation().compareToIgnoreCase(b.getLocation());
            case SORT_STATUS:
                return a.getStatus().compareToIgnoreCase(b.getStatus());
            default:
                return Integer.compare(a.getPropertyId(), b.getPropertyId());
        }
    }

    private ArrayList<Property> mergeSort(ArrayList<Property> list, int mode) {
        if (list.size() <= 1) return list;

        int mid = list.size() / 2;
        ArrayList<Property> left = mergeSort(new ArrayList<>(list.subList(0, mid)), mode);
        ArrayList<Property> right = mergeSort(new ArrayList<>(list.subList(mid, list.size())), mode);

        ArrayList<Property> merged = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            if (compare(left.get(i), right.get(j), mode) <= 0) {
                merged.add(left.get(i++));
            } else {
                merged.add(right.get(j++));
            }
        }
        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }

    // Insertion sort: highest sale amount first.
    private ArrayList<Transaction> insertionSortByAmount(ArrayList<Transaction> source) {
        ArrayList<Transaction> list = new ArrayList<>(source);
        for (int i = 1; i < list.size(); i++) {
            Transaction key = list.get(i);
            int j = i - 1;
            while (j >= 0 && list.get(j).getAmount() < key.getAmount()) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
        return list;
    }

    // ---------------------------------------------------------------- REPORTS

    private String fit(String text, int width) {
        if (text == null) return "";
        return text.length() <= width ? text : text.substring(0, width - 1) + "~";
    }

    private String sortLabel(int mode) {
        switch (mode) {
            case SORT_PRICE_ASC:
                return "Price (Low to High)";
            case SORT_PRICE_DESC:
                return "Price (High to Low)";
            case SORT_LOCATION:
                return "Location (A-Z)";
            case SORT_STATUS:
                return "Status";
            default:
                return "Property ID";
        }
    }

    public String generateSalesReport() {
        if (transactionList.isEmpty()) {
            return "No sales have been recorded yet.";
        }
        ArrayList<Transaction> sorted = insertionSortByAmount(transactionList);
        StringBuilder report = new StringBuilder("=== SALES REPORT (highest sale first) ===\n\n");
        report.append(String.format("%-6s %-9s %-22s %-16s %15s  %-19s %s\n",
                "Trans", "Property", "Location", "Client", "Amount (PHP)", "Date", "By"));

        double total = 0;
        for (Transaction t : sorted) {
            Property p = searchById(t.getPropertyId());
            Client c = clientMap.get(t.getClientId());
            String location = (p == null) ? "N/A" : p.getLocation();
            String clientName = (c == null) ? "N/A" : c.getName();
            report.append(String.format("%-6d %-9d %-22s %-16s %,15.2f  %-19s %s\n",
                    t.getTransactionId(), t.getPropertyId(), fit(location, 22), fit(clientName, 16),
                    t.getAmount(), t.getDate(), t.getRecordedBy()));
            total += t.getAmount();
        }

        report.append("\nTotal Sales: ").append(sorted.size());
        report.append("\nTotal Revenue: PHP ").append(String.format("%,.2f", total));
        report.append("\nAverage Sale: PHP ").append(String.format("%,.2f", total / sorted.size()));
        return report.toString();
    }

    public String generateMarketAnalysis(int sortMode) {
        if (propertyList.isEmpty()) {
            return "No properties to analyze yet. Add properties first.";
        }

        int available = 0;
        int reserved = 0;
        int sold = 0;
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = 0;
        TreeMap<String, double[]> byLocation = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Property p : propertyList) {
            if ("Available".equals(p.getStatus())) {
                available++;
            } else if ("Reserved".equals(p.getStatus())) {
                reserved++;
            } else {
                sold++;
            }
            sum += p.getPrice();
            if (p.getPrice() < min) min = p.getPrice();
            if (p.getPrice() > max) max = p.getPrice();

            String key = p.getLocation().trim();
            double[] stats = byLocation.get(key);
            if (stats == null) {
                stats = new double[2];
                byLocation.put(key, stats);
            }
            stats[0]++;
            stats[1] += p.getPrice();
        }

        int total = propertyList.size();
        StringBuilder report = new StringBuilder("=== MARKET ANALYSIS REPORT ===\n\n");

        report.append("-- Overview --\n");
        report.append("Total Properties : ").append(total).append("\n");
        report.append("Available        : ").append(available).append("\n");
        report.append("Reserved         : ").append(reserved).append("\n");
        report.append("Sold             : ").append(sold).append("\n");
        report.append("Sell-through     : ").append(String.format("%.1f", sold * 100.0 / total)).append("%\n\n");

        report.append("-- Price Statistics (PHP) --\n");
        report.append("Lowest  : ").append(String.format("%,.2f", min)).append("\n");
        report.append("Highest : ").append(String.format("%,.2f", max)).append("\n");
        report.append("Average : ").append(String.format("%,.2f", sum / total)).append("\n\n");

        report.append("-- Average Price per Location --\n");
        for (Map.Entry<String, double[]> entry : byLocation.entrySet()) {
            double[] s = entry.getValue();
            report.append(String.format("%-26s %3d listing(s)   avg PHP %,.2f\n",
                    fit(entry.getKey(), 26), (int) s[0], s[1] / s[0]));
        }

        report.append("\n-- Property Listing sorted by ").append(sortLabel(sortMode)).append(" --\n");
        report.append(String.format("%-6s %-26s %16s  %s\n", "ID", "Location", "Price (PHP)", "Status"));
        ArrayList<Property> sorted = mergeSort(new ArrayList<>(propertyList), sortMode);
        for (Property p : sorted) {
            report.append(String.format("%-6d %-26s %,16.2f  %s\n",
                    p.getPropertyId(), fit(p.getLocation(), 26), p.getPrice(), p.getStatus()));
        }
        return report.toString();
    }
}

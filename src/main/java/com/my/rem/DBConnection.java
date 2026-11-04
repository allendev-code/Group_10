
package com.my.rem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


    public class DBConnection {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String URL = "jdbc:mysql://localhost:3306/realestate_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. Add mysql-connector-j to the project libraries.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
                 Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS realestate_db");
            }

            try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                        + "username VARCHAR(50) PRIMARY KEY, "
                        + "password VARCHAR(100) NOT NULL, "
                        + "role VARCHAR(20) NOT NULL)");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS properties ("
                        + "property_id INT PRIMARY KEY, "
                        + "location VARCHAR(150) NOT NULL, "
                        + "price DOUBLE NOT NULL, "
                        + "status VARCHAR(20) NOT NULL)");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS clients ("
                        + "client_id INT PRIMARY KEY, "
                        + "name VARCHAR(100) NOT NULL, "
                        + "contact_info VARCHAR(150) NOT NULL)");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS transactions ("
                        + "transaction_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "property_id INT NOT NULL, "
                        + "client_id INT NOT NULL, "
                        + "amount DOUBLE NOT NULL, "
                        + "username VARCHAR(50) NOT NULL, "
                        + "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                // The only predefined data: one Admin account. Agents are created through the GUI.
                st.executeUpdate("INSERT IGNORE INTO users (username, password, role) "
                        + "VALUES ('admin', 'admin123', 'Admin')");
            }
            return null;
        } catch (ClassNotFoundException e) {
            return "MySQL JDBC driver not found. Add mysql-connector-j to the project libraries.";
        } catch (SQLException e) {
            return "Database setup failed: " + e.getMessage();
        }
    }
}

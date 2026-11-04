
package com.my.rem;

import javax.swing.JOptionPane;


public class RealEstateManagementMain {
    public static void main(String[] args) {
        String error = DBConnection.initializeDatabase();
        if (error != null) {
            JOptionPane.showMessageDialog(null, error, "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}

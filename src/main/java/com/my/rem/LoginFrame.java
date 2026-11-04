
package com.my.rem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;


public class LoginFrame extends JFrame implements ActionListener {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lbUser, lbPass, lbTitle;

    public LoginFrame() {
        setTitle("Login - Real Estate Management System");
        setSize(350, 250);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        lbTitle = new JLabel("Real Estate System Login");
        lbTitle.setBounds(100, 20, 200, 30);
        add(lbTitle);

        lbUser = new JLabel("Username:");
        lbUser.setBounds(30, 70, 80, 25);
        add(lbUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(110, 70, 180, 25);
        add(txtUsername);

        lbPass = new JLabel("Password:");
        lbPass.setBounds(30, 110, 80, 25);
        add(lbPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(110, 110, 180, 25);
        add(txtPassword);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(120, 160, 100, 30);
        add(btnLogin);

        btnLogin.addActionListener(this);
        txtPassword.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                this.dispose();
                new DashboardFrame(role, user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


package RealEstate;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;

public class LoginFrame extends JFrame implements ActionListener {

    private String url = "jdbc:mysql://localhost:3306/realestate_db";
    private String dbUser = "root";
    private String dbPass = "";

    private JLabel lblTitle, lblUsername, lblPassword;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public static void main(String[] args) {
        LoginFrame app = new LoginFrame();
        app.setTitle("Real Estate Management System - Login");
        app.setVisible(true);
    }

    public LoginFrame() {
        setSize(420, 300);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Title
        lblTitle = new JLabel("Admin Login");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 26));
        lblTitle.setBounds(115, 20, 250, 40);
        add(lblTitle);

        //Username
        lblUsername = new JLabel("Username");
        lblUsername.setBounds(40, 90, 80, 25);
        add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setBounds(130, 90, 230, 28);
        add(txtUsername);

        //Password
        lblPassword = new JLabel("Password");
        lblPassword.setBounds(40, 135, 80, 25);
        add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(130, 135, 230, 28);
        add(txtPassword);

        //Button
        btnLogin = new JButton("Login");
        btnLogin.setBounds(130, 190, 120, 35);
        add(btnLogin);

        //Add to ActionListener
        btnLogin.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            String userName = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());

            if (userName.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Connection con = DriverManager.getConnection(url, dbUser, dbPass);
                PreparedStatement st = con.prepareStatement("SELECT username, password FROM users WHERE username=? AND password=?");
                st.setString(1, userName);
                st.setString(2, password);
                ResultSet rs = st.executeQuery();

                if (rs.next()) {
                    dispose();
                    HomeFrame home = new HomeFrame(userName);
                    home.setTitle("Welcome");
                    home.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong Username & Password", "Login Error", JOptionPane.ERROR_MESSAGE);
                    txtPassword.setText("");
                }
                con.close();
            } catch (SQLException sqlException) {
                JOptionPane.showMessageDialog(this, "Cannot connect to the database. Check MySQL, schema.sql and the connector jar.", "Database Error", JOptionPane.ERROR_MESSAGE);
                sqlException.printStackTrace();
            }
        }
    }
}

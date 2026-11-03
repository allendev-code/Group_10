
package com.my.rem;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class ReportFrame extends JFrame implements ActionListener {
    private final RealEstateManager manager;
    private JTextArea txtReport;
    private JScrollPane reportScroll;
    private JButton btnSales, btnMarket;
    private JLabel lbSort;
    private JComboBox<String> cmbSort;

    ReportFrame(RealEstateManager manager) {
        this.manager = manager;
        setTitle("Reports");
        setSize(760, 480);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        btnSales = new JButton("Sales Report");
        btnSales.setBounds(10, 10, 140, 30);
        add(btnSales);

        btnMarket = new JButton("Market Analysis");
        btnMarket.setBounds(160, 10, 150, 30);
        add(btnMarket);

        lbSort = new JLabel("Sort listing by:");
        lbSort.setBounds(330, 10, 100, 30);
        add(lbSort);

        cmbSort = new JComboBox<>(new String[]{"Price (Low to High)", "Price (High to Low)", "Location (A-Z)", "Status", "Property ID"});
        cmbSort.setBounds(430, 10, 200, 30);
        add(cmbSort);

        txtReport = new JTextArea();
        txtReport.setEditable(false);
        txtReport.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportScroll = new JScrollPane(txtReport);
        reportScroll.setBounds(10, 50, 725, 380);
        add(reportScroll);

        btnSales.addActionListener(this);
        btnMarket.addActionListener(this);

        if (!manager.isAdmin()) {
            btnMarket.setEnabled(false);
            cmbSort.setEnabled(false);
        }

        String error = manager.loadDataFromDatabase();
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        }
        txtReport.setText(manager.generateSalesReport());
        txtReport.setCaretPosition(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String error = manager.loadDataFromDatabase();
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (e.getSource() == btnSales) {
            txtReport.setText(manager.generateSalesReport());
        } else if (e.getSource() == btnMarket) {
            if (!manager.isAdmin()) return;
            txtReport.setText(manager.generateMarketAnalysis(cmbSort.getSelectedIndex() + 1));
        }
        txtReport.setCaretPosition(0);
    }
}

package edu.univ.erp.ui.common;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private JPanel contentPanel;
    private JLabel roleLabel;

    public MainWindow(String role) {
        setTitle("University ERP System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        roleLabel = new JLabel("Logged in as: " + role, SwingConstants.RIGHT);
        add(roleLabel, BorderLayout.NORTH);

        contentPanel = new JPanel(new CardLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setContent(JPanel panel) {
        getContentPane().removeAll();
        add(roleLabel, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}


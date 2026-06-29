package edu.univ.erp.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoadingDialog extends JDialog {

    public LoadingDialog(Window parent, String message) {
        super(parent != null ? parent : null, "Processing", ModalityType.APPLICATION_MODAL);

        setUndecorated(true);

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(20, 30, 20, 30)
        ));

        // 1. The Spinner (Indeterminate Progress Bar)
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // Makes it bounce back and forth
        progressBar.setPreferredSize(new Dimension(200, 6)); // Thin and modern

        // 2. The Message Label
        JLabel lblMessage = new JLabel(message, SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMessage.setForeground(new Color(60, 60, 60));

        content.add(lblMessage, BorderLayout.CENTER);
        content.add(progressBar, BorderLayout.SOUTH);

        add(content);
        pack();
        setLocationRelativeTo(parent); // Center on the parent window
    }
}
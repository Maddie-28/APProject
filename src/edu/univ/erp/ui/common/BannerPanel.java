package edu.univ.erp.ui.common;

import javax.swing.*;
import java.awt.*;

public class BannerPanel extends JPanel {
    private JLabel banner;

    public BannerPanel() {
        setLayout(new BorderLayout());
        banner = new JLabel("", SwingConstants.CENTER);
        banner.setOpaque(true);
        banner.setForeground(Color.WHITE);
        banner.setBackground(Color.RED);
        banner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(banner, BorderLayout.CENTER);
        setVisible(false);
    }

    public void showMaintenanceBanner(boolean on) {
        setVisible(on);
        banner.setText(on ? "⚠ Maintenance Mode: Read-only access for Students and Instructors" : "");
    }

}


package edu.univ.erp.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIUtils {
    // 1. Color Palette (Modern "University Blue" Theme)
    public static final Color COLOR_PRIMARY = new Color(0, 90, 170);   // Darker Blue
    public static final Color COLOR_ACCENT = new Color(14, 165, 233);  // Light Blue
    public static final Color COLOR_BACKGROUND = new Color(245, 247, 250); // Very light gray-blue
    public static final Color COLOR_WHITE = Color.WHITE;
    public static final Color COLOR_TEXT_HEADER = new Color(40, 44, 52);
    public static final Color COLOR_OUTLINE = new Color(220, 220, 220);

    // 2. Fonts (Segoe UI is standard, but we define sizes here)
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_GENERAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    // 3. Common Components Factory

    // Helper to create a consistent Header Label
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADER);
        label.setForeground(COLOR_TEXT_HEADER);
        return label;
    }

    // Helper to create a consistent Blue Button
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(COLOR_WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20)); // Padding inside button
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Helper to create a white "Card" panel with a border
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_OUTLINE, 1),
                new EmptyBorder(20, 20, 20, 20) // Padding inside the card
        ));
        return panel;
    }
}
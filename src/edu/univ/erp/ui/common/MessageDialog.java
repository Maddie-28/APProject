package edu.univ.erp.ui.common;

import javax.swing.*;
import java.awt.Component;

public class MessageDialog {

    // --- INFO MESSAGES ---
    public static void showInfo(String message) {
        showInfo(null, message);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- ERROR MESSAGES ---
    public static void showError(String message) {
        showError(null, message);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // --- WARNING MESSAGES ---
    public static void showWarning(String message) {
        showWarning(null, message);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
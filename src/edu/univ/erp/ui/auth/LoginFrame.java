package edu.univ.erp.ui.auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.student.StudentDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.common.MessageDialog;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("University ERP Portal - Login");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {}

        initUI();
    }

    // ---------- Placeholder Text Field ----------
    class PlaceholderTextField extends JTextField {
        private final String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                g.setColor(new Color(140, 140, 140));
                g.drawString(placeholder, 10, getHeight() / 2 + 5);
            }
        }
    }

    // ---------- Placeholder Password Field ----------
    class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;

        public PlaceholderPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && !isFocusOwner()) {
                g.setColor(new Color(140, 140, 140));
                g.drawString(placeholder, 10, getHeight() / 2 + 5);
            }
        }
    }

    // ---------- Rounded Input Wrapper (UPDATED with solid borders) ----------
    private JPanel roundedField(JComponent field) {
        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 10;

                // Fill
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

                // Solid border
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(120, 120, 120)); // darker & sharp
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(6, 12, 6, 12)); // perfect spacing
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    // ---------- MAIN UI ----------
    private void initUI() {

        // Background
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(Color.lightGray);
        setContentPane(bg);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        bg.add(center);

        // Floating Card Container
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 10;

                // Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, arc, arc);

                // Main area
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

                // Border
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(160, 160, 160));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setPreferredSize(new Dimension(430, 450));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(32, 34, 32, 34));

        center.add(card);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 10, 14, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // ---------- Title ----------
        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(30, 30, 30));
        card.add(title, gbc);

        // ---------- Username ----------
        gbc.gridy++;
        gbc.gridwidth = 1;

        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userLbl.setForeground(new Color(60, 60, 60));
        card.add(userLbl, gbc);

        gbc.gridx = 1;
        usernameField = new PlaceholderTextField("eg. student1");
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setPreferredSize(new Dimension(260, 42));
        card.add(roundedField(usernameField), gbc);

        // ---------- Password ----------
        gbc.gridx = 0;
        gbc.gridy++;

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passLbl.setForeground(new Color(60, 60, 60));
        card.add(passLbl, gbc);

        gbc.gridx = 1;
        passwordField = new PlaceholderPasswordField("eg. pass123");
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setPreferredSize(new Dimension(260, 42));
        card.add(roundedField(passwordField), gbc);

        // ---------- Role ----------
        gbc.gridx = 0;
        gbc.gridy++;

        JLabel roleLbl = new JLabel("Select Role:");
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        roleLbl.setForeground(new Color(60, 60, 60));
        card.add(roleLbl, gbc);

        gbc.gridx = 1;
        String[] roles = {"Student", "Instructor", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleComboBox.setOpaque(false);
        roleComboBox.setBorder(null);
        roleComboBox.setPreferredSize(new Dimension(260, 42));
        card.add(roundedField(roleComboBox), gbc);

        // ---------- Buttons ----------
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        // Button sizes: both buttons identical width & height
        Dimension btnSize = new Dimension(140, 42);
        Font btnFont = new Font("Segoe UI", Font.BOLD, 14);

        // Clear Button
        JButton clearButton = new JButton("Clear");
        clearButton.setPreferredSize(btnSize);
        clearButton.setMinimumSize(btnSize);
        clearButton.setFont(btnFont);
        clearButton.setBackground(new Color(220, 30, 30));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // consistent padding/margin
        clearButton.setMargin(new Insets(6, 14, 6, 14));
        clearButton.setBorder(BorderFactory.createLineBorder(new Color(150,150,150), 1));

        // Login Button (same size as Clear)
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(btnSize);
        loginButton.setMinimumSize(btnSize);
        loginButton.setFont(btnFont);
        loginButton.setBackground(new Color(30, 120, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setMargin(new Insets(6, 14, 6, 14));
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(120,140,180), 1));

        GridBagConstraints bb = new GridBagConstraints();
        bb.insets = new Insets(0, 12, 0, 12);

        buttonPanel.add(clearButton, bb);
        bb.gridx = 1;
        buttonPanel.add(loginButton, bb);

        card.add(buttonPanel, gbc);

        // ---------- Status ----------
        gbc.gridy++;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(statusLabel, gbc);

        // ---------- Button Actions ----------
        loginButton.addActionListener(e -> handleLogin());

        clearButton.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            statusLabel.setText(" ");
        });
    }

    // ---------- Login handling ----------
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedRole = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        // --- Use LoadingDialog for Login Process ---
        edu.univ.erp.ui.common.LoadingDialog loader =
                new edu.univ.erp.ui.common.LoadingDialog(this, "Verifying credentials...");

        // Background Worker to prevent freezing
        SwingWorker<JFrame, Void> worker = new SwingWorker<>() {
            private String errorMessage = null; // Store error to show later

            @Override
            protected JFrame doInBackground() {
                try {
                    AuthService authService = new AuthService();
                    // This creates the network connection (might take time)
                    UserSession session = authService.login(username, password);

                    if (session != null) {
                        String actualRole = session.role;
                        if (!actualRole.equalsIgnoreCase(selectedRole)) {
                            errorMessage = "Role mismatch. Please select " + actualRole + ".";
                            return null;
                        }

                        // Load the heavy dashboard in background too
                        if (actualRole.equalsIgnoreCase("admin")) {
                            return new AdminDashboard(session.userID);
                        } else if (actualRole.equalsIgnoreCase("student")) {
                            return new StudentDashboard(session);
                        } else {
                            return new InstructorDashboard(session);
                        }
                    }
                } catch (Exception e) {
                    // Capture the "Locked" or "Wrong Password" message
                    errorMessage = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                loader.dispose(); // Close "Verifying..." dialog

                try {
                    JFrame dashboard = get();
                    if (dashboard != null) {
                        dashboard.setVisible(true);
                        LoginFrame.this.dispose();
                    } else {
                        // Show the specific error (e.g., "Account locked! Try again in 5 minutes")
                        if (errorMessage != null) {
                            MessageDialog.showError(LoginFrame.this, errorMessage);
                            statusLabel.setText(errorMessage);
                        } else {
                            statusLabel.setText("Login failed.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
        loader.setVisible(true);
    }
    // ---------- MAIN ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

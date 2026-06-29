package edu.univ.erp.ui.common;

import edu.univ.erp.auth.AuthService;
import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private JPasswordField txtOldPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;
    private int userId;

    public ChangePasswordDialog(Frame parent, int userId) {
        super(parent, "Change Password", true);
        this.userId = userId;
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initUI();
    }

    public ChangePasswordDialog(Dialog parent, int userId) {
        super(parent, "Change Password", true);
        this.userId = userId;
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtOldPass = new JPasswordField(15);
        txtNewPass = new JPasswordField(15);
        txtConfirmPass = new JPasswordField(15);

        addRow(form, gbc, 0, "Old Password:", txtOldPass);
        addRow(form, gbc, 1, "New Password:", txtNewPass);
        addRow(form, gbc, 2, "Confirm New:", txtConfirmPass);

        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton("Update Password");
        btnSave.setBackground(new Color(0, 120, 215));
        btnSave.setForeground(Color.WHITE);

        btnSave.addActionListener(e -> savePassword());

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        add(new JLabel("Update your login credentials", SwingConstants.CENTER), BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int y, String label, Component c) {
        gbc.gridx = 0; gbc.gridy = y; p.add(new JLabel(label), gbc);
        gbc.gridx = 1; p.add(c, gbc);
    }

    private void savePassword() {
        String oldP = new String(txtOldPass.getPassword());
        String newP = new String(txtNewPass.getPassword());
        String confP = new String(txtConfirmPass.getPassword());

        if (oldP.isEmpty() || newP.isEmpty()) {
            MessageDialog.showError(this, "Please fill all fields.");
            return;
        }
        if (!newP.equals(confP)) {
            MessageDialog.showError(this, "New passwords do not match.");
            return;
        }

        // --- LOADING LOGIC ---
        // We pass 'this' (the dialog) as the parent so the loader appears over it
        LoadingDialog loader = new LoadingDialog(this, "Updating Password...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                AuthService auth = new AuthService();
                return auth.updatePassword(userId, oldP, newP);
            }

            @Override
            protected void done() {
                loader.dispose();
                try {
                    if (get()) {
                        MessageDialog.showInfo(ChangePasswordDialog.this, "Password updated successfully!");
                        dispose(); // Close the dialog on success
                    } else {
                        MessageDialog.showError(ChangePasswordDialog.this, "Incorrect old password.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();

        loader.setVisible(true);
    }
}
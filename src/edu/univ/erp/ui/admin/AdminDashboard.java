package edu.univ.erp.ui.admin;

import edu.univ.erp.ui.common.UIUtils;
import edu.univ.erp.ui.common.MessageDialog;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.ui.common.LoadingDialog;
import edu.univ.erp.data.LoggerDAO;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import com.formdev.flatlaf.FlatLightLaf;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import edu.univ.erp.domain.Section; // Ensure this is imported

public class AdminDashboard extends JFrame {

    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private int userId;

    // Services & DAOs
    private AdminService adminService;
    private SettingsDAO settingsDAO;
    private CourseDAO courseDAO;
    private LoggerDAO loggerDAO = new LoggerDAO();
//    private Section section;

    private JButton activeNavButton; // for active highlight

    // Visual constants
    private static final Font UI_FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color DARK_TEXT = new Color(34, 34, 34);
    private static final Color SOFT_PANEL_BG = new Color(250, 250, 250);
    private static final Color SOFT_BG = new Color(245, 246, 247);
    private static final Color HIGHLIGHT_BLUE = new Color(233, 243, 254);

    public AdminDashboard() {
        this(0);
    }

    public AdminDashboard(int userId) {
        this.userId = userId;
        this.adminService = new AdminService();
        this.settingsDAO = new SettingsDAO();
        this.courseDAO = new CourseDAO();

        setTitle("Admin Console — University ERP");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // match Student panel background
        getContentPane().setBackground(SOFT_BG);

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
        }
        initUI();
    }

    private void setPlaceholder(JTextField field, String placeholder) {
        // show placeholder initially
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        field.putClientProperty("placeholder", placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String cur = field.getText();
                String ph = (String) field.getClientProperty("placeholder");
                if (ph != null && ph.equals(cur)) {
                    field.setText("");
                    field.setForeground(DARK_TEXT); // real text color
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    String ph = (String) field.getClientProperty("placeholder");
                    if (ph != null) {
                        field.setForeground(Color.GRAY);
                        field.setText(ph);
                    }
                }
            }
        });
    }

    private String getTextFieldValue(JTextField field) {
        String raw = field.getText();
        String ph = (String) field.getClientProperty("placeholder");
        if (ph != null && ph.equals(raw)) {
            return "";
        }
        return raw == null ? "" : raw.trim();
    }

    private void initUI() {
        getContentPane().setLayout(new BorderLayout(8, 8));

        // ----- Sidebar -----
        JPanel sidebarWrap = new JPanel(new BorderLayout());
        sidebarWrap.setOpaque(false);
        sidebarWrap.setBorder(new EmptyBorder(8, 8, 8, 0));

        JPanel sidebar = new JPanel(new GridLayout(0, 1, 0, 10));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(SOFT_PANEL_BG);
        sidebar.setBorder(new CompoundBorder(
                new RoundedLineBorder(new Color(230, 230, 230), 1, 12),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = UIUtils.createHeaderLabel("Admin");
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setBorder(new EmptyBorder(0, 6, 8, 0));
        // override font to be small and dark like requested
        title.setFont(UI_FONT_SMALL);
        title.setForeground(DARK_TEXT);
        sidebar.add(title);

        // Navigation (rounded buttons, small dark font)
        JButton btnUsers = createStudentStyleNav("Manage Users", "Users", makeGlyphIcon("users"), false);
        JButton btnCourses = createStudentStyleNav("Manage Courses", "Courses", makeGlyphIcon("book"), true); // catalogue -> rounder
        JButton btnSections = createStudentStyleNav("Manage Sections", "Sections", makeGlyphIcon("grid"), false);
        JButton btnMaint = createStudentStyleNav("Maintenance", "Maintenance", makeGlyphIcon("tools"), false);
        JButton btnBackup = createStudentStyleNav("Backup / Restore", "Backup", makeGlyphIcon("backup"), false);
        JButton btnLogs = createStudentStyleNav("System Logs", "Logs", makeGlyphIcon("stats"), false); // Reuse stats icon or make new
        sidebar.add(btnLogs);

        // Change Password (preserve original action)
        JButton btnPass = createStudentStyleNav("Change Password", "Password", makeGlyphIcon("lock"), false);
        for (var al : btnPass.getActionListeners()) {
            btnPass.removeActionListener(al);
        }
        btnPass.addActionListener(e -> new ChangePasswordDialog(this, userId).setVisible(true));

        JButton btnLogout = createStudentStyleNav("Logout", "Logout", makeGlyphIcon("logout"), false);
        btnLogout.setForeground(new Color(190, 40, 40)); // red text like Student panel
        // ensure logout font small/dark red
        btnLogout.setFont(UI_FONT_SMALL);

        sidebar.add(btnUsers);
        sidebar.add(btnCourses);
        sidebar.add(btnSections);
        sidebar.add(btnMaint);
        sidebar.add(btnBackup);
        sidebar.add(btnPass);
        sidebar.add(btnLogout);

        sidebarWrap.add(sidebar, BorderLayout.CENTER);
        getContentPane().add(sidebarWrap, BorderLayout.WEST);

        // ----- Main content -----
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(SOFT_BG);
        mainContentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel mainWrap = new JPanel(new BorderLayout());
        mainWrap.setOpaque(false);
        mainWrap.setBorder(new CompoundBorder(
                new RoundedLineBorder(new Color(230, 230, 230), 1, 12),
                new EmptyBorder(10, 10, 10, 10)
        ));
        mainWrap.add(mainContentPanel, BorderLayout.CENTER);

        // add panels (original logic preserved)
        mainContentPanel.add(makeUsersPanel(), "Users");
        mainContentPanel.add(makeCoursesPanel(), "Courses");
        mainContentPanel.add(makeSectionsPanel(), "Sections");
        mainContentPanel.add(makeMaintenancePanel(), "Maintenance");
        mainContentPanel.add(makeBackupPanel(), "Backup");
        mainContentPanel.add(makeLogsPanel(), "Logs");

        getContentPane().add(mainWrap, BorderLayout.CENTER);

        // default selection
        setActiveNavButton(btnUsers);
        cardLayout.show(mainContentPanel, "Users");
    }

    private JButton createStudentStyleNav(String text, String cardName, Icon icon, boolean rounder) {
        JButton btn = new JButton(text);
        btn.setFont(UI_FONT_SMALL);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(40, 40, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIcon(icon);
        btn.setIconTextGap(10);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setOpaque(true);

        int radius = rounder ? 18 : 12;
        btn.setBorder(new CompoundBorder(
                new RoundedLineBorder(new Color(220, 220, 220), 1, radius),
                new EmptyBorder(12, 12, 12, 12)
        ));

        btn.addActionListener(e -> {
            if ("Logout".equals(cardName)) {
                logout();
                return;
            }
            setActiveNavButton(btn);
            cardLayout.show(mainContentPanel, cardName);
        });

        return btn;
    }

    // Active visual: soft light-blue background like Student panel
    private void setActiveNavButton(JButton btn) {
        if (activeNavButton != null) {
            activeNavButton.setBackground(Color.WHITE);
            activeNavButton.setForeground(new Color(40, 40, 40));
        }
        activeNavButton = btn;
        if (btn != null) {
            btn.setBackground(HIGHLIGHT_BLUE);
            btn.setForeground(new Color(20, 40, 60));
        }
    }

    private Icon makeGlyphIcon(String kind) {
        int size = 20;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(80, 80, 80));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int p = 3;
        switch (kind) {
            case "users":
                // two circles + shoulders
                g.drawOval(p + 1, p, 6, 6);
                g.drawOval(size - p - 7, p, 6, 6);
                g.drawArc(p, p + 6, 10, 6, 0, 180);
                break;
            case "book":
                g.drawRect(p, p, size - 2 * p - 1, size - 2 * p - 1);
                g.drawLine(size / 2, p, size / 2, size - p - 1);
                break;
            case "grid":
                int s = 4;
                g.drawRect(p, p, s, s);
                g.drawRect(p + s + 2, p, s, s);
                g.drawRect(p, p + s + 2, s, s);
                g.drawRect(p + s + 2, p + s + 2, s, s);
                break;
            case "tools":
                g.drawLine(p + 2, size - p - 2, size - p - 2, p + 2);
                g.drawOval(size - p - 6, p + 1, 6, 6);
                break;
            case "backup":
                g.drawArc(p, p, size - 2 * p, size - 2 * p, 0, 270);
                g.drawLine(size / 2, p + 2, size - p - 2, size / 2);
                break;
            case "lock":
                g.drawRect(p + 2, p + 6, size - 2 * (p + 2), size / 3);
                g.drawArc(p + 2, p - 1, size - 2 * (p + 2), size / 2, 0, 180);
                break;
            case "logout":
                g.drawRect(p + 2, p + 2, size - 2 * (p + 2), size - 2 * (p + 6));
                g.drawLine(size - p - 6, size / 2 - 3, size - p - 2, size / 2);
                g.drawLine(size - p - 6, size / 2 + 3, size - p - 2, size / 2);
                break;
            default:
                g.drawOval(p, p, size - 2 * p - 1, size - 2 * p - 1);
        }
        g.dispose();
        return new ImageIcon(img);
    }

    // ---------------- PANELS ----------------
    private JPanel makeUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Create New User", SwingConstants.CENTER), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Common Fields
        JTextField txtUser = new JTextField(15);
        JPasswordField txtPass = new JPasswordField(15);
        String[] roles = {"Student", "Instructor"};
        JComboBox<String> comboRole = new JComboBox<>(roles);

        // Dynamic Fields Panel (Switches between Student/Instructor inputs)
        JPanel dynamicPanel = new JPanel(new CardLayout());

        // -- Student Form --
        JPanel studentForm = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtRoll = new JTextField();
        JTextField txtProg = new JTextField();
        JTextField txtYear = new JTextField();
        studentForm.add(new JLabel("Roll No:"));
        studentForm.add(txtRoll);
        studentForm.add(new JLabel("Program:"));
        studentForm.add(txtProg);
        studentForm.add(new JLabel("Year:"));
        studentForm.add(txtYear);

        // -- Instructor Form --
        JPanel instructorForm = new JPanel(new GridLayout(1, 2, 5, 5));
        JTextField txtDept = new JTextField();
        instructorForm.add(new JLabel("Department:"));
        instructorForm.add(txtDept);

        dynamicPanel.add(studentForm, "Student");
        dynamicPanel.add(instructorForm, "Instructor");

        // Toggle logic
        comboRole.addActionListener(e -> {
            CardLayout cl = (CardLayout) dynamicPanel.getLayout();
            cl.show(dynamicPanel, (String) comboRole.getSelectedItem());
        });

        // Layout
        addFormRow(center, gbc, 0, "Username:", txtUser);
        addFormRow(center, gbc, 1, "Password:", txtPass);
        addFormRow(center, gbc, 2, "Role:", comboRole);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        center.add(new JSeparator(), gbc);

        gbc.gridy = 4;
        center.add(dynamicPanel, gbc);

        JButton btnCreate = new JButton("Create User");
        btnCreate.setBackground(new Color(0, 120, 215));
        btnCreate.setForeground(Color.WHITE);

        // ... inside makeUsersPanel ...
        btnCreate.addActionListener(e -> {
            String role = (String) comboRole.getSelectedItem();
            String u = txtUser.getText();
            String p = new String(txtPass.getPassword());
            String r = txtRoll.getText();
            String pr = txtProg.getText();
            String y = txtYear.getText();
            String d = txtDept.getText();

            // 1. Show Loader
            LoadingDialog loader = new LoadingDialog(this, "Creating User & Hashing Password...");

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if ("Student".equals(role)) {
                        return adminService.createFullStudent(u, p, r, pr, Integer.parseInt(y));
                    } else {
                        return adminService.createFullInstructor(u, p, d);
                    }
                }

                @Override
                protected void done() {
                    loader.dispose();
                    try {
                        boolean success = get();
                        if (success) {
                            MessageDialog.showInfo(AdminDashboard.this, role + " Created Successfully!");
                            // Clear fields
                            txtUser.setText("");
                            txtPass.setText("");
                        } else {
                            MessageDialog.showError(AdminDashboard.this, "Creation Failed. Username may exist.");
                        }
                    } catch (Exception ex) {
                        MessageDialog.showError(AdminDashboard.this, "Error: " + ex.getMessage());
                    }
                }
            }.execute();

            loader.setVisible(true);
        });

        JPanel wrapper = new JPanel();
        wrapper.add(center);
        panel.add(wrapper, BorderLayout.CENTER);
        panel.add(btnCreate, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeCoursesPanel() {
        JPanel card = UIUtils.createCardPanel();
        card.setBorder(new CompoundBorder(new RoundedLineBorder(new Color(235, 235, 235), 1, 12), new EmptyBorder(10, 10, 10, 10)));
        card.add(createHeader("Add New Course"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SOFT_PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField code = new JTextField(15);
        JTextField title = new JTextField(15);
        JTextField cred = new JTextField(15);

        addFormRow(form, gbc, 0, "Course Code:", boxedComponent(code));
        addFormRow(form, gbc, 1, "Title:", boxedComponent(title));
        addFormRow(form, gbc, 2, "Credits:", boxedComponent(cred));

        JButton btn = UIUtils.createPrimaryButton("Save Course");
        btn.setFont(UI_FONT_SMALL);
        btn.addActionListener(e -> {
            try {
                boolean ok = adminService.createCourse(code.getText(), title.getText(), Integer.parseInt(cred.getText()));
                if (ok) {
                    MessageDialog.showInfo("Course Added!");
                    code.setText("");
                } else {
                    MessageDialog.showError("Failed.");
                }
            } catch (Exception ex) {
                MessageDialog.showError("Invalid inputs.");
            }
        });

        JPanel cw = new JPanel();
        cw.setBackground(SOFT_PANEL_BG);
        cw.add(form);
        card.add(cw, BorderLayout.CENTER);
        JPanel bp = new JPanel();
        bp.setBackground(SOFT_PANEL_BG);
        bp.add(btn);
        card.add(bp, BorderLayout.SOUTH);
        return card;
    }

private JPanel makeSectionsPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    // 1. TOP: CREATION FORM
    JPanel topForm = new JPanel(new GridBagLayout());
    topForm.setBorder(BorderFactory.createTitledBorder("Schedule New Section"));
    topForm.setBackground(new Color(250, 250, 250));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Inputs
    JTextField txtCourse = new JTextField(8);
    JTextField txtInstId = new JTextField(5);
    JTextField txtTime = new JTextField(12);
    setPlaceholder(txtTime, "e.g. Mon/Fri 09:00");

    JTextField txtRoom = new JTextField(6);
    JTextField txtCap = new JTextField(4);
    JTextField txtSem = new JTextField("Monsoon");
    JTextField txtYear = new JTextField("2025");

    JTextField txtRegDate = new JTextField("2025-09-01");
    JTextField txtDropDate = new JTextField("2025-09-15");

    // -- Layout Rows --
    gbc.gridy = 0;
    gbc.gridx = 0; topForm.add(new JLabel("Course Code:"), gbc);
    gbc.gridx = 1; topForm.add(txtCourse, gbc);
    gbc.gridx = 2; topForm.add(new JLabel("Instructor ID:"), gbc);
    gbc.gridx = 3; topForm.add(txtInstId, gbc);

    gbc.gridy = 1;
    gbc.gridx = 0; topForm.add(new JLabel("Time:"), gbc);
    gbc.gridx = 1; topForm.add(txtTime, gbc);
    gbc.gridx = 2; topForm.add(new JLabel("Room:"), gbc);
    gbc.gridx = 3; topForm.add(txtRoom, gbc);

    gbc.gridy = 2;
    gbc.gridx = 0; topForm.add(new JLabel("Capacity:"), gbc);
    gbc.gridx = 1; topForm.add(txtCap, gbc);
    gbc.gridx = 2; topForm.add(new JLabel("Sem/Year:"), gbc);
    JPanel sy = new JPanel(new GridLayout(1, 2, 5, 0));
    sy.setOpaque(false);
    sy.add(txtSem); sy.add(txtYear);
    gbc.gridx = 3; topForm.add(sy, gbc);

    gbc.gridy = 3;
    gbc.gridx = 0; topForm.add(new JLabel("Reg Deadline:"), gbc);
    gbc.gridx = 1; topForm.add(txtRegDate, gbc);
    gbc.gridx = 2; topForm.add(new JLabel("Drop Deadline:"), gbc);
    gbc.gridx = 3; topForm.add(txtDropDate, gbc);

    JButton btnCreate = new JButton("Create Section");
    btnCreate.setBackground(new Color(0, 120, 215));
    btnCreate.setForeground(Color.WHITE);
    gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 4;
    topForm.add(btnCreate, gbc);

    panel.add(topForm, BorderLayout.NORTH);

    // 2. BOTTOM: LISTS
    javax.swing.table.DefaultTableModel modelCourses = new javax.swing.table.DefaultTableModel(new String[]{"Code", "Title", "Credits"}, 0);
    javax.swing.table.DefaultTableModel modelInstructors = new javax.swing.table.DefaultTableModel(new String[]{"ID", "Department"}, 0);
    javax.swing.table.DefaultTableModel modelStudents = new javax.swing.table.DefaultTableModel(new String[]{"ID", "Roll No", "Program"}, 0);
    javax.swing.table.DefaultTableModel modelSections = new javax.swing.table.DefaultTableModel(new String[]{"ID", "Course", "Inst ID", "Time", "Cap"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable tableSections = new JTable(modelSections);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Courses", new JScrollPane(new JTable(modelCourses)));
    tabs.addTab("Instructors", new JScrollPane(new JTable(modelInstructors)));
    tabs.addTab("Students", new JScrollPane(new JTable(modelStudents)));
    tabs.addTab("Existing Sections", new JScrollPane(tableSections));

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton btnRefresh = new JButton("🔄 Refresh Lists");
    JButton btnDelete = new JButton("🗑 Delete Section");
    btnDelete.setBackground(new Color(220, 53, 69));
    btnDelete.setForeground(Color.WHITE);

    // --- REFRESH LOGIC ---
    ActionListener runRefresh = e -> {
        LoadingDialog loader = new LoadingDialog(this, "Fetching data...");
        new SwingWorker<Void, Void>() {
            java.util.List<edu.univ.erp.domain.Course> courses;
            java.util.List<edu.univ.erp.domain.Instructor> instructors;
            java.util.List<edu.univ.erp.domain.Student> students;
            java.util.List<edu.univ.erp.domain.Section> sections;

            @Override protected Void doInBackground() {
                courses = adminService.getAllCourses();
                instructors = adminService.getAllInstructors();
                students = adminService.getAllStudents();
                sections = adminService.getAllSections();
                return null;
            }

            @Override protected void done() {
                loader.dispose();
                try {
                    get();
                    modelCourses.setRowCount(0); modelInstructors.setRowCount(0);
                    modelStudents.setRowCount(0); modelSections.setRowCount(0);

                    for(var c : courses) modelCourses.addRow(new Object[]{c.getCourse_code(), c.getTitle(), c.getCredits()});
                    for(var i : instructors) modelInstructors.addRow(new Object[]{i.getUser_id(), i.getDepartment()});
                    for(var s : students) modelStudents.addRow(new Object[]{s.getUser_id(), s.getRoll_no(), s.getProgram()});
                    for(var s : sections) modelSections.addRow(new Object[]{s.getSection_id(), s.getCourse_code(), s.getInstructor_id(), s.getDay_time(), s.getCapacity()});
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
        loader.setVisible(true);
    };
    btnRefresh.addActionListener(runRefresh);

    // --- DELETE LOGIC ---
    btnDelete.addActionListener(e -> {
        if (tabs.getSelectedIndex() != 3) {
            MessageDialog.showError(this, "Switch to 'Existing Sections' tab to delete.");
            return;
        }
        int row = tableSections.getSelectedRow();
        if (row == -1) {
            MessageDialog.showError(this, "Select a section to delete.");
            return;
        }
        int sectionId = (int) modelSections.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete Section " + sectionId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String res = adminService.deleteSection(sectionId);
            if (res.startsWith("Success")) {
                MessageDialog.showInfo(this, res);
                btnRefresh.doClick();
            } else {
                MessageDialog.showError(this, res);
            }
        }
    });

    // --- CREATE LOGIC (FIXED) ---
    btnCreate.addActionListener(e -> {
        try {
            // 1. Validate Capacity FIRST
            int capacity = Integer.parseInt(txtCap.getText().trim());
            if (capacity <= 0) {
                MessageDialog.showError(this, "Capacity must be positive.");
                return; // Stop here
            }

            // 2. Build Object
            Section s = new Section();
            s.setCapacity(capacity);
            s.setCourse_code(txtCourse.getText().trim());
            s.setInstructor_id(Integer.parseInt(txtInstId.getText().trim()));
            s.setDay_time(txtTime.getText().trim());
            s.setRoom(txtRoom.getText().trim());
            s.setSemester(txtSem.getText().trim());
            s.setYear(Integer.parseInt(txtYear.getText().trim()));

            String regStr = txtRegDate.getText().trim();
            String dropStr = txtDropDate.getText().trim();
            if (!regStr.isEmpty()) s.setRegDeadline(LocalDate.parse(regStr));
            if (!dropStr.isEmpty()) s.setDropDeadline(LocalDate.parse(dropStr));

            // 3. Send to Service
            boolean ok = adminService.createSection(s);
            if (ok) {
                MessageDialog.showInfo(this, "Section Created Successfully!");
                btnRefresh.doClick();
            } else {
                MessageDialog.showError(this, "Failed. Check constraints (Instructor ID, Course Code).");
            }
        } catch (NumberFormatException nfe) {
            MessageDialog.showError(this, "Invalid Number: " + nfe.getMessage());
        } catch (DateTimeParseException dtpe) {
            MessageDialog.showError(this, "Invalid Date Format. Use YYYY-MM-DD.");
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageDialog.showError(this, "Error: " + ex.getMessage());
        }
    });

    SwingUtilities.invokeLater(btnRefresh::doClick);
    buttonPanel.add(btnRefresh);
    buttonPanel.add(btnDelete);
    JPanel bottomContainer = new JPanel(new BorderLayout());
    bottomContainer.add(buttonPanel, BorderLayout.NORTH);
    bottomContainer.add(tabs, BorderLayout.CENTER);
    panel.add(bottomContainer, BorderLayout.CENTER);
    return panel;
}
    private JPanel makeMaintenancePanel() {
        JPanel card = UIUtils.createCardPanel();
        card.setBorder(new CompoundBorder(new RoundedLineBorder(new Color(235, 235, 235), 1, 12), new EmptyBorder(10, 10, 10, 10)));
        card.add(createHeader("System Maintenance"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(SOFT_PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);

        boolean isOn = settingsDAO.isMaintenanceModeOn();
        JLabel status = new JLabel(isOn ? "Status: ON" : "Status: OFF");
        status.setFont(UI_FONT_SMALL);

        JButton toggle = UIUtils.createPrimaryButton("Toggle Mode");
        toggle.setFont(UI_FONT_SMALL);
        if (isOn) {
            toggle.setBackground(Color.RED);
        }

        toggle.addActionListener(e -> {
            boolean current = settingsDAO.isMaintenanceModeOn();
            adminService.toggleMaintenanceMode(!current);
            boolean newStatus = !current;
            status.setText(newStatus ? "Status: ON" : "Status: OFF");
            toggle.setBackground(newStatus ? Color.RED : UIUtils.COLOR_PRIMARY);
            MessageDialog.showInfo("Maintenance is now " + (newStatus ? "ON" : "OFF"));
        });

        content.add(status, gbc);
        gbc.gridy++;
        content.add(toggle, gbc);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel makeBackupPanel() {
        JPanel card = UIUtils.createCardPanel();
        card.setBorder(new javax.swing.border.CompoundBorder(new AdminDashboard.RoundedLineBorder(new Color(235, 235, 235), 1, 12), new javax.swing.border.EmptyBorder(10, 10, 10, 10)));
        card.add(createHeader("Database Operations"), BorderLayout.NORTH);

        JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 30));
        content.setBackground(new Color(250, 250, 250));

        JButton backup = UIUtils.createPrimaryButton("Backup Database");
        backup.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton restore = new JButton("Restore Database");
        restore.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        restore.setBackground(new Color(220, 53, 69));
        restore.setForeground(Color.WHITE);
        restore.setFocusPainted(false);
        restore.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.LineBorder(new Color(200, 40, 40), 1), new javax.swing.border.EmptyBorder(10, 20, 10, 20)));

        backup.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("backup.sql"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                // 1. Show Loader
                LoadingDialog loader = new LoadingDialog(this, "Backing up data (this may take time)...");
                File targetFile = fc.getSelectedFile();

                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() {
                        return adminService.backupDatabase(targetFile);
                    }

                    @Override
                    protected void done() {
                        loader.dispose();
                        try {
                            if (get()) {
                                MessageDialog.showInfo(AdminDashboard.this, "Backup Successful!"); 
                            }else {
                                MessageDialog.showError(AdminDashboard.this, "Backup Failed. Check console/logs.");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();

                loader.setVisible(true);
            }
        });

        restore.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                LoadingDialog loader = new LoadingDialog(this, "Restoring Database... Please wait.");
                File sourceFile = fc.getSelectedFile();

                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() {
                        return adminService.restoreDatabase(sourceFile);
                    }

                    @Override
                    protected void done() {
                        loader.dispose();
                        try {
                            if (get()) {
                                MessageDialog.showInfo(AdminDashboard.this, "Restore Successful!"); 
                            }else {
                                MessageDialog.showError(AdminDashboard.this, "Restore Failed.");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();

                loader.setVisible(true);
            }
        });

        content.add(backup);
        content.add(restore);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHeader(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SOFT_PANEL_BG);
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(UI_FONT_SMALL);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        l.setForeground(DARK_TEXT);
        p.add(l, BorderLayout.NORTH);
        return p;
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int y, String lbl, Component c) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel label = new JLabel(lbl);
        label.setFont(UI_FONT_SMALL);
        label.setForeground(DARK_TEXT);
        p.add(label, gbc);
        gbc.gridx = 1;
        p.add(c, gbc);
    }

    private void logout() {
        dispose();
        new edu.univ.erp.ui.auth.LoginFrame().setVisible(true);
    }

    private JComponent boxedComponent(JComponent comp) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(new CompoundBorder(new RoundedLineBorder(new Color(235, 235, 235), 1, 10), new EmptyBorder(6, 8, 6, 8)));
        comp.setFont(UI_FONT_SMALL);
        wrap.add(comp, BorderLayout.CENTER);
        return wrap;
    }

    private JButton boxedButton(JButton src) {
        src.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        src.setFocusPainted(false);
        src.setFont(UI_FONT_SMALL);
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(2, 4, 2, 4));
        wrapper.add(src);
        return src;
    }

    private JPanel makeLogsPanel() {
        JPanel card = UIUtils.createCardPanel();
        card.setBorder(new javax.swing.border.CompoundBorder(new AdminDashboard.RoundedLineBorder(new Color(235, 235, 235), 1, 12), new javax.swing.border.EmptyBorder(10, 10, 10, 10)));
        card.add(createHeader("System Access & Activity Logs"), BorderLayout.NORTH);

        String[] cols = {"Log ID", "Timestamp", "User", "Action", "Details"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);

        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getColumnModel().getColumn(0).setMaxWidth(60); // ID small
        table.getColumnModel().getColumn(1).setMinWidth(140); // Timestamp

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        // Refresh Button with Loader
        JButton btnRefresh = UIUtils.createPrimaryButton("Refresh Logs");

        btnRefresh.addActionListener(e -> {
            edu.univ.erp.ui.common.LoadingDialog loader
                    = new edu.univ.erp.ui.common.LoadingDialog(this, "Fetching System Logs...");

            new SwingWorker<java.util.List<Object[]>, Void>() {
                @Override
                protected java.util.List<Object[]> doInBackground() {
                    return loggerDAO.getAllLogs();
                }

                @Override
                protected void done() {
                    loader.dispose();
                    try {
                        java.util.List<Object[]> data = get();
                        model.setRowCount(0);
                        for (Object[] row : data) {
                            model.addRow(row);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();

            loader.setVisible(true);
        });

        // Initial Load
        SwingUtilities.invokeLater(btnRefresh::doClick);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(250, 250, 250));
        bottom.add(btnRefresh);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }

    static class RoundedLineBorder extends AbstractBorder {

        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(thickness));
                int inset = thickness / 2;
                RoundRectangle2D rr = new RoundRectangle2D.Float(
                        x + inset, y + inset,
                        width - thickness, height - thickness,
                        radius, radius
                );
                g2.draw(rr);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = thickness;
            return insets;
        }
    }
}

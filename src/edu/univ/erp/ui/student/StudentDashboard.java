package edu.univ.erp.ui.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.ui.common.MessageDialog;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.ui.common.UIUtils;
import edu.univ.erp.ui.common.BannerPanel;
import edu.univ.erp.ui.common.LoadingDialog;
import edu.univ.erp.auth.UserSession;
import java.time.LocalDate;

public class StudentDashboard extends JFrame {

    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // User Details
    private int userId;
    private String username;
    private String lastLogin;

    // Services & DAOs
    private StudentService studentService;
    private SectionDAO sectionDAO;
    private EnrollmentDAO enrollmentDAO;
    private SettingsDAO settingsDAO;

    // Constructor accepting UserSession
    public StudentDashboard(UserSession session) {
        this.userId = session.userID;
        this.username = session.username;
        this.lastLogin = (session.lastLogin != null) ? session.lastLogin : "First Login";

        this.studentService = new StudentService();
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.settingsDAO = new SettingsDAO();

        setTitle("Student Dashboard — University ERP");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(UIUtils.COLOR_BACKGROUND);

        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ex) { }
        initUI();
    }

    // Compatibility Constructor (Optional, if you haven't updated LoginFrame yet)
    public StudentDashboard(int userId, String username) {
        this(new UserSession(userId, "Student", username, "Unknown"));
    }

    private void initUI() {
        getContentPane().setLayout(new BorderLayout());

        // --- 1. Maintenance Banner ---
        if (settingsDAO.isMaintenanceModeOn()) {
            BannerPanel bannerPanel = new BannerPanel();
            bannerPanel.showMaintenanceBanner(true);
            getContentPane().add(bannerPanel, BorderLayout.NORTH);
        }

        // --- 2. Sidebar ---
        JPanel sidebar = new JPanel(new GridLayout(9, 1, 0, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebar.setBackground(UIUtils.COLOR_WHITE);
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Header Info
        JPanel headerInfo = new JPanel(new GridLayout(2, 1));
        headerInfo.setBackground(UIUtils.COLOR_WHITE);

        JLabel title = UIUtils.createHeaderLabel(username);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblLogin = new JLabel("Last Login: " + lastLogin);
        lblLogin.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLogin.setForeground(Color.GRAY);
        lblLogin.setHorizontalAlignment(SwingConstants.CENTER);

        headerInfo.add(title);
        headerInfo.add(lblLogin);
        sidebar.add(headerInfo);

        // Navigation Buttons
        sidebar.add(createNavButton("Course Catalog", "Catalog", "catalog.png"));
        sidebar.add(createNavButton("My Registrations", "Registrations", "registrations.png"));
        sidebar.add(createNavButton("Timetable", "Timetable", "timetable.png"));
        sidebar.add(createNavButton("Grades", "Grades", "grades.png"));
        sidebar.add(createNavButton("Transcript", "Transcript", "transcript.png"));

        JButton btnPassword = createNavButton("Change Password", "Password", null);
        for(var al : btnPassword.getActionListeners()) btnPassword.removeActionListener(al);
        btnPassword.addActionListener(e -> new ChangePasswordDialog(this, userId).setVisible(true));
        sidebar.add(btnPassword);

        JButton btnLogout = createNavButton("Logout", "Logout", null);
        btnLogout.setForeground(Color.RED);
        sidebar.add(btnLogout);

        // --- 3. Main Content Area ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize Panels
        mainContentPanel.add(makeCatalogPanel(), "Catalog");
        mainContentPanel.add(makeRegistrationsPanel(), "Registrations");
        mainContentPanel.add(makeTimetablePanel(), "Timetable");
        mainContentPanel.add(makeGradesPanel(), "Grades");
        mainContentPanel.add(makeTranscriptPanel(), "Transcript");

        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(mainContentPanel, BorderLayout.CENTER);
    }

    // --- Helper to create styled Nav Buttons ---
    private JButton createNavButton(String text, String cardName, String iconName) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_GENERAL);
        btn.setBackground(UIUtils.COLOR_WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (iconName != null) {
            btn.setIcon(loadIcon(iconName));
            btn.setIconTextGap(15);
        }

        btn.addActionListener(e -> {
            if(cardName.equals("Logout")) logout();
            else cardLayout.show(mainContentPanel, cardName);
        });
        return btn;
    }

    // PANEL IMPLEMENTATIONS (With Loading Dialogs)

    private JPanel makeCatalogPanel() {
        JPanel card = UIUtils.createCardPanel();
        card.add(createHeader("Course Catalog"), BorderLayout.NORTH);

        // 1. Updated Columns to show Capacity and Deadlines
        String[] cols = {"ID", "Course", "Day/Time", "Seats", "Reg Deadline", "Status"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        // Optional: Adjust column widths
        table.getColumnModel().getColumn(0).setMaxWidth(50); // ID
        table.getColumnModel().getColumn(3).setMaxWidth(80); // Seats
        table.getColumnModel().getColumn(5).setMaxWidth(80); // Status

        // 2. Async Load Catalog with Status Logic
        SwingUtilities.invokeLater(() -> {
            LoadingDialog loader = new LoadingDialog(this, "Loading Catalog...");
            new SwingWorker<List<Section>, Void>() {
                @Override
                protected List<Section> doInBackground() {
                    // This calls the updated DAO which fetches enrollment counts + dates
                    return sectionDAO.getAllAvailableSections();
                }

                @Override
                protected void done() {
                    loader.dispose();
                    try {
                        model.setRowCount(0); // Clear table
                        List<Section> sections = get();

                        for (Section s : sections) {
                            // A. Calculate Seat Status
                            int filled = s.getEnrolledCount();
                            int total = s.getCapacity();
                            String seatsDisplay = filled + " / " + total;

                            // B. Format Deadline
                            String deadlineStr = (s.getRegDeadline() == null) ? "-" : s.getRegDeadline().toString();

                            // C. Determine Status (Open / Full / Closed)
                            String status = "OPEN";
                            if (filled >= total) {
                                status = "FULL";
                            } else if (s.getRegDeadline() != null && LocalDate.now().isAfter(s.getRegDeadline())) {
                                status = "CLOSED";
                            }

                            // D. Add Row
                            model.addRow(new Object[]{
                                    s.getSection_id(),
                                    s.getCourse_code(),
                                    s.getDay_time(),
                                    seatsDisplay,
                                    deadlineStr,
                                    status
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageDialog.showError(StudentDashboard.this, "Error loading catalog.");
                    }
                }
            }.execute();
            loader.setVisible(true);
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. Register Button Logic
        JButton btn = UIUtils.createPrimaryButton("Register Selected");

        btn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                MessageDialog.showError(this, "Please select a section to register.");
                return;
            }

            // UX: Pre-check status from the table model before hitting DB
            String status = (String) model.getValueAt(row, 5);
            if ("FULL".equalsIgnoreCase(status)) {
                MessageDialog.showError(this, "Cannot Register: Section is FULL.");
                return;
            }
            if ("CLOSED".equalsIgnoreCase(status)) {
                MessageDialog.showError(this, "Cannot Register: Deadline has passed.");
                return;
            }

            int secId = (int) table.getValueAt(row, 0);

            // Async Registration Call
            LoadingDialog loader = new LoadingDialog(this, "Registering...");
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return studentService.registerForSection(userId, secId);
                }

                @Override
                protected void done() {
                    loader.dispose();
                    try {
                        String res = get();
                        if (res.startsWith("Success")) {
                            MessageDialog.showInfo(StudentDashboard.this, res);
                            // Refresh the table to update seat counts
                            // (You could extract the loading logic into a separate method 'loadCatalog()' and call it here)
                        } else {
                            MessageDialog.showError(StudentDashboard.this, res);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
            loader.setVisible(true);
        });

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.setBackground(Color.WHITE);
        bp.add(btn);
        card.add(bp, BorderLayout.SOUTH);

        return card;
    }

    private JPanel makeRegistrationsPanel() {
        JPanel card = UIUtils.createCardPanel();
        card.add(createHeader("My Registrations"), BorderLayout.NORTH);
        String[] cols = {"ID", "Code", "Title", "Time", "Room", "Status", "Grade"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        styleTable(table);
        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0); table.getColumnModel().getColumn(0).setMaxWidth(0);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Shared Refresh Logic ---
        Runnable runRefresh = () -> {
            LoadingDialog loader = new LoadingDialog(this, "Fetching Registrations...");
            new SwingWorker<List<Object[]>, Void>() {
                @Override protected List<Object[]> doInBackground() {
                    return enrollmentDAO.getStudentRegistrations(userId);
                }
                @Override protected void done() {
                    loader.dispose();
                    try {
                        model.setRowCount(0);
                        for(Object[] r : get()) model.addRow(r);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
            loader.setVisible(true);
        };

        // Load initially
        SwingUtilities.invokeLater(runRefresh);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bp.setBackground(Color.WHITE);

        JButton drop = UIUtils.createPrimaryButton("Drop Selected");
        drop.setBackground(new Color(220, 53, 69));

        // --- Async Drop ---
        drop.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row==-1) { MessageDialog.showError(this, "Select a course."); return; }
            int secId = (int)model.getValueAt(row, 0);

            LoadingDialog loader = new LoadingDialog(this, "Dropping Course...");
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() {
                    return studentService.dropSection(userId, secId);
                }
                @Override protected void done() {
                    loader.dispose();
                    try {
                        String res = get();
                        if(res.startsWith("Success")) {
                            MessageDialog.showInfo(StudentDashboard.this, res);
                            runRefresh.run(); // Reload list
                        } else {
                            MessageDialog.showError(StudentDashboard.this, res);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
            loader.setVisible(true);
        });

        JButton ref = UIUtils.createPrimaryButton("Refresh");
        ref.addActionListener(e -> runRefresh.run());

        bp.add(drop); bp.add(ref);
        card.add(bp, BorderLayout.SOUTH);
        return card;
    }

    private JPanel makeTimetablePanel() {
        JPanel card = UIUtils.createCardPanel();
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(Color.WHITE);
        JLabel header = new JLabel("Weekly Schedule");
        header.setFont(UIUtils.FONT_SUBHEADER);
        headerPanel.add(header);
        card.add(headerPanel, BorderLayout.NORTH);

        String[] days = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] times = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};

        DefaultTableModel model = new DefaultTableModel(days, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (String t : times) model.addRow(new Object[]{t, "", "", "", "", ""});

        JTable table = new JTable(model);
        table.setRowHeight(65);
        table.getTableHeader().setFont(UIUtils.FONT_BUTTON);
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setVerticalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Async Timetable Builder ---
        Runnable runLoad = () -> {
            LoadingDialog loader = new LoadingDialog(this, "Building Timetable...");
            new SwingWorker<List<Object[]>, Void>() {
                @Override protected List<Object[]> doInBackground() {
                    return enrollmentDAO.getStudentRegistrations(userId);
                }
                @Override protected void done() {
                    loader.dispose();
                    try {
                        // Clear grid
                        for (int r = 0; r < model.getRowCount(); r++)
                            for (int c = 1; c < model.getColumnCount(); c++) model.setValueAt("", r, c);

                        List<Object[]> data = get();
                        // Parse logic
                        for (Object[] row : data) {
                            if (!"enrolled".equalsIgnoreCase((String) row[5])) continue;
                            String code = (String) row[1];
                            String rawTime = (String) row[3];
                            String room = (String) row[4];

                            if (rawTime != null && rawTime.contains(" ")) {
                                try {
                                    String[] parts = rawTime.split(" ");
                                    String timePart = parts[1];
                                    int rowIndex = -1;
                                    String hourPrefix = timePart.split(":")[0];
                                    for (int r = 0; r < times.length; r++) {
                                        if (times[r].startsWith(hourPrefix)) { rowIndex = r; break; }
                                    }
                                    if (rowIndex != -1) {
                                        for (String d : parts[0].split("/")) {
                                            int colIndex = -1;
                                            if (d.startsWith("Mon")) colIndex = 1;
                                            else if (d.startsWith("Tue")) colIndex = 2;
                                            else if (d.startsWith("Wed")) colIndex = 3;
                                            else if (d.startsWith("Thu")) colIndex = 4;
                                            else if (d.startsWith("Fri")) colIndex = 5;

                                            if (colIndex != -1) {
                                                String cellContent = "<html><div style='text-align:center; color:#0056aa;'><b>"
                                                        + code + "</b><br><span style='color:gray; font-size:9px;'>"
                                                        + room + "</span></div></html>";
                                                model.setValueAt(cellContent, rowIndex, colIndex);
                                            }
                                        }
                                    }
                                } catch (Exception ex) { }
                            }
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
            loader.setVisible(true);
        };

        SwingUtilities.invokeLater(runLoad);

        JButton refreshBtn = UIUtils.createPrimaryButton("Refresh Schedule");
        refreshBtn.addActionListener(e -> runLoad.run());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(refreshBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel makeGradesPanel() {
        JPanel card = UIUtils.createCardPanel();
        card.add(createHeader("My Grades"), BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Code", "Title", "Grade"}, 0);
        JTable table = new JTable(model);
        styleTable(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton ref = UIUtils.createPrimaryButton("Refresh");

        // --- Async Grade Fetch ---
        ref.addActionListener(e -> {
            LoadingDialog loader = new LoadingDialog(this, "Fetching Grades...");
            new SwingWorker<List<Object[]>, Void>() {
                @Override protected List<Object[]> doInBackground() {
                    return enrollmentDAO.getStudentRegistrations(userId);
                }
                @Override protected void done() {
                    loader.dispose();
                    try {
                        model.setRowCount(0);
                        for(Object[] r : get())
                            model.addRow(new Object[]{r[1], r[2], r[6]==null?"-":r[6]});
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
            loader.setVisible(true);
        });

        SwingUtilities.invokeLater(ref::doClick);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bp.setBackground(Color.WHITE); bp.add(ref);
        card.add(bp, BorderLayout.SOUTH);
        return card;
    }

    private JPanel makeTranscriptPanel() {
        JPanel card = UIUtils.createCardPanel();
        JPanel c = new JPanel(new GridBagLayout()); c.setBackground(Color.WHITE);
        JButton btn = UIUtils.createPrimaryButton("Download CSV");

        // --- Async Transcript Download ---
        btn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
                LoadingDialog loader = new LoadingDialog(this, "Generating Transcript...");
                new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() throws Exception {
                        // DB Call
                        List<Object[]> data = enrollmentDAO.getStudentRegistrations(userId);
                        // File IO
                        try(java.io.PrintWriter w = new java.io.PrintWriter(fc.getSelectedFile())) {
                            w.println("Code,Title,Status,Grade");
                            for(Object[] r : data) w.println(r[1]+","+r[2]+","+r[5]+","+(r[6]==null?"N/A":r[6]));
                        }
                        return null;
                    }
                    @Override protected void done() {
                        loader.dispose();
                        try {
                            get(); // Check for exceptions
                            MessageDialog.showInfo(StudentDashboard.this, "Transcript Saved!");
                        } catch (Exception ex) {
                            MessageDialog.showError(StudentDashboard.this, "Error saving file.");
                        }
                    }
                }.execute();
                loader.setVisible(true);
            }
        });

        c.add(new JLabel("Download Official Transcript"), new GridBagConstraints());
        GridBagConstraints gbc = new GridBagConstraints(); gbc.gridy=1; gbc.insets=new Insets(10,0,0,0);
        c.add(btn, gbc);
        card.add(c, BorderLayout.CENTER);
        return card;
    }

    // --- UTILS ---
    private JPanel createHeader(String t) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(Color.WHITE);
        JLabel l = new JLabel(t); l.setFont(UIUtils.FONT_SUBHEADER); p.add(l, BorderLayout.NORTH);
        return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(35); t.getTableHeader().setFont(UIUtils.FONT_BUTTON);
        t.getTableHeader().setBackground(new Color(240,240,240)); t.setShowVerticalLines(false);
    }

    private ImageIcon loadIcon(String fileName) {
        try {
            java.net.URL imgURL = getClass().getResource("/icons/" + fileName);
            if (imgURL != null) {
                Image img = new ImageIcon(imgURL).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            } else return null;
        } catch (Exception e) { return null; }
    }

    private void logout() { dispose(); new edu.univ.erp.ui.auth.LoginFrame().setVisible(true); }

    public static void main(String[] args) {
        // Pass dummy ID and Username for testing
        SwingUtilities.invokeLater(() -> new StudentDashboard(1, "Test Student").setVisible(true));
    }
}
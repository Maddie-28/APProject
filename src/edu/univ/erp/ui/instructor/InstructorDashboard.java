package edu.univ.erp.ui.instructor;

import edu.univ.erp.ui.common.UIUtils;
import edu.univ.erp.ui.common.MessageDialog;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.ui.common.LoadingDialog;
import edu.univ.erp.auth.UserSession; // Import Added

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import com.formdev.flatlaf.FlatLightLaf;

import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.ui.common.BannerPanel;

public class InstructorDashboard extends JFrame {

    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // User Info
    private int userId;
    private String lastLogin; // Added

    private InstructorService instructorService;
    private SectionDAO sectionDAO;
    private EnrollmentDAO enrollmentDAO;
    private SettingsDAO settingsDAO;

    // Visual constants
    private static final Color DARK_TEXT = new Color(28, 28, 28);
    private static final Color DARK_BORDER = new Color(90, 90, 90);
    private static final Color SOFT_BG = new Color(248, 249, 250);
    private static final Color OPTION_BG = new Color(255, 255, 255);
    private static final Color HIGHLIGHT_GREEN = new Color(76, 175, 80);
    private static final Color HIGHLIGHT_GREEN_SOFT = new Color(232, 245, 233);
    private static final Color HIGHLIGHT_RED = new Color(196, 28, 28);
    private static final Color HIGHLIGHT_RED_SOFT = new Color(255, 235, 238);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final Map<String, JButton> navButtons = new HashMap<>();
    private final Map<String, Icon> glyphCache = new HashMap<>();

    // Updated Constructor to accept UserSession
    public InstructorDashboard(UserSession session) {
        this.userId = session.userID;
        this.lastLogin = (session.lastLogin != null) ? session.lastLogin : "First Login";

        this.instructorService = new InstructorService();
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.settingsDAO = new SettingsDAO();

        setTitle("Instructor Dashboard — University ERP");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(UIUtils.COLOR_BACKGROUND);

        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ex) { }
        initUI();
    }

    // Fallback constructor
    public InstructorDashboard(int userId) {
        this(new UserSession(userId, "Instructor", "Instructor", "Unknown"));
    }

    private void initUI() {
        getContentPane().setLayout(new BorderLayout());

        if (settingsDAO.isMaintenanceModeOn()) {
            BannerPanel bannerPanel = new BannerPanel();
            bannerPanel.showMaintenanceBanner(true);
            getContentPane().add(bannerPanel, BorderLayout.NORTH);
        }

        // Sidebar
        JPanel sidebar = new JPanel(new GridLayout(9, 1, 0, 12));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        sidebar.setBackground(SOFT_BG);
        sidebar.setPreferredSize(new Dimension(260, 0));

        // Header with Last Login
        JPanel headerInfo = new JPanel(new GridLayout(2, 1));
        headerInfo.setBackground(SOFT_BG);
        headerInfo.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        JLabel title = new JLabel("Instructor Panel", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(DARK_TEXT);

        JLabel lblLogin = new JLabel("Last Login: " + lastLogin, SwingConstants.CENTER);
        lblLogin.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLogin.setForeground(Color.GRAY);

        headerInfo.add(title);
        headerInfo.add(lblLogin);
        sidebar.add(headerInfo);

        // Buttons
        JButton bSections = createNavButton("   My Sections", "Sections", true);
        bSections.setIcon(makeGlyphIcon("sections", 18));
        sidebar.add(bSections);

        JButton bScores = createNavButton("   Enter Scores", "Scores", false);
        bScores.setIcon(makeGlyphIcon("scores", 18));
        sidebar.add(bScores);

        JButton bFinals = createNavButton("   Compute Finals", "Finals", false);
        bFinals.setIcon(makeGlyphIcon("finals", 18));
        sidebar.add(bFinals);

        JButton bStats = createNavButton("   Class Stats", "Stats", false);
        bStats.setIcon(makeGlyphIcon("stats", 18));
        sidebar.add(bStats);

        JButton btnPass = createNavButton("   Change Password", "Password", false);
        btnPass.setIcon(makeGlyphIcon("password", 18));
        for(var al : btnPass.getActionListeners()) btnPass.removeActionListener(al);
        btnPass.addActionListener(e -> new ChangePasswordDialog(this, userId).setVisible(true));
        sidebar.add(btnPass);

        JButton btnLogout = createNavButton("   Logout", "Logout", false);
        btnLogout.setIcon(makeGlyphIcon("logout", 18));
        btnLogout.setBackground(HIGHLIGHT_RED_SOFT);
        btnLogout.setForeground(HIGHLIGHT_RED.darker());
        btnLogout.setBorder(new RoundedLineBorder(HIGHLIGHT_RED.darker(), 1, 14));
        sidebar.add(btnLogout);

        // Main content
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(OPTION_BG);
        mainContentPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(DARK_BORDER, 2, 16),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        mainContentPanel.add(makeSectionsPanel(), "Sections");
        mainContentPanel.add(makeScoresPanel(), "Scores");
        mainContentPanel.add(makeFinalsPanel(), "Finals");
        mainContentPanel.add(makeStatsPanel(), "Stats");

        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(mainContentPanel, BorderLayout.CENTER);

        cardLayout.show(mainContentPanel, "Sections");
        setActiveNav("Sections");
    }

    private JButton createNavButton(String text, String cardName, boolean highlightDefault) {
        JButton btn = new JButton(text);
        btn.setFont(UI_FONT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(OPTION_BG);
        btn.setOpaque(true);
        btn.setForeground(DARK_TEXT);
        btn.setIconTextGap(10);

        if ("Sections".equals(cardName)) {
            btn.setBorder(new RoundedLineBorder(new Color(200,200,200), 1, 18));
        } else {
            btn.setBorder(new RoundedLineBorder(new Color(200,200,200), 1, 10));
        }

        btn.setPreferredSize(new Dimension(220, 44));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (cardName.equals("Logout")) logout();
            else {
                cardLayout.show(mainContentPanel, cardName);
                setActiveNav(cardName);
            }
        });
        navButtons.put(cardName, btn);
        return btn;
    }

    private void setActiveNav(String activeCard) {
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            String key = e.getKey();
            JButton b = e.getValue();
            if (key.equals(activeCard)) {
                if (key.equals("Logout")) continue;
                b.setBackground(HIGHLIGHT_GREEN_SOFT);
                b.setForeground(HIGHLIGHT_GREEN.darker());
                b.setBorder(new RoundedLineBorder(HIGHLIGHT_GREEN.darker(), 1, 10));
            } else {
                if (key.equals("Logout")) continue;
                b.setBackground(OPTION_BG);
                b.setForeground(DARK_TEXT);
                b.setBorder(new RoundedLineBorder(new Color(200,200,200), 1, 10));
            }
        }
    }

    // --- PANELS WITH LOADERS ---
    private JPanel makeSectionsPanel() {
        JPanel card = UIUtils.createCardPanel();
        styleSubPanel(card);
        card.add(createHeader("Course Catalog"), BorderLayout.NORTH);

        String[] cols = {"Section ID", "Course", "Day/Time", "Room", "Capacity"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        table.setFont(UI_FONT);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UI_FONT);
        table.getTableHeader().setBackground(new Color(245,245,245));
        table.setShowVerticalLines(false);

        // Async Load
        SwingUtilities.invokeLater(() -> {
            LoadingDialog loader = new LoadingDialog(this, "Loading Sections...");
            new SwingWorker<List<Section>, Void>() {
                @Override protected List<Section> doInBackground() {
                    return sectionDAO.getSectionsByInstructor(userId);
                }
                @Override protected void done() {
                    loader.dispose();
                    try {
                        for (Section s : get())
                            model.addRow(new Object[]{s.getSection_id(), s.getCourse_code(), s.getDay_time(), s.getRoom(), s.getCapacity()});
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
            loader.setVisible(true);
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel makeScoresPanel() {
        JPanel card = UIUtils.createCardPanel();
        styleSubPanel(card);
        card.add(createHeader("Enter Grades"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(OPTION_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10); gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> comboSections = new JComboBox<>();
        comboSections.setFont(UI_FONT);
        JComboBox<String> comboStudents = new JComboBox<>();
        comboStudents.setFont(UI_FONT);
        JComboBox<String> comboType = new JComboBox<>(new String[]{"Quiz", "Midterm", "End-Sem"});
        comboType.setFont(UI_FONT);
        JTextField txtScore = new JTextField(10);
        txtScore.setFont(UI_FONT);

        // Populate sections async
        SwingUtilities.invokeLater(() -> {
            new SwingWorker<List<Section>, Void>() {
                @Override protected List<Section> doInBackground() {
                    return sectionDAO.getSectionsByInstructor(userId);
                }
                @Override protected void done() {
                    try {
                        for (Section s : get())
                            comboSections.addItem(s.getSection_id() + " - " + s.getCourse_code());
                    } catch(Exception e){}
                }
            }.execute();
        });

        comboSections.addActionListener(e -> {
            comboStudents.removeAllItems();
            if (comboSections.getSelectedItem() != null) {
                int secId = Integer.parseInt(((String)comboSections.getSelectedItem()).split(" - ")[0]);
                for (Integer id : enrollmentDAO.getStudentIdsInSection(secId))
                    comboStudents.addItem(String.valueOf(id));
            }
        });

        addFormRow(form, gbc, 0, "Section:", comboSections);
        addFormRow(form, gbc, 1, "Student ID:", comboStudents);
        addFormRow(form, gbc, 2, "Assessment:", comboType);
        addFormRow(form, gbc, 3, "Score:", txtScore);

        JButton btnSave = UIUtils.createPrimaryButton("Save Grade");
        styleActionButtonGreen(btnSave);

        btnSave.addActionListener(e -> {
            if(comboStudents.getSelectedItem() == null) return;
            try {
                int secId = Integer.parseInt(((String)comboSections.getSelectedItem()).split(" - ")[0]);
                int stuId = Integer.parseInt((String)comboStudents.getSelectedItem());
                String type = (String)comboType.getSelectedItem();
                double score = Double.parseDouble(txtScore.getText());

                LoadingDialog loader = new LoadingDialog(this, "Saving Grade...");
                new SwingWorker<String, Void>() {
                    @Override protected String doInBackground() {
                        int enrollId = enrollmentDAO.getEnrollmentId(stuId, secId);
                        return instructorService.enterScore(userId, enrollId, type, score);
                    }
                    @Override protected void done() {
                        loader.dispose();
                        try {
                            String res = get();
                            if(res.startsWith("Success")) { MessageDialog.showInfo(InstructorDashboard.this, res); txtScore.setText(""); }
                            else MessageDialog.showError(InstructorDashboard.this, res);
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }.execute();
                loader.setVisible(true);
            } catch(Exception ex) { MessageDialog.showError("Invalid Input"); }
        });

        JPanel center = new JPanel(); center.setBackground(OPTION_BG); center.add(form);
        card.add(center, BorderLayout.CENTER);
        JPanel bottom = new JPanel(); bottom.setBackground(OPTION_BG); bottom.add(btnSave);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel makeFinalsPanel() {
        JPanel card = UIUtils.createCardPanel();
        styleSubPanel(card);
        card.add(createHeader("Compute Final Grades"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(OPTION_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10); gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> comboSections = new JComboBox<>();
        comboSections.setFont(UI_FONT);
        JComboBox<String> comboStudents = new JComboBox<>();
        comboStudents.setFont(UI_FONT);

        // Async Load Sections
        SwingUtilities.invokeLater(() -> {
            new SwingWorker<List<Section>, Void>() {
                @Override protected List<Section> doInBackground() { return sectionDAO.getSectionsByInstructor(userId); }
                @Override protected void done() {
                    try { for (Section s : get()) comboSections.addItem(s.getSection_id() + " - " + s.getCourse_code()); } catch(Exception e){}
                }
            }.execute();
        });

        comboSections.addActionListener(e -> {
            comboStudents.removeAllItems();
            if(comboSections.getSelectedItem() != null) {
                int secId = Integer.parseInt(((String)comboSections.getSelectedItem()).split(" - ")[0]);
                for(Integer id : enrollmentDAO.getStudentIdsInSection(secId)) comboStudents.addItem(String.valueOf(id));
            }
        });

        addFormRow(form, gbc, 0, "Section:", comboSections);
        addFormRow(form, gbc, 1, "Student:", comboStudents);

        JButton btnCalc = UIUtils.createPrimaryButton("Compute & Publish");
        styleActionButtonGreen(btnCalc);

        btnCalc.addActionListener(e -> {
            if(comboStudents.getSelectedItem() == null) return;
            int secId = Integer.parseInt(((String)comboSections.getSelectedItem()).split(" - ")[0]);
            int stuId = Integer.parseInt((String)comboStudents.getSelectedItem());

            LoadingDialog loader = new LoadingDialog(this, "Computing...");
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() {
                    int enrollId = enrollmentDAO.getEnrollmentId(stuId, secId);
                    return instructorService.computeAndPublishFinalGrade(enrollId);
                }
                @Override protected void done() {
                    loader.dispose();
                    try {
                        String res = get();
                        if(res.startsWith("Success")) MessageDialog.showInfo(InstructorDashboard.this, res);
                        else MessageDialog.showError(InstructorDashboard.this, res);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
            loader.setVisible(true);
        });

        JPanel center = new JPanel(); center.setBackground(OPTION_BG); center.add(form);
        card.add(center, BorderLayout.CENTER);
        JPanel bottom = new JPanel(); bottom.setBackground(OPTION_BG); bottom.add(btnCalc);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel makeStatsPanel() {
        JPanel card = UIUtils.createCardPanel();
        styleSubPanel(card);
        card.add(createHeader("Performance Statistics"), BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        top.setBackground(OPTION_BG);

        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(UI_FONT);

        // Async Load
        SwingUtilities.invokeLater(() -> {
            new SwingWorker<List<Section>, Void>() {
                @Override protected List<Section> doInBackground() { return sectionDAO.getSectionsByInstructor(userId); }
                @Override protected void done() {
                    try { for (Section s : get()) combo.addItem(s.getSection_id() + " - " + s.getCourse_code()); } catch(Exception e){}
                }
            }.execute();
        });

        JButton btn = UIUtils.createPrimaryButton("Generate Report");
        styleActionButtonGreen(btn);

        top.add(new JLabel("Section:")); top.add(combo); top.add(btn);

        JTextArea report = new JTextArea();
        report.setFont(new Font("Monospaced", Font.PLAIN, 13));
        report.setEditable(false);

        btn.addActionListener(e -> {
            if(combo.getSelectedItem() == null) return;
            int secId = Integer.parseInt(((String)combo.getSelectedItem()).split(" - ")[0]);

            LoadingDialog loader = new LoadingDialog(this, "Analyzing...");
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() { return instructorService.generateSectionStatistics(secId); }
                @Override protected void done() {
                    loader.dispose();
                    try { report.setText(get()); } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
            loader.setVisible(true);
        });

        card.add(top, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout()); center.setBackground(OPTION_BG);
        center.add(new JScrollPane(report), BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    // --- Helpers (Same as before) ---
    private void styleSubPanel(JPanel p) {
        p.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(DARK_BORDER, 1, 12),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        p.setBackground(OPTION_BG);
    }
    private void styleActionButtonGreen(JButton b) {
        b.setFont(UI_FONT); b.setBackground(HIGHLIGHT_GREEN); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new RoundedLineBorder(HIGHLIGHT_GREEN.darker(), 1, 10));
    }
    private JPanel createHeader(String text) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(OPTION_BG);
        JLabel l = new JLabel(text); l.setFont(UI_FONT); l.setForeground(DARK_TEXT);
        p.add(l, BorderLayout.NORTH); return p;
    }
    private void addFormRow(JPanel p, GridBagConstraints gbc, int y, String lbl, Component c) {
        JLabel jl = new JLabel(lbl); jl.setFont(UI_FONT); jl.setForeground(DARK_TEXT);
        gbc.gridx = 0; gbc.gridy = y; p.add(jl, gbc);
        gbc.gridx = 1; p.add(c, gbc);
    }
    private void logout() { dispose(); new edu.univ.erp.ui.auth.LoginFrame().setVisible(true); }
    private Icon makeGlyphIcon(String kind, int size) { /* (Keep your existing Icon code here) */ return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)); }
    static class RoundedLineBorder extends AbstractBorder { /* (Keep your existing Border code here) */
        private Color color; private int thickness, radius;
        public RoundedLineBorder(Color c, int t, int r){color=c;thickness=t;radius=r;}
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x+thickness/2, y+thickness/2, width-thickness, height-thickness, radius, radius));
        }
        public Insets getBorderInsets(Component c) { return new Insets(thickness,thickness,thickness,thickness); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InstructorDashboard(new UserSession(0, "Instructor", "Test", "Now")).setVisible(true));
    }
}
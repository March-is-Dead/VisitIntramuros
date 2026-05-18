package visitintramuros.view;

import visitintramuros.controller.MapController;
import visitintramuros.model.Location;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * MainAppScreen — Swing Main App Screen matching the PDF wireframe.
 * Layout: TopBar | Sidebar (search + filters + list + reset) | MapPanel + InfoPanel
 *
 * All 8 key functions from the PDF are wired to interactive controls:
 *   loadMap(), displayMarkers(), selectionLocation(), showLocationInfo(),
 *   navigateMap(), filterLocations(), resetView(), exitApp()
 */
public class MainAppScreen extends JPanel {

    // Colors
    private static final Color DARK_GREEN   = new Color(26,  46,  26);
    private static final Color MED_GREEN    = new Color(30,  51,  32);
    private static final Color GOLD         = new Color(200, 169, 110);
    private static final Color CREAM        = new Color(240, 230, 200);
    private static final Color CREAM_FAINT  = new Color(240, 230, 200, 100);
    private static final Color BG           = new Color(245, 243, 239);
    private static final Color BORDER_COLOR = new Color(220, 215, 205);

    private final JFrame        frame;
    private final MapController controller;
    private       MapPanel      mapPanel;

    // Info panel labels (updated by showLocationInfo)
    private JLabel infoIcon;
    private JLabel infoName;
    private JLabel infoCategory;
    private JLabel infoDesc;
    private JLabel breadcrumb;

    // Coordinates label reference — updated by showLocationInfo()
    private JLabel infoCoords;

    // Sidebar list panel (rebuilt on filter)
    private JPanel     listPanel;
    private JScrollPane listScroll;
    private String    activeFilter = "All";

    public MainAppScreen(JFrame frame) {
        this.frame      = frame;
        this.controller = new MapController();
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
    }

    // ── Build UI ──────────────────────────────────────────────

    private void buildUI() {
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        JPanel centerCol = new JPanel(new BorderLayout());
        centerCol.setBackground(BG);
        centerCol.add(buildToolbar(), BorderLayout.NORTH);

        // Map + info panel stacked vertically
        mapPanel = new MapPanel(controller);
        mapPanel.setOnLocationSelected(loc -> {
            showLocationInfo(loc);
            highlightListItem(loc.getName());
            if (breadcrumb != null)
                breadcrumb.setText("Map View  ›  " + loc.getName());
        });

        centerCol.add(mapPanel,        BorderLayout.CENTER);
        centerCol.add(buildInfoPanel(), BorderLayout.SOUTH);

        add(centerCol, BorderLayout.CENTER);
    }

    // ── TOP BAR ──────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(DARK_GREEN);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Logo
        JPanel logoBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoBox.setOpaque(false);
        JLabel visit = new JLabel("Visit");
        visit.setFont(new Font("Serif", Font.PLAIN, 17));
        visit.setForeground(CREAM);
        JLabel intra = new JLabel("Intramuros");
        intra.setFont(new Font("Serif", Font.BOLD, 17));
        intra.setForeground(GOLD);
        logoBox.add(visit);
        logoBox.add(intra);

        // Right buttons — only exitApp() remains (Profile/Saved removed: not in PDF key functions)
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBox.setOpaque(false);

        // exitApp() button
        JButton exitBtn = makeTopBtn("⬅  Exit App", true);
        exitBtn.addActionListener(e -> {
            controller.exitApp();
            frame.setContentPane(new HomeScreen(frame));
            frame.revalidate();
        });
        rightBox.add(exitBtn);

        bar.add(logoBox,  BorderLayout.WEST);
        bar.add(rightBox, BorderLayout.EAST);
        return bar;
    }

    private JButton makeTopBtn(String text, boolean accent) {
        Color bg = accent ? new Color(60, 40, 10) : new Color(40, 60, 40);
        Color fg = accent ? GOLD : CREAM;
        Color border = accent
            ? new Color(200, 169, 110, 180)
            : new Color(255, 255, 255, 60);

        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Hover highlight
                if (getModel().isRollover()) {
                    g2.setColor(accent
                        ? new Color(200, 169, 110, 60)
                        : new Color(255, 255, 255, 30));
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(false);          // Let our paintComponent handle bg
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 28));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return btn;
    }

    // ── SIDEBAR ───────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(MED_GREEN);
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Search field — filterLocations()
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 11));
        searchField.setBackground(new Color(255, 255, 255, 18));
        searchField.setForeground(CREAM);
        searchField.setCaretColor(GOLD);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JLabel searchLabel = new JLabel("🔍  Search landmarks, plazas...");
        searchLabel.setForeground(new Color(240, 230, 200, 90));
        searchLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        // Placeholder simulation
        searchField.setText("Search landmarks, plazas...");
        searchField.setForeground(new Color(240, 230, 200, 90));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search landmarks, plazas...")) {
                    searchField.setText("");
                    searchField.setForeground(CREAM);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search landmarks, plazas...");
                    searchField.setForeground(new Color(240, 230, 200, 90));
                }
            }
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
            private void applySearch() {
                String q = searchField.getText().toLowerCase();
                if (q.equals("search landmarks, plazas...")) q = "";
                String query = q;
                List<Location> all = controller.getLocations();
                java.util.List<Location> results = new java.util.ArrayList<>();
                for (Location l : all) {
                    if (l.getName().toLowerCase().contains(query)) results.add(l);
                }
                rebuildList(results, "");
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Filter chips — filterLocations()
        JPanel filterRow = buildFilterRow();

        // Section label
        JLabel sectionLabel = new JLabel("LANDMARKS");
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
        sectionLabel.setForeground(new Color(240, 230, 200, 76));
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(8, 14, 4, 14));

        // Landmark list — displayMarkers() + selectionLocation()
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(MED_GREEN);
        rebuildList(controller.getLocations(), null);

        listScroll = new JScrollPane(listPanel);
        listScroll.setBorder(null);
        listScroll.setBackground(MED_GREEN);
        listScroll.getViewport().setBackground(MED_GREEN);
        listScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // resetView() button
        JButton resetBtn = new JButton("↺   Reset View") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getModel().isRollover()
                    ? new Color(255, 255, 255, 25) : new Color(255, 255, 255, 13));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        resetBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        resetBtn.setForeground(new Color(240, 230, 200, 153));
        resetBtn.setOpaque(false);
        resetBtn.setContentAreaFilled(false);
        resetBtn.setBorderPainted(false);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 18)),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        resetBtn.addActionListener(e -> {
            mapPanel.resetView();
            activeFilter = "All";
            rebuildList(controller.getLocations(), null);
            showLocationInfo(null);
            if (breadcrumb != null) breadcrumb.setText("Map View  ›  Intramuros, Manila");
        });

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(searchPanel);
        topSection.add(filterRow);
        topSection.add(sectionLabel);

        sidebar.add(topSection,  BorderLayout.NORTH);
        sidebar.add(listScroll,  BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.add(resetBtn, BorderLayout.CENTER);
        sidebar.add(bottomBar, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));

        String[] categories = {"All", "Historical", "Church", "Museum", "Plaza"};
        ButtonGroup group = new ButtonGroup();

        for (String cat : categories) {
            JToggleButton chip = new JToggleButton(cat);
            chip.setFont(new Font("SansSerif", Font.PLAIN, 10));
            chip.setFocusPainted(false);
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chip.setSelected(cat.equals("All"));
            applyChipStyle(chip, cat.equals("All"));

            chip.addActionListener(e -> {
                activeFilter = cat;
                // filterLocations() — key function
                List<Location> filtered = controller.filterLocations(cat);
                rebuildList(filtered, cat);
                row.getComponents();
                for (Component c : row.getComponents()) {
                    if (c instanceof JToggleButton) applyChipStyle((JToggleButton) c, c == chip);
                }
            });

            group.add(chip);
            row.add(chip);
        }
        return row;
    }

    private void applyChipStyle(JToggleButton chip, boolean active) {
        if (active) {
            chip.setBackground(GOLD);
            chip.setForeground(DARK_GREEN);
            chip.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
            chip.setOpaque(true);
        } else {
            chip.setBackground(new Color(0, 0, 0, 0));
            chip.setForeground(new Color(240, 230, 200, 140));
            chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 46), 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
            chip.setOpaque(false);
        }
    }

    private void rebuildList(List<Location> locs, String activeCategory) {
        listPanel.removeAll();
        boolean first = true;
        for (Location loc : locs) {
            boolean isActive = activeCategory != null && loc.getName().equals(
                controller.getSelectedLocation() != null
                    ? controller.getSelectedLocation().getName() : "");
            listPanel.add(makeLandmarkRow(loc, isActive));
            first = false;
        }
        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel makeLandmarkRow(Location loc, boolean selected) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(true);
        row.setBackground(selected
            ? new Color(200, 169, 110, 26)
            : MED_GREEN);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, selected ? 2 : 0, 0, 0,
                selected ? GOLD : MED_GREEN),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Icon
        JLabel icon = new JLabel(getCatIcon(loc.getCategory()));
        icon.setFont(new Font("SansSerif", Font.BOLD, 13));
        icon.setOpaque(true);
        icon.setBackground(getCatBgColor(loc.getCategory()));
        icon.setForeground(getCatFgColor(loc.getCategory()));
        icon.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // Text
        JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 1));
        textCol.setOpaque(false);
        JLabel nameL = new JLabel(loc.getName());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 11));
        nameL.setForeground(CREAM);
        JLabel catL = new JLabel(loc.getCategory());
        catL.setFont(new Font("SansSerif", Font.PLAIN, 9));
        catL.setForeground(new Color(240, 230, 200, 100));
        textCol.add(nameL);
        textCol.add(catL);

        row.add(icon,    BorderLayout.WEST);
        row.add(textCol, BorderLayout.CENTER);

        // selectionLocation() + showLocationInfo() on click
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Location selected2 = controller.selectLocationByName(loc.getName());
                if (selected2 != null) {
                    showLocationInfo(selected2);
                    mapPanel.setOnLocationSelected(null); // avoid double fire
                    rebuildList(listPanel.getComponentCount() > 0
                        ? controller.filterLocations(activeFilter)
                        : controller.getLocations(), activeFilter);
                    mapPanel.setOnLocationSelected(l -> {
                        showLocationInfo(l);
                        highlightListItem(l.getName());
                        if (breadcrumb != null)
                            breadcrumb.setText("Map View  ›  " + l.getName());
                    });
                    if (breadcrumb != null)
                        breadcrumb.setText("Map View  ›  " + selected2.getName());
                }
            }
            public void mouseEntered(MouseEvent e) {
                if (!selected) row.setBackground(new Color(255, 255, 255, 10));
            }
            public void mouseExited(MouseEvent e) {
                row.setBackground(selected
                    ? new Color(200, 169, 110, 26) : MED_GREEN);
            }
        });

        return row;
    }

    private void highlightListItem(String name) {
        for (Component c : listPanel.getComponents()) {
            if (c instanceof JPanel) {
                JPanel row = (JPanel) c;
                boolean match = false;
                for (Component inner : row.getComponents()) {
                    if (inner instanceof JPanel) {
                        for (Component label : ((JPanel) inner).getComponents()) {
                            if (label instanceof JLabel &&
                                ((JLabel) label).getText().equals(name)) {
                                match = true; break;
                            }
                        }
                    }
                }
                row.setBackground(match
                    ? new Color(200, 169, 110, 26) : MED_GREEN);
                row.setBorder(BorderFactory.createMatteBorder(
                    0, match ? 2 : 0, 0, 0, match ? GOLD : MED_GREEN));
            }
        }
        listPanel.repaint();
    }

    // ── TOOLBAR (navigateMap tools) ───────────────────────────

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));

        breadcrumb = new JLabel("Map View  ›  Intramuros, Manila");
        breadcrumb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        breadcrumb.setForeground(new Color(100, 90, 70));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        // navigateMap() buttons — Pan button removed (map already pans by default via mouse drag)
        JButton filterBtn  = makeToolBtn("⚙  Filter");
        JButton zoomInBtn  = makeToolBtn("＋  Zoom In");
        JButton zoomOutBtn = makeToolBtn("－  Zoom Out");

        // Navigate — zooms and pans the map to the currently selected landmark (navigateMap())
        JButton navBtn = new JButton("▶  Navigate") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                    ? new Color(40, 65, 40) : DARK_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        navBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        navBtn.setForeground(GOLD);
        navBtn.setOpaque(false);
        navBtn.setContentAreaFilled(false);
        navBtn.setBorderPainted(false);
        navBtn.setFocusPainted(false);
        navBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        navBtn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        navBtn.addActionListener(e -> {
            Location selected = controller.getSelectedLocation();
            if (selected != null) {
                // navigateMap() — pan and zoom map to the selected landmark
                mapPanel.navigateTo(selected);
                controller.navigateMap();
                breadcrumb.setText("Navigating to  ›  " + selected.getName());
            } else {
                JOptionPane.showMessageDialog(frame,
                    "Please select a landmark first.",
                    "No Landmark Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        zoomInBtn .addActionListener(e -> mapPanel.zoomIn());
        zoomOutBtn.addActionListener(e -> mapPanel.zoomOut());

        right.add(zoomInBtn);
        right.add(zoomOutBtn);
        right.add(navBtn);

        bar.add(breadcrumb, BorderLayout.WEST);
        bar.add(right,      BorderLayout.EAST);
        return bar;
    }

    private JButton makeToolBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(240, 235, 225));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setForeground(new Color(90, 80, 60));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return btn;
    }

    // ── INFO PANEL — showLocationInfo() ──────────────────────

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        panel.setPreferredSize(new Dimension(0, 120));

        // Icon
        infoIcon = new JLabel("🛡", SwingConstants.CENTER);
        infoIcon.setFont(new Font("SansSerif", Font.PLAIN, 22));
        infoIcon.setBackground(DARK_GREEN);
        infoIcon.setOpaque(true);
        infoIcon.setPreferredSize(new Dimension(54, 54));
        infoIcon.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel iconBox = new JPanel(new BorderLayout());
        iconBox.setOpaque(false);
        iconBox.add(infoIcon, BorderLayout.NORTH);

        // Text body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Name row — landmark name + image buttons side by side
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nameRow.setOpaque(false);

        infoName = new JLabel("Fort Santiago");
        infoName.setFont(new Font("Serif", Font.BOLD, 15));
        infoName.setForeground(new Color(30, 25, 15));
        nameRow.add(infoName);

        // Image buttons — placed right next to the landmark name
        for (int i = 0; i < 2; i++) {
            final Color imgBtnBg = new Color(240, 237, 230);
            JButton imgBtn = new JButton("🖼 " + (i + 1)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover()
                        ? new Color(220, 215, 200) : imgBtnBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            imgBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            imgBtn.setForeground(new Color(80, 70, 55));
            imgBtn.setOpaque(false);
            imgBtn.setContentAreaFilled(false);
            imgBtn.setBorderPainted(false);
            imgBtn.setFocusPainted(false);
            imgBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(3, 7, 3, 7)
            ));
            imgBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int imgNum = i + 1;
            imgBtn.addActionListener(e -> showImagePopup(imgNum));
            nameRow.add(imgBtn);
        }

        infoCategory = new JLabel("HISTORICAL");
        infoCategory.setFont(new Font("SansSerif", Font.BOLD, 9));
        infoCategory.setForeground(DARK_GREEN);
        infoCategory.setBorder(BorderFactory.createEmptyBorder(3, 0, 4, 0));

        infoDesc = new JLabel("<html><body style='width:460px'>"
            + "A citadel built by Spanish conquistador Miguel López de Legazpi (1571). "
            + "One of the most significant historical landmarks in the Philippines."
            + "</body></html>");
        infoDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoDesc.setForeground(new Color(80, 70, 55));

        // Coordinates label — from Marker.coordinates in the class diagram
        infoCoords = new JLabel("📍 14.5944, 120.9717");
        infoCoords.setFont(new Font("SansSerif", Font.PLAIN, 10));
        infoCoords.setForeground(new Color(120, 100, 60));
        infoCoords.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        body.add(nameRow);
        body.add(infoCategory);
        body.add(infoDesc);
        body.add(infoCoords);

        // Action buttons — Save button removed (not in PDF key functions)
        JPanel actions = new JPanel(new GridLayout(1, 1, 0, 6));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(110, 0));

        JButton navBtn = new JButton("▶  Navigate") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                    ? new Color(40, 65, 40) : DARK_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        navBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        navBtn.setForeground(GOLD);
        navBtn.setOpaque(false);
        navBtn.setContentAreaFilled(false);
        navBtn.setBorderPainted(false);
        navBtn.setFocusPainted(false);
        navBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        navBtn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        navBtn.addActionListener(e -> {
            Location sel = controller.getSelectedLocation();
            if (sel != null) {
                mapPanel.navigateTo(sel);
                controller.navigateMap();
                breadcrumb.setText("Navigating to  ›  " + sel.getName());
            } else {
                JOptionPane.showMessageDialog(frame,
                    "Please select a landmark first.",
                    "No Landmark Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        actions.add(navBtn);

        panel.add(iconBox, BorderLayout.WEST);
        panel.add(body,    BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    /**
     * showLocationInfo() — key function from the PDF.
     * Updates the info panel with the selected location's data.
     */
    private void showLocationInfo(Location loc) {
        if (loc == null) {
            infoName.setText("Select a landmark");
            infoCategory.setText("");
            infoDesc.setText("<html><body style='width:460px'>Click any marker on the map or a landmark in the list.</body></html>");
            infoIcon.setText("📍");
            infoCoords.setText("");
            return;
        }
        infoName.setText(loc.getName());
        infoCategory.setText(loc.getCategory().toUpperCase());
        infoDesc.setText("<html><body style='width:460px'>" + loc.getDescription() + "</body></html>");
        infoIcon.setText(getCatIcon(loc.getCategory()));
        // Fix 4 — show coordinates from Marker.coordinates (class diagram)
        infoCoords.setText("📍 " + loc.getCoordinates());

        // Also call LocationInfo.showLocationInfo() (OOP)
        new visitintramuros.model.LocationInfo(
            loc.getName(), loc.getCoordinates(),
            loc.getName(), loc.getDescription(), loc.getImage()
        ).showLocationInfo();

        revalidate();
        repaint();
    }

    /**
     * showImagePopup() — loads and displays the actual landmark image.
     * Images are loaded from the images/ folder relative to the project root.
     * Any image size is automatically scaled to fit the popup window.
     */
    private void showImagePopup(int imgNum) {
        Location sel = controller.getSelectedLocation();
        if (sel == null) {
            JOptionPane.showMessageDialog(frame,
                "Please select a landmark first.",
                "No Landmark Selected",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Build the image file name from the location name
        String imgKey  = getImageKey(sel.getName());
        String imgPath = "images/" + imgKey + "_" + imgNum + ".jpg";

        // Try .jpg first, then .png
        java.io.File imgFile = new java.io.File(imgPath);
        if (!imgFile.exists()) {
            imgPath = "images/" + imgKey + "_" + imgNum + ".png";
            imgFile  = new java.io.File(imgPath);
        }

        if (!imgFile.exists()) {
            JOptionPane.showMessageDialog(frame,
                "Image file not found:\n" + imgPath +
                "\n\nMake sure you have placed the image inside the images/ folder\n" +
                "with the correct file name: " + imgKey + "_" + imgNum + ".jpg",
                "Image Not Found",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Load and scale the image to fit 700x500 popup
        try {
            java.awt.image.BufferedImage original =
                javax.imageio.ImageIO.read(imgFile);

            int maxW = 700, maxH = 500;
            int origW = original.getWidth();
            int origH = original.getHeight();

            // Calculate scaled size maintaining aspect ratio
            double scale = Math.min((double) maxW / origW, (double) maxH / origH);
            int scaledW  = (int) (origW * scale);
            int scaledH  = (int) (origH * scale);

            java.awt.Image scaled = original.getScaledInstance(
                scaledW, scaledH, java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaled);

            // Build popup dialog
            JDialog dialog = new JDialog(frame,
                "📸  " + sel.getName() + "  —  Image " + imgNum, true);
            dialog.setLayout(new BorderLayout(0, 8));
            dialog.getContentPane().setBackground(DARK_GREEN);

            // Image label
            JLabel imgLabel = new JLabel(icon, SwingConstants.CENTER);
            imgLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
            dialog.add(imgLabel, BorderLayout.CENTER);

            // Caption bar at bottom
            JPanel caption = new JPanel(new BorderLayout());
            caption.setBackground(DARK_GREEN);
            caption.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));

            JLabel nameLabel = new JLabel(sel.getName() + "  ·  Image " + imgNum);
            nameLabel.setFont(new Font("Serif", Font.BOLD, 13));
            nameLabel.setForeground(GOLD);

            JLabel catLabel = new JLabel(sel.getCategory().toUpperCase());
            catLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            catLabel.setForeground(new Color(240, 230, 200, 150));

            JButton closeBtn = new JButton("Close");
            closeBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
            closeBtn.setBackground(GOLD);
            closeBtn.setForeground(DARK_GREEN);
            closeBtn.setFocusPainted(false);
            closeBtn.setBorderPainted(false);
            closeBtn.setOpaque(true);
            closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeBtn.addActionListener(e -> dialog.dispose());

            JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 2));
            textCol.setOpaque(false);
            textCol.add(nameLabel);
            textCol.add(catLabel);

            caption.add(textCol,  BorderLayout.CENTER);
            caption.add(closeBtn, BorderLayout.EAST);
            dialog.add(caption, BorderLayout.SOUTH);

            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setResizable(false);
            dialog.setVisible(true);

        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(frame,
                "Could not load image: " + imgPath,
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Converts a landmark name to its image file key.
     * e.g. "Fort Santiago" → "fort_santiago"
     */
    private String getImageKey(String locationName) {
        switch (locationName) {
            case "Fort Santiago":         return "fort_santiago";
            case "San Agustin Church":    return "san_agustin";
            case "Casa Manila":           return "casa_manila";
            case "Manila Cathedral":      return "manila_cathedral";
            case "Plaza Roma":            return "plaza_roma";
            case "Intramuros Walls":      return "intramuros_walls";
            case "Baluarte de San Diego": return "baluarte";
            default: return locationName.toLowerCase().replace(" ", "_");
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private String getCatIcon(String cat) {
        switch (cat) {
            case "Historical": return "🛡";
            case "Church":     return "⛪";
            case "Museum":     return "🏛";
            case "Plaza":      return "🏛";
            default:           return "📍";
        }
    }

    private Color getCatBgColor(String cat) {
        switch (cat) {
            case "Historical": return new Color(200, 169, 110, 46);
            case "Church":     return new Color(90,  160, 120, 46);
            case "Museum":     return new Color(150, 100, 200, 46);
            case "Plaza":      return new Color(80,  140, 200, 46);
            default:           return new Color(200, 169, 110, 46);
        }
    }

    private Color getCatFgColor(String cat) {
        switch (cat) {
            case "Historical": return GOLD;
            case "Church":     return new Color(126, 200, 160);
            case "Museum":     return new Color(184, 141, 224);
            case "Plaza":      return new Color(128, 184, 224);
            default:           return GOLD;
        }
    }
}

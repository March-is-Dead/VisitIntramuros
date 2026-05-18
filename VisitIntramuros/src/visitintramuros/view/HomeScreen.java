package visitintramuros.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * HomeScreen — Swing Home / Title Screen matching the PDF wireframe.
 * Shows: NU badge, "Visit Intramuros" title, subtitle, "Explore" button,
 * decorative cross pattern, and building silhouette at bottom.
 */
public class HomeScreen extends JPanel {

    private static final Color DARK_GREEN  = new Color(26,  46,  26);
    private static final Color GOLD        = new Color(200, 169, 110);
    private static final Color CREAM       = new Color(240, 230, 200);
    private static final Color CREAM_FAINT = new Color(240, 230, 200, 60);

    private final JFrame frame;

    public HomeScreen(JFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setBackground(DARK_GREEN);
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // NU Manila badge
        JLabel badge = new JLabel("NATIONAL UNIVERSITY  ·  MANILA");
        badge.setFont(new Font("SansSerif", Font.PLAIN, 10));
        badge.setForeground(GOLD);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 1, true),
            BorderFactory.createEmptyBorder(4, 14, 4, 14)
        ));
        add(badge, gbc);

        // "Visit" label
        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 0, 0);
        JLabel visitLabel = new JLabel("Visit", SwingConstants.CENTER);
        visitLabel.setFont(new Font("Serif", Font.PLAIN, 58));
        visitLabel.setForeground(CREAM);
        add(visitLabel, gbc);

        // "Intramuros" label
        gbc.gridy++;
        gbc.insets = new Insets(-10, 0, 0, 0);
        JLabel intraLabel = new JLabel("Intramuros", SwingConstants.CENTER);
        intraLabel.setFont(new Font("Serif", Font.BOLD, 58));
        intraLabel.setForeground(GOLD);
        add(intraLabel, gbc);

        // Subtitle
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        JLabel sub = new JLabel("Cultural Navigation  ·  Heritage Discovery", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(240, 230, 200, 120));
        add(sub, gbc);

        // Explore button
        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 0, 0);
        JButton exploreBtn = new JButton("▶   Explore the Walled City");
        exploreBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        exploreBtn.setBackground(GOLD);
        exploreBtn.setForeground(DARK_GREEN);
        exploreBtn.setFocusPainted(false);
        exploreBtn.setBorderPainted(false);
        exploreBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exploreBtn.setPreferredSize(new Dimension(280, 46));
        exploreBtn.setOpaque(true);

        exploreBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                exploreBtn.setBackground(new Color(212, 184, 122));
            }
            public void mouseExited(MouseEvent e) {
                exploreBtn.setBackground(GOLD);
            }
        });

        exploreBtn.addActionListener(e -> {
            frame.setContentPane(new MainAppScreen(frame));
            frame.revalidate();
        });

        add(exploreBtn, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Cross pattern
        g2.setColor(CREAM_FAINT);
        g2.setStroke(new BasicStroke(0.5f));
        int step = 40;
        for (int x = 0; x < getWidth(); x += step) {
            for (int y = 0; y < getHeight(); y += step) {
                g2.drawLine(x - 8, y, x + 8, y);
                g2.drawLine(x, y - 8, x, y + 8);
                g2.fillOval(x - 2, y - 2, 4, 4);
            }
        }

        // Building silhouette at the bottom
        drawSilhouette(g2);

        // Footer text
        g2.setColor(new Color(240, 230, 200, 50));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        String footer = "© VisitIntramuros  ·  BSIT Group 3  ·  National University Manila";
        g2.drawString(footer,
            (getWidth() - fm.stringWidth(footer)) / 2,
            getHeight() - 14
        );
    }

    private void drawSilhouette(Graphics2D g2) {
        int base = getHeight();
        int cx   = getWidth() / 2;
        g2.setColor(new Color(200, 169, 110, 30));

        // Central arch building
        g2.fillRect(cx - 30, base - 100, 60, 100);
        g2.fillArc(cx - 30, base - 130, 60, 60, 0, 180);
        g2.fillRect(cx - 8, base - 145, 16, 20);

        // Left wing
        g2.fillRect(cx - 130, base - 65, 90, 65);
        g2.fillRect(cx - 110, base - 80, 20, 20);

        // Right wing
        g2.fillRect(cx + 40, base - 65, 90, 65);
        g2.fillRect(cx + 90, base - 80, 20, 20);

        // Far left
        g2.fillRect(cx - 230, base - 45, 80, 45);

        // Far right
        g2.fillRect(cx + 150, base - 45, 80, 45);
    }
}

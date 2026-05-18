package visitintramuros.view;

import visitintramuros.controller.MapController;
import visitintramuros.model.Location;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

/**
 * MapPanel — static image map of Intramuros with clickable landmark zones.
 * Draws the real street layout of Intramuros as a painted map.
 * Each landmark has a clickable zone (circle) that triggers showLocationInfo().
 *
 * Functions implemented:
 *   loadMap(), displayMarkers(), selectionLocation(),
 *   navigateMap() (zoom/pan), resetView()
 */
public class MapPanel extends JPanel {

    private static final Color DARK_GREEN   = new Color(26,  46,  26);
    private static final Color GOLD         = new Color(200, 169, 110);
    private static final Color CREAM        = new Color(240, 230, 200);
    private static final Color ROAD_COLOR   = new Color(255, 255, 248);
    private static final Color BLOCK_COLOR  = new Color(210, 202, 185);
    private static final Color BLOCK_ALT    = new Color(196, 188, 170);
    private static final Color PARK_COLOR   = new Color(168, 200, 154);
    private static final Color WALL_COLOR   = new Color(150, 120,  70);
    private static final Color WATER_COLOR  = new Color(160, 200, 220);

    private final MapController    controller;
    private       Consumer<Location> onLocationSelected;
    private       Location         hoveredLocation;
    private       Location         selectedLocation;

    // Zoom and pan state
    private double  zoom      = 1.0;
    private int     offsetX   = 0;
    private int     offsetY   = 0;
    private Point   dragStart;
    private int     dragOffX;
    private int     dragOffY;

    // PIN_RADIUS for clickable hit zones
    private static final int PIN_RADIUS = 14;

    public MapPanel(MapController controller) {
        this.controller = controller;
        setBackground(new Color(232, 226, 212));
        setPreferredSize(new Dimension(700, 460));
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setupMouseListeners();
        controller.loadMap();
        controller.displayMarkers();
    }

    // ── Mouse Listeners ──────────────────────────────────────

    private void setupMouseListeners() {

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // selectionLocation() — check if user clicked near a landmark pin
                Point world = screenToWorld(e.getPoint());
                for (Location loc : controller.getLocations()) {
                    int dx = world.x - loc.getPinX();
                    int dy = world.y - loc.getPinY();
                    if (Math.sqrt(dx * dx + dy * dy) <= PIN_RADIUS + 4) {
                        selectedLocation = controller.selectLocationByName(loc.getName());
                        if (onLocationSelected != null && selectedLocation != null) {
                            onLocationSelected.accept(selectedLocation);
                        }
                        repaint();
                        return;
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                dragOffX  = offsetX;
                dragOffY  = offsetY;
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                // Hover detection
                Point world = screenToWorld(e.getPoint());
                Location prev = hoveredLocation;
                hoveredLocation = null;
                for (Location loc : controller.getLocations()) {
                    int dx = world.x - loc.getPinX();
                    int dy = world.y - loc.getPinY();
                    if (Math.sqrt(dx * dx + dy * dy) <= PIN_RADIUS + 4) {
                        hoveredLocation = loc;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        break;
                    }
                }
                if (hoveredLocation == null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
                if (prev != hoveredLocation) repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Pan — navigateMap()
                if (dragStart != null) {
                    offsetX = dragOffX + (e.getX() - dragStart.x);
                    offsetY = dragOffY + (e.getY() - dragStart.y);
                    controller.navigateMap();
                    repaint();
                }
            }
        });

        // Scroll wheel zoom — navigateMap()
        addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            setZoom(zoom * factor);
        });
    }

    // ── Zoom / Pan API ────────────────────────────────────────

    public void zoomIn()  { setZoom(zoom * 1.2); controller.zoomIn(); }
    public void zoomOut() { setZoom(zoom / 1.2); controller.zoomOut(); }

    /**
     * navigateTo() — called by the Navigate button.
     * Pans and zooms the map to center on the selected landmark. navigateMap()
     */
    public void navigateTo(Location loc) {
        // Center the map on the landmark's pin position with a close zoom
        int targetX = loc.getPinX();
        int targetY = loc.getPinY();
        zoom = 2.2;
        // Offset so the pin is centered in the panel
        offsetX = (int)(getWidth()  / 2 - targetX * zoom);
        offsetY = (int)(getHeight() / 2 - targetY * zoom);
        selectedLocation = loc;
        controller.navigateMap();
        repaint();
    }

    public void resetView() {
        zoom    = 1.0;
        offsetX = 0;
        offsetY = 0;
        selectedLocation = null;
        controller.resetView();
        repaint();
    }

    private void setZoom(double newZoom) {
        zoom = Math.max(0.5, Math.min(3.5, newZoom));
        repaint();
    }

    /** Convert screen pixel to world pixel (accounting for zoom & pan). */
    private Point screenToWorld(Point screen) {
        int wx = (int) ((screen.x - offsetX) / zoom);
        int wy = (int) ((screen.y - offsetY) / zoom);
        return new Point(wx, wy);
    }

    public void setOnLocationSelected(Consumer<Location> listener) {
        this.onLocationSelected = listener;
    }

    // ── Painting ─────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Apply zoom + pan transform
        g2.translate(offsetX, offsetY);
        g2.scale(zoom, zoom);

        // Draw the static Intramuros map
        drawMap(g2);

        // Draw landmark pins — displayMarkers()
        drawMarkers(g2);

        g2.dispose();

        // Draw HUD overlays (not affected by zoom/pan)
        drawHUD(g);
    }

    // ── Map Drawing ───────────────────────────────────────────

    private void drawMap(Graphics2D g2) {
        int W = 700, H = 460;

        // Background (river / moat)
        g2.setColor(WATER_COLOR);
        g2.fillRect(0, 0, W, H);

        // Intramuros wall boundary (filled city area)
        int[] wx = {80, 580, 600, 570, 200, 80, 60};
        int[] wy = {60,  60, 200, 400, 420, 380, 200};
        g2.setColor(new Color(232, 226, 212));
        g2.fillPolygon(wx, wy, wx.length);

        // Wall outline
        g2.setColor(WALL_COLOR);
        g2.setStroke(new BasicStroke(6f));
        g2.drawPolygon(wx, wy, wx.length);

        // ── Streets ──
        g2.setColor(ROAD_COLOR);
        g2.setStroke(new BasicStroke(7f));
        // Horizontal streets
        int[][] hStreets = {{90,510,120},{90,530,200},{90,540,290},{90,545,360}};
        for (int[] s : hStreets) g2.drawLine(s[0], s[2], s[1], s[2]);
        // Vertical streets
        int[][] vStreets = {{110,390,120},{200,400,100},{300,410,100},{420,400,100},{510,390,100}};
        for (int[] s : vStreets) g2.drawLine(s[2], s[0], s[2], s[1]);

        // ── City blocks ──
        g2.setStroke(new BasicStroke(1f));
        int[][][] blocks = {
            {{115,100},{85,115}}, {{205,100},{85,115}}, {{305,100},{90,115}}, {{400,100},{85,115}}, {{490,100},{75,115}},
            {{115,220},{85,65}},  {{205,220},{90,65}},  {{300,220},{95,65}},  {{400,220},{85,65}},  {{490,220},{75,65}},
            {{115,295},{85,60}},  {{205,295},{90,60}},  {{300,295},{95,60}},  {{400,295},{85,60}},  {{490,295},{70,60}},
            {{115,365},{85,50}},  {{205,365},{90,50}},  {{300,365},{95,50}},  {{400,365},{85,50}},
        };
        for (int[][] b : blocks) {
            g2.setColor(BLOCK_COLOR);
            g2.fillRect(b[0][0], b[0][1], b[1][0], b[1][1]);
            g2.setColor(BLOCK_ALT);
            g2.drawRect(b[0][0], b[0][1], b[1][0], b[1][1]);
        }

        // Fort Santiago park (green area)
        g2.setColor(PARK_COLOR);
        g2.fillRoundRect(115, 100, 85, 115, 6, 6);
        g2.setColor(new Color(100, 140, 90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(115, 100, 85, 115, 6, 6);
        g2.setColor(new Color(60, 100, 50, 160));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
        g2.drawString("Fort Santiago", 124, 155);
        g2.drawString("Heritage Park", 124, 166);

        // Plaza Roma (lighter open square)
        g2.setColor(new Color(220, 215, 195));
        g2.fillRect(205, 185, 90, 65);
        g2.setColor(new Color(160, 150, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(205, 185, 90, 65);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
        g2.setColor(new Color(100, 90, 60));
        g2.drawString("Plaza Roma", 215, 220);

        // Compass rose
        drawCompass(g2, 640, 80);

        // Scale bar
        g2.setColor(new Color(80, 70, 50, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.fillRect(95, 430, 100, 4);
        g2.drawString("≈ 500 m", 95, 443);

        // Wall label
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(WALL_COLOR);
        g2.drawString("INTRAMUROS", 250, 50);
        g2.drawString("Walled City, Manila", 240, 62);
    }

    private void drawCompass(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillOval(cx - 18, cy - 18, 36, 36);
        g2.setColor(new Color(150, 130, 90));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(cx - 18, cy - 18, 36, 36);

        // N arrow
        int[] nx = {cx, cx - 5, cx + 5};
        int[] ny = {cy - 14, cy + 2, cy + 2};
        g2.setColor(DARK_GREEN);
        g2.fillPolygon(nx, ny, 3);

        // S arrow
        int[] sx = {cx, cx - 5, cx + 5};
        int[] sy = {cy + 14, cy - 2, cy - 2};
        g2.setColor(new Color(180, 160, 120));
        g2.fillPolygon(sx, sy, 3);

        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
        g2.setColor(DARK_GREEN);
        g2.drawString("N", cx - 3, cy - 16);
    }

    // ── Marker Drawing ────────────────────────────────────────

    private void drawMarkers(Graphics2D g2) {
        List<Location> locs = controller.getLocations();

        for (Location loc : locs) {
            int x = loc.getPinX();
            int y = loc.getPinY();
            boolean isSelected = loc == selectedLocation;
            boolean isHovered  = loc == hoveredLocation;

            Color pinColor = getCategoryColor(loc.getCategory());

            // Pulse ring for selected
            if (isSelected) {
                g2.setColor(new Color(pinColor.getRed(), pinColor.getGreen(), pinColor.getBlue(), 60));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x - 20, y - 20, 40, 40);
            }

            // Outer ring
            g2.setColor(isSelected ? DARK_GREEN : (isHovered ? pinColor.darker() : DARK_GREEN));
            g2.setStroke(new BasicStroke(1f));
            g2.fillOval(x - PIN_RADIUS, y - PIN_RADIUS, PIN_RADIUS * 2, PIN_RADIUS * 2);

            // Inner colored circle
            g2.setColor(isSelected ? GOLD : (isHovered ? pinColor.brighter() : pinColor));
            g2.fillOval(x - (PIN_RADIUS - 3), y - (PIN_RADIUS - 3),
                       (PIN_RADIUS - 3) * 2,  (PIN_RADIUS - 3) * 2);

            // Category symbol
            g2.setColor(DARK_GREEN);
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            String sym = getCategorySymbol(loc.getCategory());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(sym, x - fm.stringWidth(sym) / 2, y + fm.getAscent() / 2 - 1);

            // Label below pin
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            fm = g2.getFontMetrics();
            int lw = fm.stringWidth(loc.getName()) + 10;
            int lx = x - lw / 2;
            int ly = y + PIN_RADIUS + 4;

            // Label background
            g2.setColor(new Color(26, 46, 26, 210));
            g2.fillRoundRect(lx, ly, lw, 14, 4, 4);

            // Label text
            g2.setColor(CREAM);
            g2.drawString(loc.getName(), lx + 5, ly + 11);
        }
    }

    // ── HUD (zoom indicator, legend) ─────────────────────────

    private void drawHUD(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Zoom indicator (bottom-right)
        String zoomText = String.format("Zoom: %.0f%%", zoom * 100);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        int zw = fm.stringWidth(zoomText) + 16;
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRoundRect(getWidth() - zw - 8, getHeight() - 24, zw, 18, 6, 6);
        g2.setColor(new Color(80, 70, 50));
        g2.drawString(zoomText, getWidth() - zw - 1, getHeight() - 10);

        // Map status (top-left)
        g2.setColor(new Color(255, 255, 255, 210));
        g2.fillRoundRect(8, 8, 180, 20, 6, 6);
        g2.setColor(new Color(58, 122, 42));
        g2.fillOval(16, 14, 7, 7);
        g2.setColor(new Color(60, 60, 60));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("Map loaded · 7 landmarks", 28, 22);

        g2.dispose();
    }

    // ── Helpers ───────────────────────────────────────────────

    private Color getCategoryColor(String cat) {
        switch (cat) {
            case "Historical": return GOLD;
            case "Church":     return new Color(126, 200, 160);
            case "Museum":     return new Color(184, 141, 224);
            case "Plaza":      return new Color(128, 184, 224);
            default:           return GOLD;
        }
    }

    private String getCategorySymbol(String cat) {
        switch (cat) {
            case "Historical": return "H";
            case "Church":     return "C";
            case "Museum":     return "M";
            case "Plaza":      return "P";
            default:           return "•";
        }
    }
}

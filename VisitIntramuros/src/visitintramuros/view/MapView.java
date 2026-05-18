package visitintramuros.view;

import visitintramuros.controller.MapController;
import visitintramuros.model.Location;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapView — custom drawn canvas map of Intramuros.
 * Matches the wireframe design with street grid, markers, and legend.
 */
public class MapView extends StackPane {

    private final MapController controller;
    private MarkerClickListener clickListener;

    private Canvas canvas;
    private GraphicsContext gc;

    // Map viewport state
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale   = 1.0;

    // Map dimensions
    private static final double MAP_W = 860;
    private static final double MAP_H = 420;

    // Intramuros reference coords
    private static final double REF_LAT = 14.5895;
    private static final double REF_LNG = 120.9745;
    private static final double LAT_SCALE = 8000;
    private static final double LNG_SCALE = 7000;

    // Marker pixel positions (pre-computed from coordinates)
    private Map<String, double[]> markerPixels = new HashMap<>();

    public interface MarkerClickListener {
        void onMarkerClicked(Location location);
    }

    public MapView(MapController controller) {
        this.controller = controller;
        setStyle("-fx-background-color: #e8e2d4;");
        setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Canvas for drawing the map
        canvas = new Canvas(MAP_W, MAP_H);
        gc     = canvas.getGraphicsContext2D();

        // Pre-compute marker pixel positions
        computeMarkerPositions();

        // Draw the map
        drawMap();

        // Zoom buttons (bottom right)
        VBox zoomBox = buildZoomButtons();
        StackPane.setAlignment(zoomBox, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(zoomBox, new Insets(0, 16, 16, 0));

        // Legend (top right)
        VBox legend = buildLegend();
        StackPane.setAlignment(legend, Pos.TOP_RIGHT);
        StackPane.setMargin(legend, new Insets(12, 12, 0, 0));

        // Wall boundary label (bottom left)
        Label wallLabel = new Label("— intramuros wall boundary");
        wallLabel.setStyle(
            "-fx-text-fill: rgba(80,60,40,0.5);" +
            "-fx-font-size: 9px;" +
            "-fx-font-style: italic;"
        );
        StackPane.setAlignment(wallLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(wallLabel, new Insets(0, 0, 10, 10));

        getChildren().addAll(canvas, legend, zoomBox, wallLabel);

        // Click detection on canvas
        canvas.setOnMouseClicked(e -> handleCanvasClick(e.getX(), e.getY()));
        canvas.setCursor(Cursor.HAND);

        // Resize canvas when parent resizes
        widthProperty().addListener((obs, o, n) -> {
            canvas.setWidth(n.doubleValue());
            drawMap();
        });
        heightProperty().addListener((obs, o, n) -> {
            canvas.setHeight(n.doubleValue());
            drawMap();
        });
    }

    /**
     * Pre-computes pixel positions for each landmark marker.
     */
    private void computeMarkerPositions() {
        for (Location loc : controller.getLocations()) {
            String[] parts = loc.getCoordinates().split(",");
            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());
            double px = (lng - REF_LNG) * LNG_SCALE + MAP_W / 2.0;
            double py = (REF_LAT - lat) * LAT_SCALE + MAP_H / 2.0;
            markerPixels.put(loc.getName(), new double[]{px, py});
        }
    }

    /**
     * Draws the full map: background, grid, walls, blocks, markers.
     */
    private void drawMap() {
        double w = canvas.getWidth()  > 0 ? canvas.getWidth()  : MAP_W;
        double h = canvas.getHeight() > 0 ? canvas.getHeight() : MAP_H;

        gc.clearRect(0, 0, w, h);

        // Background
        gc.setFill(Color.web("#e8e2d4"));
        gc.fillRect(0, 0, w, h);

        // Wall boundary (dashed rectangle)
        double wallX = w * 0.04, wallY = h * 0.06;
        double wallW = w * 0.92, wallH = h * 0.82;
        gc.setStroke(Color.web("#b0a080"));
        gc.setLineWidth(2);
        gc.setLineDashes(8, 5);
        gc.strokeRect(wallX, wallY, wallW, wallH);
        gc.setLineDashes(null);

        // Draw street grid
        drawStreetGrid(gc, wallX, wallY, wallW, wallH);

        // Fort Santiago highlighted block
        double fsX = wallX + wallW * 0.06;
        double fsY = wallY + wallH * 0.08;
        double fsW = wallW * 0.18;
        double fsH = wallH * 0.35;
        gc.setFill(Color.web("#c8d8b0", 0.5));
        gc.fillRect(fsX, fsY, fsW, fsH);
        gc.setStroke(Color.web("#80a060"));
        gc.setLineWidth(1);
        gc.strokeRect(fsX, fsY, fsW, fsH);

        // "Fort Santiago Historical Park" label
        gc.setFill(Color.web("#4a6030"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
        gc.fillText("Fort Santiago", fsX + 4, fsY + 14);
        gc.fillText("Historical Park", fsX + 4, fsY + 24);

        // Draw all landmark markers
        List<Location> locs = controller.getLocations();
        for (Location loc : locs) {
            double[] px = getScaledPixel(loc, w, h);
            drawMarker(gc, px[0], px[1], loc);
        }
    }

    /**
     * Draws city block grid to simulate Intramuros streets.
     */
    private void drawStreetGrid(GraphicsContext gc, double wallX, double wallY,
                                 double wallW, double wallH) {
        gc.setFill(Color.web("#d4cdb8"));
        gc.setStroke(Color.web("#c8c0a8"));
        gc.setLineWidth(0.5);

        // Horizontal blocks
        int rows = 5, cols = 7;
        double blockW = wallW / cols;
        double blockH = wallH / rows;
        double streetW = 6, streetH = 6;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double bx = wallX + c * blockW + streetW / 2;
                double by = wallY + r * blockH + streetH / 2;
                double bw = blockW - streetW;
                double bh = blockH - streetH;
                gc.setFill(Color.web("#ddd8c8"));
                gc.fillRect(bx, by, bw, bh);
                gc.strokeRect(bx, by, bw, bh);
            }
        }
    }

    /**
     * Draws a single landmark marker at the given pixel position.
     */
    private void drawMarker(GraphicsContext gc, double px, double py, Location loc) {
        String cat = loc.getCategory();
        Color fill = getCategoryColorFX(cat);

        // Outer ring
        gc.setFill(Color.web("#f0e6c8", 0.9));
        gc.fillOval(px - 14, py - 14, 28, 28);

        // Inner circle
        gc.setFill(fill);
        gc.fillOval(px - 10, py - 10, 20, 20);

        // Icon letter
        gc.setFill(Color.web("#1a2e1a"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        String letter = cat.substring(0, 1);
        gc.fillText(letter, px - 4, py + 4);

        // Label below marker
        gc.setFill(Color.web("#1a2e1a"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
        String name = loc.getName();
        if (name.length() > 12) name = name.substring(0, 11) + "..";
        gc.fillText(name, px - (name.length() * 2.8), py + 20);
    }

    /**
     * Converts a location's lat/lng to scaled canvas pixel coordinates.
     */
    private double[] getScaledPixel(Location loc, double w, double h) {
        String[] parts = loc.getCoordinates().split(",");
        double lat = Double.parseDouble(parts[0].trim());
        double lng = Double.parseDouble(parts[1].trim());
        double px = (lng - REF_LNG) * LNG_SCALE * scale + w / 2.0 + offsetX;
        double py = (REF_LAT - lat) * LAT_SCALE * scale + h / 2.0 + offsetY;
        return new double[]{px, py};
    }

    /**
     * Handles click on canvas — checks if a marker was clicked.
     */
    private void handleCanvasClick(double mx, double my) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        for (Location loc : controller.getLocations()) {
            double[] px = getScaledPixel(loc, w, h);
            double dist = Math.sqrt(Math.pow(mx - px[0], 2) + Math.pow(my - px[1], 2));
            if (dist <= 14) {
                Location selected = controller.selectLocationByName(loc.getName());
                if (selected != null && clickListener != null) {
                    clickListener.onMarkerClicked(selected);
                }
                // Highlight selected marker
                drawMap();
                highlightMarker(px[0], px[1], loc);
                return;
            }
        }
    }

    /**
     * Draws a highlight ring around the selected marker.
     */
    private void highlightMarker(double px, double py, Location loc) {
        gc.setStroke(Color.web("#c8a96e"));
        gc.setLineWidth(3);
        gc.strokeOval(px - 16, py - 16, 32, 32);
    }

    /**
     * Builds the zoom in/out/locate buttons.
     */
    private VBox buildZoomButtons() {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);

        Button zoomIn  = makeZoomBtn("+");
        Button zoomOut = makeZoomBtn("−");
        Button locate  = makeZoomBtn("⊕");

        zoomIn.setOnAction(e -> {
            scale = Math.min(scale * 1.3, 4.0);
            drawMap();
        });
        zoomOut.setOnAction(e -> {
            scale = Math.max(scale / 1.3, 0.5);
            drawMap();
        });
        locate.setOnAction(e -> resetView());

        box.getChildren().addAll(zoomIn, zoomOut, locate);
        return box;
    }

    private Button makeZoomBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: #1a2e1a;" +
            "-fx-text-fill: #c8a96e;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 32px;" +
            "-fx-min-height: 32px;" +
            "-fx-max-width: 32px;" +
            "-fx-max-height: 32px;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }

    /**
     * Builds the legend panel.
     */
    private VBox buildLegend() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(8, 12, 8, 12));
        box.setStyle(
            "-fx-background-color: rgba(26,46,26,0.88);" +
            "-fx-background-radius: 8;"
        );

        Label title = new Label("LEGEND");
        title.setStyle("-fx-text-fill: #f0e6c8; -fx-font-size: 9px; -fx-font-weight: bold;");
        box.getChildren().add(title);

        addLegendItem(box, "#c8a96e", "Selected site");
        addLegendItem(box, "#7ec8a0", "Church");
        addLegendItem(box, "#b88de0", "Museum");
        addLegendItem(box, "#80b8e0", "Plaza");
        addLegendItem(box, "#c8c0a8", "Wall boundary");

        return box;
    }

    private void addLegendItem(VBox box, String color, String label) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px;");
        Label text = new Label(label);
        text.setStyle("-fx-text-fill: rgba(240,230,200,0.8); -fx-font-size: 10px;");
        row.getChildren().addAll(dot, text);
        box.getChildren().add(row);
    }

    // ── Public API ────────────────────────────────────────────

    public void panToLocation(Location loc) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double[] px = getScaledPixel(loc, w, h);
        offsetX += (w / 2.0 - px[0]);
        offsetY += (h / 2.0 - px[1]);
        drawMap();
        double[] newPx = getScaledPixel(loc, w, h);
        highlightMarker(newPx[0], newPx[1], loc);
    }

    public void zoomIn() {
        scale = Math.min(scale * 1.3, 4.0);
        drawMap();
    }

    public void zoomOut() {
        scale = Math.max(scale / 1.3, 0.5);
        drawMap();
    }

    public void resetView() {
        scale   = 1.0;
        offsetX = 0;
        offsetY = 0;
        drawMap();
    }

    public void filterByCategory(String category) {
        drawMap(); // future: filter visible markers
    }

    public void setMarkerClickListener(MarkerClickListener listener) {
        this.clickListener = listener;
    }

    private Color getCategoryColorFX(String category) {
        switch (category) {
            case "Historical": return Color.web("#c8a96e");
            case "Church":     return Color.web("#7ec8a0");
            case "Museum":     return Color.web("#b88de0");
            case "Plaza":      return Color.web("#80b8e0");
            default:           return Color.web("#c8a96e");
        }
    }
}
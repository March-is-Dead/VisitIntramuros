package visitintramuros.model;

/**
 * UIControls — from the VisitIntramuros class diagram.
 * Extends MapElement. Manages zoom level and filter state.
 *
 * OOP Concept: Inheritance (extends MapElement), Encapsulation.
 */
public class UIControls extends MapElement {

    private int    zoomLevel;
    private String filterCategory;

    public UIControls(String identifier, String position) {
        super(identifier, position);
        this.zoomLevel      = 1;
        this.filterCategory = "All";
    }

    public void zoomIn()  {
        if (zoomLevel < 3) { zoomLevel++; }
        System.out.println("Zoomed in  — level: " + zoomLevel);
    }

    public void zoomOut() {
        if (zoomLevel > 1) { zoomLevel--; }
        System.out.println("Zoomed out — level: " + zoomLevel);
    }

    public void panMap() { System.out.println("Panning map..."); }

    public void resetView() {
        zoomLevel      = 1;
        filterCategory = "All";
        System.out.println("View reset to default.");
    }

    public void filterLocations(String category) {
        this.filterCategory = category;
        System.out.println("Filter applied: " + category);
    }

    public int    getZoomLevel()       { return zoomLevel; }
    public String getFilterCategory()  { return filterCategory; }
    public void   setZoomLevel(int z)  { this.zoomLevel = z; }
}

package visitintramuros.controller;

import visitintramuros.interfaces.Navigable;
import visitintramuros.interfaces.Selectable;
import visitintramuros.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapController — from the VisitIntramuros class diagram.
 * Implements Navigable and Selectable interfaces.
 * Central controller: manages locations, markers, filtering, zoom, selection.
 *
 * OOP Concept: Abstraction via interfaces, manages Location + Marker objects.
 * All 8 key functions from the PDF are implemented here.
 */
public class MapController implements Navigable, Selectable {

    private String         baseMap;
    private List<Location> locations;
    private List<Marker>   markers;
    private int            defaultZoom;
    private String         defaultPosition;
    private Location       selectedLocation;
    private UIControls     uiControls;

    public MapController() {
        this.baseMap         = "intramuros_map.png";
        this.locations       = new ArrayList<>();
        this.markers         = new ArrayList<>();
        this.defaultZoom     = 1;
        this.defaultPosition = "14.5895,120.9745";
        this.uiControls      = new UIControls("ui-controls", defaultPosition);
        initializeLocations();
    }

    /**
     * Populates all Intramuros landmark data.
     * pinX / pinY = pixel position on the 700x500 static map image.
     */
    private void initializeLocations() {

        // Fort Santiago
        Location fort = new Location(
            "Fort Santiago",
            "A citadel built by Spanish conquistador Miguel López de Legazpi "
            + "(1571). One of the most significant historical landmarks in the "
            + "Philippines, used as a military defense during the Spanish colonial period.",
            "fort_santiago.jpg", "Historical", "14.5944, 120.9717", 168, 148
        );
        locations.add(fort);
        markers.add(new HistoricalMarker(
            "mk-fort", "168,148", "14.5944,120.9717",
            fort.getName(), fort.getDescription()));

        // San Agustin Church
        Location agustin = new Location(
            "San Agustin Church",
            "The oldest stone church in the Philippines, completed in 1607. "
            + "A UNESCO World Heritage Site renowned for its Baroque architecture "
            + "and role in Philippine colonial history.",
            "san_agustin.jpg", "Church", "14.5888, 120.9752", 310, 248
        );
        locations.add(agustin);
        markers.add(new CulturalMarker(
            "mk-agustin", "310,248", "14.5888,120.9752",
            agustin.getName(), agustin.getDescription()));

        // Casa Manila
        Location casa = new Location(
            "Casa Manila",
            "A museum complex that recreates a 19th-century colonial Filipino home. "
            + "Features period furniture, paintings, and artifacts illustrating "
            + "life during the Spanish colonial era.",
            "casa_manila.jpg", "Museum", "14.5886, 120.9756", 355, 260
        );
        locations.add(casa);
        markers.add(new CulturalMarker(
            "mk-casa", "355,260", "14.5886,120.9756",
            casa.getName(), casa.getDescription()));

        // Manila Cathedral
        Location cathedral = new Location(
            "Manila Cathedral",
            "The Cathedral-Basilica of the Immaculate Conception — seat of the "
            + "Archdiocese of Manila. Rebuilt multiple times after wars and "
            + "earthquakes; the present structure dates to 1958.",
            "manila_cathedral.jpg", "Church", "14.5910, 120.9742", 248, 198
        );
        locations.add(cathedral);
        markers.add(new CulturalMarker(
            "mk-cathedral", "248,198", "14.5910,120.9742",
            cathedral.getName(), cathedral.getDescription()));

        // Plaza Roma
        Location plaza = new Location(
            "Plaza Roma",
            "The central plaza of Intramuros, formerly known as Plaza Mayor. "
            + "It served as the civic and commercial center during the Spanish "
            + "colonial period, flanked by the Cathedral and government buildings.",
            "plaza_roma.jpg", "Plaza", "14.5907, 120.9745", 262, 208
        );
        locations.add(plaza);
        markers.add(new HistoricalMarker(
            "mk-plaza", "262,208", "14.5907,120.9745",
            plaza.getName(), plaza.getDescription()));

        // Intramuros Walls
        Location walls = new Location(
            "Intramuros Walls",
            "Massive stone walls and bastions surrounding Intramuros, built by the "
            + "Spanish to defend Manila from attacks. Originally about 4.5 km in "
            + "perimeter, they remain largely intact today.",
            "intramuros_walls.jpg", "Historical", "14.5895, 120.9730", 120, 300
        );
        locations.add(walls);
        markers.add(new HistoricalMarker(
            "mk-walls", "120,300", "14.5895,120.9730",
            walls.getName(), walls.getDescription()));

        // Baluarte de San Diego
        Location baluarte = new Location(
            "Baluarte de San Diego",
            "A circular bastion that was part of the original Intramuros "
            + "fortification system. Now hosts a sunken garden and archaeological "
            + "remains from the colonial era.",
            "baluarte.jpg", "Historical", "14.5870, 120.9720", 145, 360
        );
        locations.add(baluarte);
        markers.add(new HistoricalMarker(
            "mk-baluarte", "145,360", "14.5870,120.9720",
            baluarte.getName(), baluarte.getDescription()));
    }

    // ── 8 Key Functions from the PDF ──────────────────────────

    /** loadMap() — initializes the interactive map interface. */
    public void loadMap() {
        System.out.println("[MapController] loadMap() — Intramuros map initialized.");
    }

    /** displayMarkers() — places markers on the map. */
    public void displayMarkers() {
        System.out.println("[MapController] displayMarkers() — " + markers.size() + " markers placed.");
        for (Marker m : markers) m.displayInfo();
    }

    /** selectionLocation() / selectLocation() — implements Selectable. */
    @Override
    public void selectLocation() {
        if (selectedLocation != null)
            System.out.println("[MapController] selectLocation() — " + selectedLocation.getName());
    }

    /** selectLocationByName() — resolves a name to a Location object. */
    public Location selectLocationByName(String name) {
        for (Location loc : locations) {
            if (loc.getName().equals(name)) {
                selectedLocation = loc;
                // trigger Selectable + Marker.onSelect()
                selectLocation();
                for (Marker m : markers) {
                    if (m.getLocationName().equals(name)) m.onSelect();
                }
                return loc;
            }
        }
        return null;
    }

    /** navigateMap() — implements Navigable interface. */
    @Override
    public void navigateMap() {
        System.out.println("[MapController] navigateMap() — zoom level: " + uiControls.getZoomLevel());
    }

    /** filterLocations() — filters by category, returns matching list. */
    public List<Location> filterLocations(String category) {
        uiControls.filterLocations(category);
        if (category.equalsIgnoreCase("All")) return locations;
        return locations.stream()
            .filter(loc -> loc.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }

    /** resetView() — resets map and UI to defaults. */
    public void resetView() {
        uiControls.resetView();
        selectedLocation = null;
        System.out.println("[MapController] resetView() — map returned to default.");
    }

    /** exitApp() — signals return to home screen. */
    public void exitApp() {
        System.out.println("[MapController] exitApp() — returning to Home screen.");
    }

    /** zoomIn() / zoomOut() — delegated to UIControls. */
    public void zoomIn()  { uiControls.zoomIn(); }
    public void zoomOut() { uiControls.zoomOut(); }
    public int  getZoomLevel() { return uiControls.getZoomLevel(); }

    // Getters
    public List<Location> getLocations()       { return locations; }
    public List<Marker>   getMarkers()         { return markers; }
    public Location       getSelectedLocation(){ return selectedLocation; }
    public String         getBaseMap()         { return baseMap; }
}

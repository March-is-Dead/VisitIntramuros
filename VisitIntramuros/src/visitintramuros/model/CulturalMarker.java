package visitintramuros.model;

/**
 * CulturalMarker — from the VisitIntramuros class diagram.
 * Extends Marker. Represents cultural sites (churches, museums, plazas).
 *
 * OOP Concept: Polymorphism — overrides displayInfo() and onSelect().
 */
public class CulturalMarker extends Marker {

    public CulturalMarker(String identifier, String position,
                          String coordinates, String locationName, String description) {
        super(identifier, position, coordinates, locationName, description);
    }

    @Override
    public void displayInfo() {
        System.out.println("[Cultural] " + getLocationName());
        System.out.println("  Coords : " + getCoordinates());
        System.out.println("  Info   : " + getDescription());
    }

    @Override
    public void onSelect() {
        System.out.println("Cultural marker selected: " + getLocationName());
    }
}

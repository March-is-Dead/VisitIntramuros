package visitintramuros.model;

/**
 * Abstract Marker — from the VisitIntramuros class diagram.
 * Extends MapElement. Represents a map pin for a tourist location.
 * Extended by: HistoricalMarker, CulturalMarker.
 *
 * OOP Concept: Inheritance + Polymorphism
 */
public abstract class Marker extends MapElement {

    private String coordinates;
    private String locationName;
    private String description;

    public Marker(String identifier, String position,
                  String coordinates, String locationName, String description) {
        super(identifier, position);
        this.coordinates  = coordinates;
        this.locationName = locationName;
        this.description  = description;
    }

    /** Polymorphic — each subclass renders its own info. */
    public abstract void displayInfo();

    /** Polymorphic — each subclass handles its own selection behavior. */
    public abstract void onSelect();

    public String getCoordinates()               { return coordinates; }
    public void   setCoordinates(String c)       { this.coordinates = c; }
    public String getLocationName()              { return locationName; }
    public void   setLocationName(String name)   { this.locationName = name; }
    public String getDescription()               { return description; }
    public void   setDescription(String desc)    { this.description = desc; }
}

package visitintramuros.model;

/**
 * LocationInfo — from the VisitIntramuros class diagram.
 * Extends MapElement. Holds and displays detail info for a selected Location.
 *
 * OOP Concept: Inheritance (extends MapElement), Composition (holds Location data).
 */
public class LocationInfo extends MapElement {

    private String locationName;
    private String details;
    private String image;

    public LocationInfo(String identifier, String position,
                        String locationName, String details, String image) {
        super(identifier, position);
        this.locationName = locationName;
        this.details      = details;
        this.image        = image;
    }

    /** showLocationInfo() — key function from the PDF. */
    public void showLocationInfo() {
        System.out.println("=== Location Info ===");
        System.out.println("Name   : " + locationName);
        System.out.println("Details: " + details);
        System.out.println("Image  : " + image);
    }

    public void displayInfo() { showLocationInfo(); }

    public String getLocationName()              { return locationName; }
    public void   setLocationName(String name)   { this.locationName = name; }
    public String getDetails()                   { return details; }
    public void   setDetails(String details)     { this.details = details; }
    public String getImage()                     { return image; }
    public void   setImage(String image)         { this.image = image; }
}

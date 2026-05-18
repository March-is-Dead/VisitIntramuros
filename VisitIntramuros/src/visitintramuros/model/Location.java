package visitintramuros.model;

/**
 * Location — from the VisitIntramuros class diagram.
 * Represents one tourist destination inside Intramuros.
 *
 * OOP Concept: Encapsulation — all fields private,
 * accessed only through getters and setters.
 */
public class Location {

    private String name;
    private String description;
    private String image;
    private String category;
    private String coordinates;

    // Map pin position (pixel x,y on the static map image)
    private int pinX;
    private int pinY;

    public Location(String name, String description, String image,
                    String category, String coordinates, int pinX, int pinY) {
        this.name        = name;
        this.description = description;
        this.image       = image;
        this.category    = category;
        this.coordinates = coordinates;
        this.pinX        = pinX;
        this.pinY        = pinY;
    }

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getImage()       { return image; }
    public String getCategory()    { return category; }
    public String getCoordinates() { return coordinates; }
    public int    getPinX()        { return pinX; }
    public int    getPinY()        { return pinY; }

    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setImage(String image)             { this.image = image; }
    public void setCategory(String category)       { this.category = category; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }
    public void setPinX(int pinX)                  { this.pinX = pinX; }
    public void setPinY(int pinY)                  { this.pinY = pinY; }

    @Override
    public String toString() { return name + " [" + category + "]"; }
}

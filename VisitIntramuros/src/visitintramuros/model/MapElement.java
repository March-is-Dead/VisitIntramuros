package visitintramuros.model;

/**
 * Abstract base class MapElement — from the VisitIntramuros class diagram.
 * Shared properties: position, visibility, identifier.
 * Extended by: Marker, LocationInfo, UIControls.
 *
 * OOP Concept: Abstraction + Inheritance
 */
public abstract class MapElement {

    private String  position;
    private boolean visibility;
    private String  identifier;

    public MapElement(String identifier, String position) {
        this.identifier = identifier;
        this.position   = position;
        this.visibility = true;
    }

    public String  getPosition()               { return position; }
    public void    setPosition(String p)        { this.position = p; }
    public boolean isVisible()                  { return visibility; }
    public void    setVisibility(boolean v)     { this.visibility = v; }
    public String  getIdentifier()              { return identifier; }
    public void    setIdentifier(String id)     { this.identifier = id; }
}

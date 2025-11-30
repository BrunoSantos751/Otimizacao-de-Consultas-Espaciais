// Entry.java
public class Entry {
    public Point point;     // non-null for leaf entries
    public Rectangle mbr;   // bounding rectangle
    public Node child;      // non-null for non-leaf entries

    // leaf entry from point
    public Entry(Point p) {
        this.point = p;
        this.mbr = new Rectangle(p);
        this.child = null;
    }

    // node entry
    public Entry(Node child) {
        this.point = null;
        this.child = child;
        this.mbr = child.mbr.copy();
    }
}

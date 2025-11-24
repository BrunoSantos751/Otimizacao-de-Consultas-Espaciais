// QuadTree.java
import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    private static final int CAPACITY = 8;
    private static final int MAX_DEPTH = 12;

    private final Rectangle boundary;
    private final List<Point> points = new ArrayList<>();
    private boolean subdivided = false;
    private int depth = 0;

    private QuadTree nw, ne, sw, se;

    public QuadTree(Rectangle boundary) {
        this.boundary = boundary;
    }

    private QuadTree(Rectangle boundary, int depth) {
        this.boundary = boundary;
        this.depth = depth;
    }

    public void insert(Point p) {
        if (!contains(boundary, p)) return;

        if (!subdivided && (points.size() < CAPACITY || depth >= MAX_DEPTH)) {
            points.add(p);
            return;
        }

        if (!subdivided) subdivide();

        if (contains(nw.boundary, p)) nw.insert(p);
        else if (contains(ne.boundary, p)) ne.insert(p);
        else if (contains(sw.boundary, p)) sw.insert(p);
        else if (contains(se.boundary, p)) se.insert(p);
        else {
            // Edge case: if point lies exactly on a boundary, keep here
            points.add(p);
        }
    }

    public List<Point> rangeQuery(Rectangle range) {
        List<Point> found = new ArrayList<>();
        rangeQuery(range, found);
        return found;
    }

    private void rangeQuery(Rectangle range, List<Point> found) {
        if (!intersects(boundary, range)) return;

        for (Point p : points) {
            if (p.x >= range.minX && p.x <= range.maxX && p.y >= range.minY && p.y <= range.maxY) {
                found.add(p);
            }
        }

        if (subdivided) {
            nw.rangeQuery(range, found);
            ne.rangeQuery(range, found);
            sw.rangeQuery(range, found);
            se.rangeQuery(range, found);
        }
    }

    private void subdivide() {
        subdivided = true;
        double midX = (boundary.minX + boundary.maxX) / 2.0;
        double midY = (boundary.minY + boundary.maxY) / 2.0;

        nw = new QuadTree(new Rectangle(boundary.minX, midY, midX, boundary.maxY), depth + 1);
        ne = new QuadTree(new Rectangle(midX, midY, boundary.maxX, boundary.maxY), depth + 1);
        sw = new QuadTree(new Rectangle(boundary.minX, boundary.minY, midX, midY), depth + 1);
        se = new QuadTree(new Rectangle(midX, boundary.minY, boundary.maxX, midY), depth + 1);

        // redistribute
        List<Point> old = new ArrayList<>(points);
        points.clear();
        for (Point p : old) insert(p);
    }

    private boolean contains(Rectangle r, Point p) {
        return p.x >= r.minX && p.x <= r.maxX && p.y >= r.minY && p.y <= r.maxY;
    }

    private boolean intersects(Rectangle a, Rectangle b) {
        return !(a.maxX < b.minX || a.maxY < b.minY || a.minX > b.maxX || a.minY > b.maxY);
    }
}

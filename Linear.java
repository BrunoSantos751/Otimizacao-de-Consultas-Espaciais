import java.util.ArrayList;
import java.util.List;

public class Linear {
    private final List<Point> points = new ArrayList<>();

    public void insert(Point p) {
        points.add(p);
    }

    public List<Point> rangeQuery(Rectangle r) {
        List<Point> out = new ArrayList<>();
        for (Point p : points) {
            if (p.x >= r.minX && p.x <= r.maxX && p.y >= r.minY && p.y <= r.maxY) out.add(p);
        }
        return out;
    }
}

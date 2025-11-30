public class Point {
    public final int id;
    public final double x, y;

    public Point(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point(" + id + "," + x + "," + y + ")";
    }
}

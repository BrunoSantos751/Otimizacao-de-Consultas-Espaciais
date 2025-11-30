public class Rectangle {
    public double minX, minY, maxX, maxY;

    public Rectangle(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public Rectangle(Point p) {
        this.minX = p.x;
        this.maxX = p.x;
        this.minY = p.y;
        this.maxY = p.y;
    }

    public void expandToInclude(Rectangle r) {
        if (r == null) return;
        this.minX = Math.min(this.minX, r.minX);
        this.minY = Math.min(this.minY, r.minY);
        this.maxX = Math.max(this.maxX, r.maxX);
        this.maxY = Math.max(this.maxY, r.maxY);
    }

    public double area() {
        return Math.max(0.0, (maxX - minX)) * Math.max(0.0, (maxY - minY));
    }

    public Rectangle copy() {
        return new Rectangle(minX, minY, maxX, maxY);
    }

    public double enlargement(Rectangle r) {
        Rectangle c = this.copy();
        c.expandToInclude(r);
        return c.area() - this.area();
    }
}

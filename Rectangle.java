package caseStudy;

public class Rectangle {
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;

    public Rectangle(double minX, double minY, double maxX, double maxY) {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Invalid rectangle boundaries");
        }
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public double area() {
        return (maxX - minX) * (maxY - minY);
    }

    public boolean intersects(Rectangle other) {
        return !(maxX < other.minX ||
                 minX > other.maxX ||
                 maxY < other.minY ||
                 minY > other.maxY);
    }

    public boolean containsPoint(double x, double y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public Rectangle union(Rectangle other) {
        return new Rectangle(
            Math.min(minX, other.minX),
            Math.min(minY, other.minY),
            Math.max(maxX, other.maxX),
            Math.max(maxY, other.maxY)
        );
    }

    public double enlargement(Rectangle other) {
        return union(other).area() - area();
    }

    public double getMinX() { return minX; }
    public double getMinY() { return minY; }
    public double getMaxX() { return maxX; }
    public double getMaxY() { return maxY; }
}
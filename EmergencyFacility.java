

public class EmergencyFacility {
    private String id;
    private String name;
    private String type;
    private double x;
    private double y;

    public EmergencyFacility(String id, String name, String type, double x, double y) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public Rectangle getMBR() {
        return new Rectangle(x, y, x, y);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }

    public double distanceTo(double targetX, double targetY) {
        double dx = this.x - targetX;
        double dy = this.y - targetY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void printInfo() {
        System.out.println(name + " [" + id + "] - " + type + " at (" + x + ", " + y + ")");
    }
}
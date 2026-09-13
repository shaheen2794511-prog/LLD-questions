public class Horse {

    private final String name;
    private final double baseSpeed;
    private double position;

    public Horse(String name, double baseSpeed) {
        this.name = name;
        this.baseSpeed = baseSpeed;
        this.position = 0;
    }

    public void advance(double distance) {
        position += distance;
    }

    public String getName() {
        return name;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public double getPosition() {
        return position;
    }
}

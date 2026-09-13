public class Horse {

    private final String name;
    private final double baseSpeed;
    private double position;

    private MovementStrategy movementStrategy;

    public Horse(
            String name,
            double baseSpeed,
            MovementStrategy movementStrategy) {

        this.name = name;
        this.baseSpeed = baseSpeed;
        this.movementStrategy = movementStrategy;
        this.position = 0;
    }

    public void run() {

        double distance =
                movementStrategy.calculateMovement(this);

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

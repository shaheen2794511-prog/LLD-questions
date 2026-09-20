public class Horse {

    private final String name;
    private double position;

    private MovementStrategy movementStrategy;

    public Horse(
            String name,
            MovementStrategy movementStrategy) {

        this.name = name;
        this.movementStrategy = movementStrategy;
        this.position = 0;
    }

    public void advance() {

        double distance =
                movementStrategy.calculateStep();

        position += distance;
    }

    public String getName() {
        return name;
    }

    public double getPosition() {
        return position;
    }

    public void reset() {
        position = 0;
    }

    public void setMovementStrategy(
            MovementStrategy movementStrategy) {

        this.movementStrategy = movementStrategy;
    }
}

import java.util.Random;

public class RandomMovementStrategy
        implements MovementStrategy {

    private final double speed;
    private final Random random = new Random();

    public RandomMovementStrategy(double speed) {
        this.speed = speed;
    }

    @Override
    public double calculateStep() {

        return random.nextDouble() * speed;
    }
}

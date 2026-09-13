import java.util.Random;

public class RandomMovementStrategy
        implements MovementStrategy {

    private final Random random = new Random();

    @Override
    public double calculateMovement(Horse horse) {

        return random.nextDouble()
                * horse.getBaseSpeed();
    }
}

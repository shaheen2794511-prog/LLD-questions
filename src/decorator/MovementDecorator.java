public interface MovementStrategy {

    double calculateMovement(Horse horse);
}
public abstract class MovementDecorator
        implements MovementStrategy {

    protected final MovementStrategy strategy;

    protected MovementDecorator(
            MovementStrategy strategy) {

        this.strategy = strategy;
    }
}

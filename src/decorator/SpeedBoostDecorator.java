public class SpeedBoostDecorator
        extends MovementDecorator {

    public SpeedBoostDecorator(
            MovementStrategy strategy) {

        super(strategy);
    }

    @Override
    public double calculateMovement(
            Horse horse) {

        return strategy.calculateMovement(horse)
                * 1.20;
    }
}

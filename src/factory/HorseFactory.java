public interface HorseFactory {

    Horse createHorse(
            String name,
            double speed);
} 
public class DefaultHorseFactory
        implements HorseFactory {

    @Override
    public Horse createHorse(
            String name,
            double speed) {

        return new Horse(
                name,
                speed,
                new RandomMovementStrategy());
    }
}

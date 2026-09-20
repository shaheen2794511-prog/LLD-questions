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
HorseFactory factory = new DefaultHorseFactory();

Horse horse1 =
    factory.createHorse("Daisy", 2.5);

Horse horse2 =
    factory.createHorse("Thunder", 3.1);

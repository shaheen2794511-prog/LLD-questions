public class NotStartedState
        implements RaceState {

    @Override
    public void start(Race race) {

        race.setState(new RunningState());

        System.out.println(
                "Starting race...");
    }

    @Override
    public void tick(Race race) {

        throw new IllegalStateException(
                "Race has not started");
    }

    @Override
    public void finish(Race race) {

        throw new IllegalStateException(
                "Race has not started");
    }
}

public class NotStartedState
        implements RaceState {

    @Override
    public void start(Race race) {
        race.setState(new RunningState());
    }

    @Override
    public void tick(Race race) {
        throw new IllegalStateException(
                "Race has not started");
    }

    @Override
    public void finish(Race race) {
        throw new IllegalStateException();
    }

    @Override
    public void cancel(Race race) {
        race.setState(new CancelledState());
    }
}

public class RunningState
        implements RaceState {

    @Override
    public void start(Race race) {
        throw new IllegalStateException(
                "Race already started");
    }

    @Override
    public void tick(Race race) {
        // Allow horses to run
    }

    @Override
    public void finish(Race race) {
        race.setState(new FinishedState());
    }

    @Override
    public void cancel(Race race) {
        race.setState(new CancelledState());
    }
}

package state;

public class FinishedState implements RaceState {

    @Override
    public void start(Race race) {
        throw new IllegalStateException(
                "Race has already finished");
    }

    @Override
    public void runTick(Race race) {
        throw new IllegalStateException(
                "Race has already finished");
    }

    @Override
    public void finish(Race race) {
        throw new IllegalStateException(
                "Race has already finished");
    }

    @Override
    public void cancel(Race race) {
        throw new IllegalStateException(
                "Finished race cannot be cancelled");
    }
}

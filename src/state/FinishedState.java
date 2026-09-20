public class FinishedState
        implements RaceState {

    @Override
    public void start(Race race) {

        throw new IllegalStateException(
                "Race already finished");
    }

    @Override
    public void tick(Race race) {

        throw new IllegalStateException(
                "Race already finished");
    }

    @Override
    public void finish(Race race) {

        System.out.println(
                "Race has already finished");
    }
}

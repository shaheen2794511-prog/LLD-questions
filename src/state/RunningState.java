public class RunningState
        implements RaceState {

    @Override
    public void start(Race race) {

        throw new IllegalStateException(
                "Race already started");
    }

    @Override
    public void tick(Race race) {

        race.incrementTick();

        for (Horse horse : race.getHorses()) {
            horse.advance();
        }

        race.notifyObservers();

        if (race.hasFinished()) {
            race.setState(new FinishedState());
        }
    }

    @Override
    public void finish(Race race) {

        race.setState(new FinishedState());
    }
}

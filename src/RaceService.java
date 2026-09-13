public class RaceService {

    private final RaceValidationHandler validator;
    private final List<RaceObserver> observers;

    public RaceService(
            RaceValidationHandler validator,
            List<RaceObserver> observers) {

        this.validator = validator;
        this.observers = observers;
    }

    public void startRace(Race race) {

        validator.validate(race);

        race.start();
    }

    public void runRace(Race race) {

        while (!race.hasWinner()) {

            race.runTick();

            notifyObservers(race);
        }

        race.finish();
    }

    private void notifyObservers(Race race) {

        for (RaceObserver observer : observers) {
            observer.onRaceUpdate(race);
        }
    }
}

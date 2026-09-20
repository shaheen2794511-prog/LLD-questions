public class Main {

    public static void main(String[] args) {

        Horse daisy =
                HorseFactory.createHorse(
                        "Daisy", 3.5);

        Horse thunder =
                HorseFactory.createHorse(
                        "Thunder", 4.0);

        Horse lightning =
                HorseFactory.createHorse(
                        "Lightning", 3.0);

        RaceResultRepository repository =
                new FileRaceResultRepository();

        Race race =
                new RaceBuilder()
                        .addHorse(daisy)
                        .addHorse(thunder)
                        .addHorse(lightning)
                        .finishLine(50)
                        .winnerStrategy(
                                new HighestPositionWinnerStrategy())
                        .addObserver(
                                new ConsoleRaceObserver())
                        .build();

        HorseRacingFacade facade =
                new HorseRacingFacade(repository);

        facade.startRace(race);
    }
}

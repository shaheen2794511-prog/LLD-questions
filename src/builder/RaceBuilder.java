
public class RaceBuilder {

    private List<Horse> horses =
            new ArrayList<>();

    private double finishLine = 50;

    private WinnerStrategy winnerStrategy =
            new HighestPositionWinnerStrategy();

    private List<RaceObserver> observers =
            new ArrayList<>();

    public RaceBuilder addHorse(Horse horse) {

        horses.add(horse);

        return this;
    }

    public RaceBuilder finishLine(
            double finishLine) {

        this.finishLine = finishLine;

        return this;
    }

    public RaceBuilder winnerStrategy(
            WinnerStrategy strategy) {

        this.winnerStrategy = strategy;

        return this;
    }

    public RaceBuilder addObserver(
            RaceObserver observer) {

        observers.add(observer);

        return this;
    }

    public Race build() {

        Race race =
                new Race(
                        horses,
                        finishLine,
                        winnerStrategy);

        for (RaceObserver observer :
                observers) {

            race.addObserver(observer);
        }

        return race;
    }
}

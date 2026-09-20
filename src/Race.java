import java.util.*;

public class Race {

    private final List<Horse> horses;
    private final double finishLine;

    private int tickCount;

    private RaceState state;

    private final List<RaceObserver> observers;

    private final WinnerStrategy winnerStrategy;

    public Race(
            List<Horse> horses,
            double finishLine,
            WinnerStrategy winnerStrategy) {

        this.horses = horses;
        this.finishLine = finishLine;
        this.winnerStrategy = winnerStrategy;

        this.tickCount = 0;

        this.state = new NotStartedState();

        this.observers = new ArrayList<>();
    }

    public void start() {
        state.start(this);
    }

    public void tick() {
        state.tick(this);
    }

    public void finish() {
        state.finish(this);
    }

    public List<Horse> getHorses() {
        return horses;
    }

    public double getFinishLine() {
        return finishLine;
    }

    public int getTickCount() {
        return tickCount;
    }

    public void incrementTick() {
        tickCount++;
    }

    public void setState(RaceState state) {
        this.state = state;
    }

    public void addObserver(RaceObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers() {

        for (RaceObserver observer : observers) {
            observer.onRaceUpdate(this);
        }
    }

    public WinnerStrategy getWinnerStrategy() {
        return winnerStrategy;
    }

    public boolean hasFinished() {

        return horses.stream()
                .anyMatch(h ->
                        h.getPosition() >= finishLine);
    }
}

public class Race {

    private final List<Horse> horses;
    private final double finishLine;

    private int tickCount;
    private RaceState state;

    public Race(
            List<Horse> horses,
            double finishLine) {

        this.horses = horses;
        this.finishLine = finishLine;
        this.tickCount = 0;
        this.state = new NotStartedState();
    }

    public void start() {
        state.start(this);
    }

    public void runTick() {

        if (!(state instanceof RunningState)) {
            throw new IllegalStateException(
                    "Race is not running");
        }

        for (Horse horse : horses) {
            horse.run();
        }

        tickCount++;
    }

    public boolean hasWinner() {

        return horses.stream()
                .anyMatch(h ->
                    h.getPosition() >= finishLine);
    }

    public void finish() {
        state.finish(this);
    }

    public void cancel() {
        state.cancel(this);
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

    public void setState(RaceState state) {
        this.state = state;
    }
}

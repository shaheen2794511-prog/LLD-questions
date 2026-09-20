public interface RaceState {

    void start(Race race);

    void tick(Race race);

    void finish(Race race);
}

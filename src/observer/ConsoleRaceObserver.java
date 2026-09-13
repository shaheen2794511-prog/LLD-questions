public class ConsoleRaceObserver
        implements RaceObserver {

    @Override
    public void onRaceUpdate(Race race) {

        System.out.println(
                "Tick: " + race.getTickCount());

        for (Horse horse : race.getHorses()) {

            System.out.printf(
                    "%s : %.2f%n",
                    horse.getName(),
                    horse.getPosition());
        }
    }
}

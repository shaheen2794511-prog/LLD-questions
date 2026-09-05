import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Horse class - represents a single horse in the race.
 * Fields are private (encapsulation); only necessary getters are exposed.
 */
class Horse {
    private String name;
    private double speed;    // base step size
    private double position; // current distance from start

    // Constructor: assigns name and base speed, initializes position to 0
    public Horse(String name, double speed) {
        this.name = name;
        this.speed = speed;
        this.position = 0;
    }

    // run(): increases position by a random value between 0 and speed
    public void run() {
        Random rand = new Random();
        double step = rand.nextDouble() * speed; // 0 to speed
        position += step;
    }

    public double getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }
}

/**
 * Race class - orchestrates the simulation.
 * Holds a list of Horse objects, the finish line distance, and tick count.
 */
class Race {
    private List<Horse> horses;
    private double finishLine;
    private int tickCount;

    // Constructor: accepts list of horses and finish-line distance
    public Race(List<Horse> horses, double finishLine) {
        this.horses = horses;
        this.finishLine = finishLine;
        this.tickCount = 1;
    }

    // Main game loop
    public void startRace() {
        System.out.println("Starting race...");
        boolean finished = false;

        while (!finished) {
            System.out.println("Tick " + tickCount + ":");

            // a) For each horse, call its run() method
            for (Horse horse : horses) {
                horse.run();
            }

            // b) Display a summary of all horses' positions
            for (Horse horse : horses) {
                System.out.printf("  - %s: %.1f%n", horse.getName(), horse.getPosition());
            }

            // Check if at least one horse has reached/passed the finish line
            for (Horse horse : horses) {
                if (horse.getPosition() >= finishLine) {
                    finished = true;
                    break;
                }
            }

            tickCount++;
        }

        announceWinner();
    }

    // Determine winner(s) and print final results
    private void announceWinner() {
        double maxPosition = -1;
        for (Horse horse : horses) {
            if (horse.getPosition() > maxPosition) {
                maxPosition = horse.getPosition();
            }
        }

        List<Horse> winners = new ArrayList<>();
        for (Horse horse : horses) {
            if (horse.getPosition() == maxPosition) {
                winners.add(horse);
            }
        }

        System.out.println("Race finished in " + (tickCount - 1) + " ticks!");

        if (winners.size() == 1) {
            System.out.printf("Winner: %s (%.1f units)%n", winners.get(0).getName(), maxPosition);
        } else {
            System.out.print("It's a tie between: ");
            for (Horse w : winners) {
                System.out.print(w.getName() + " ");
            }
            System.out.printf("(%.1f units)%n", maxPosition);
        }
    }
}

/**
 * Main class - handles input/output and sets up the race.
 */
public class HorseRacingGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();

        System.out.print("Enter number of horses: ");
        int numHorses = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter finish line distance: ");
        double finishLine = Double.parseDouble(scanner.nextLine());

        List<Horse> horses = new ArrayList<>();
        for (int i = 1; i <= numHorses; i++) {
            System.out.print("Enter name for horse #" + i + ": ");
            String name = scanner.nextLine();
            double baseSpeed = 2 + rand.nextDouble() * 3; // random base speed between 2 and 5
            horses.add(new Horse(name, baseSpeed));
        }

        Race race = new Race(horses, finishLine);
        race.startRace();

        scanner.close();
    }
}

import java.util.*;

public class Race {

    private final List<Horse> horses;
    private final double finishLine;

    private Race(Builder builder) {
        this.horses = builder.horses;
        this.finishLine = builder.finishLine;
    }

    public static class Builder {

        private List<Horse> horses =
                new ArrayList<>();

        private double finishLine;

        public Builder addHorse(Horse horse) {
            horses.add(horse);
            return this;
        }

        public Builder finishLine(double distance) {
            this.finishLine = distance;
            return this;
        }

        public Race build() {
            return new Race(this);
        }
    }
}

package collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StreamMappingExample {

    public static void main(String[] args) {
        List<Birds> birds = getBirds();
        
        List<String> birdNames = birds.stream().collect(Collectors.mapping(Birds::getName, Collectors.toList()));
        System.out.println(birdNames);

        List<String> birdNamesAndTypes = birds.stream().collect(Collectors.mapping(getNameAndType(), Collectors.toList()));
        System.out.println(birdNamesAndTypes);
    }

    private static Function<? super Birds, ? extends String> getNameAndType() {
        return b -> b.getName() + " - " + b.getBeakType();
    }

    private static List<Birds> getBirds() {
        List<Birds> birds = new ArrayList<>();

        // FRUGIVOROUS
        birds.add(new Birds("Parrot", 0.5, 2, 30, BeakType.FRUGIVOROUS));
        birds.add(new Birds("Toucan", 0.7, 2, 35, BeakType.FRUGIVOROUS));

        // CARNIVOROUS
        birds.add(new Birds("Eagle", 4.0, 2, 70, BeakType.CARNIVOROUS));
        birds.add(new Birds("Hawk", 3.5, 2, 60, BeakType.CARNIVOROUS));

        // OMNIVOROUS
        birds.add(new Birds("Swan", 10.0, 5, 80, BeakType.OMNIVOROUS));
        birds.add(new Birds("Duck", 2.0, 5, 40, BeakType.OMNIVOROUS));

        // GRANIVOROUS
        birds.add(new Birds("Sparrow", 0.1, 2, 10, BeakType.GRANIVOROUS));
        birds.add(new Birds("Finch", 0.2, 2, 15, BeakType.GRANIVOROUS));

        // INSECTIVOROUS
        birds.add(new Birds("Hummingbird", 0.05, 2, 10, BeakType.INSECTIVOROUS));
        birds.add(new Birds("Warbler", 0.1, 2, 15, BeakType.INSECTIVOROUS));

        return birds;

    }

    public static class Birds {
        private String name;
        private double weight;
        private int eggs;
        private int wingspan;
        private BeakType beakType;

        public Birds(String name, double weight, int eggs, int wingspan, BeakType beakType) {
            this.name = name;
            this.weight = weight;
            this.eggs = eggs;
            this.wingspan = wingspan;
            this.beakType = beakType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public int getEggs() {
            return eggs;
        }

        public void setEggs(int eggs) {
            this.eggs = eggs;
        }

        public int getWingspan() {
            return wingspan;
        }

        public void setWingspan(int wingspan) {
            this.wingspan = wingspan;
        }

        public BeakType getBeakType() {
            return beakType;
        }

        public void setBeakType(BeakType beakType) {
            this.beakType = beakType;
        }
    
        
    }

    public enum BeakType {
        FRUGIVOROUS("Fruits and seeds"),
        CARNIVOROUS("Meat"),
        OMNIVOROUS("Both plants and animals"),
        GRANIVOROUS("Seeds"),
        INSECTIVOROUS("Insects"),
        FILTER_FEEDER("Feeds on small plants and animals by filtering water or air");
    
        private final String description;
    
        BeakType(String description) {
            this.description = description;
        }
    
        public String getDescription() {
            return description;
        }
    }
}

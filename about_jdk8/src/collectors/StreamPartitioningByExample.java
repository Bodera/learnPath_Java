package collectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StreamPartitioningByExample {

    static Predicate<Pokemon> isElectric = p -> p.getType().equals("Electric");

    public static void main(String[] args) {
        List<Pokemon> pokemons = Arrays.asList(
                new Pokemon("Pikachu", "Electric", 10),
                new Pokemon("Squirtle", "Water", 5),
                new Pokemon("Bulbasaur", "Grass", 15),
                new Pokemon("Charmander", "Fire", 12),
                new Pokemon("Meowth", "Normal", 8));

        Map<Boolean, List<Pokemon>> partitionedPokemons = pokemons.stream()
                .collect(Collectors.partitioningBy(isElectric));

        partitionedPokemons.forEach((electric, pokemonsInGroup) -> {
            System.out.println(electric ? "Electric pokemons" : "Non Electric pokemons");
            pokemonsInGroup.forEach(p -> System.out.println(p.getName()));
        });

        // Just the same example but using a Set
        Map<Boolean, Set<Pokemon>> setPartitionedPokemons = pokemons.stream()
                .collect(Collectors.partitioningBy(isElectric, Collectors.toSet()));

        Map<Boolean, Map<String, Pokemon>> typePartition = pokemons.stream()
                .collect(Collectors.partitioningBy(
                        isElectric,
                        Collectors.toMap(
                                Pokemon::getType,
                                Function.identity(),
                                (a, b) -> a // or b, depending on your merge strategy
                        )));

        System.out.println(typePartition);

        Map<Boolean, Map<String, List<Pokemon>>> typePartitionWithList = pokemons.stream()
                .collect(Collectors.partitioningBy(
                        isElectric,
                        Collectors.toMap(
                                Pokemon::getName,
                                pokemon -> {
                                    List<Pokemon> list = new ArrayList<>();
                                    list.add(pokemon);
                                    return list;
                                },
                                (a, b) -> {
                                    a.addAll(b);
                                    return a;
                                })));

        System.out.println(typePartitionWithList);

    }

    public static class Pokemon {
        private String name;
        private String type;
        private int level;

        public Pokemon(String name, String type, int level) {
            this.name = name;
            this.type = type;
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return "Pokemon{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", level=" + level +
                    '}';
        }
    }
}

import modernJava.Dish;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Collection {

    private List<Dish> menu;

    @BeforeEach
    void setUp() {
        menu = new ArrayList<>(){{
            this.add(new Dish("pork", false, 800, Dish.Type.MEAT));
            this.add(new Dish("beef", false, 700, Dish.Type.MEAT));
            this.add(new Dish("chicken", false, 400, Dish.Type.MEAT));
            this.add(new Dish("french fries", true, 530, Dish.Type.OTHER));
            this.add(new Dish("rice", true, 350, Dish.Type.OTHER));
            this.add(new Dish("season fruit", true, 120, Dish.Type.OTHER));
            this.add(new Dish("pizza", true, 550, Dish.Type.OTHER));
            this.add(new Dish("prawns", false, 300, Dish.Type.FISH));
            this.add(new Dish("salmon", false, 450, Dish.Type.FISH));
        }};
    }

    @Test
    void removeIf() {
        Assertions.assertThat(menu.size()).isEqualTo(9);

        menu.removeIf(Dish::vegetarian);
        Assertions.assertThat(menu.size()).isEqualTo(5);
    }

    @Test
    void replaceAll() {
        List<Dish> before = menu.stream().filter(dish -> dish.calories() <= 490).toList();
        Assertions.assertThat(before.size()).isEqualTo(5);

        menu.replaceAll(dish -> {
            if (dish.calories() > 500) {
                return new Dish(dish.name(), dish.vegetarian(), 490, dish.type());
            }
            return dish;
        });

        List<Dish> after = menu.stream().filter(dish -> dish.calories() <= 490).toList();
        Assertions.assertThat(after.size()).isEqualTo(9);
    }

    @Test
    void mapSort() {
        Map<String, Integer> ageOfFriends1 = Map.of("A", 1, "B", 2, "C", 3);
        Map<String, Integer> ageOfFriends2 = Map.ofEntries(
                Map.entry("A",1),
                Map.entry("B",2),
                Map.entry("C",3)
        );

        List<Map.Entry<String, Integer>> collect = ageOfFriends1.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
    }

    @Test
    void computeIfAbsent() {
        Map<String, Integer> ageOfFriends = new HashMap<>(Map.of("A", 1, "B", 2, "C", 3));
        // 제공된 키에 해당하는 값이 없으면 (값이 없거나 널) 값을 계산해 맵에 추가하고 키가 존재하면 기존 값을 반환한다.
        // 정보를 캐시할 때 사용할 수 있다.
        Assertions.assertThat(ageOfFriends.computeIfAbsent("A", key -> Integer.MAX_VALUE)).isEqualTo(1);
        Assertions.assertThat(ageOfFriends.computeIfAbsent("D", key -> Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
    }
}

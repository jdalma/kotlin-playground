import modernJava.Dish;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class Collection {

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
        assertThat(menu.size()).isEqualTo(9);

        menu.removeIf(Dish::vegetarian);
        assertThat(menu.size()).isEqualTo(5);
    }

    @Test
    void replaceAll() {
        List<Dish> before = menu.stream().filter(dish -> dish.calories() <= 490).toList();
        assertThat(before.size()).isEqualTo(5);

        menu.replaceAll(dish -> {
            if (dish.calories() > 500) {
                return new Dish(dish.name(), dish.vegetarian(), 490, dish.type());
            }
            return dish;
        });

        List<Dish> after = menu.stream().filter(dish -> dish.calories() <= 490).toList();
        assertThat(after.size()).isEqualTo(9);
    }

    @Test
    void mapSort() {
        Map<String, Integer> map1 = Map.of("A", 1, "B", 2, "C", 3);
        Map<String, Integer> map2 = Map.ofEntries(
                Map.entry("A",1),
                Map.entry("B",2),
                Map.entry("C",3)
        );

        List<Map.Entry<String, Integer>> collect = map1.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
    }

    @Test
    void computeIfAbsent() {
        Map<String, Integer> map = new HashMap<>(Map.of("A", 1, "B", 2, "C", 3));
        // 제공된 키에 해당하는 값이 없으면 (값이 없거나 널) 값을 계산해 맵에 추가하고 키가 존재하면 기존 값을 반환한다.
        // 정보를 캐시할 때 사용할 수 있다.
        assertThat(map.computeIfAbsent("A", key -> Integer.MAX_VALUE)).isEqualTo(1);
        assertThat(map.computeIfAbsent("D", key -> Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void computeIfPresent() {
        Map<String, Integer> map = new HashMap<>(Map.of("A", 1, "B", 2, "C", 3));
        BiFunction<String, Integer, Integer> ifPresent = (key, value) -> map.get(key) + 10;
        map.computeIfPresent("A", ifPresent);
        map.computeIfPresent("D", ifPresent);

        assertThat(map.get("A")).isEqualTo(11);
        assertThat(map.get("D")).isEqualTo(null);
    }

    @Test
    void merge() {
        Map<String, Integer> map1 = new HashMap<>(Map.of("A", 1, "B", 2, "C", 3));
        Map<String, Integer> map2 = new HashMap<>(Map.of("A", 1, "B", 2, "D", 3));

        map2.forEach((k, v) -> {
            map1.merge(k, v, Integer::sum);
        });

        assertThat(map1.get("A")).isEqualTo(2);
        assertThat(map1.get("B")).isEqualTo(4);
        assertThat(map1.get("C")).isEqualTo(3);
        assertThat(map1.get("D")).isEqualTo(3);
    }
}

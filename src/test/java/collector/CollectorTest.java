package collector;

import modernJava.Dish;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

public class CollectorTest {

    private List<Dish> menu;

    @BeforeEach
    void setUp() {
        menu = Arrays.asList(
                new Dish("pork", false, 800, Dish.Type.MEAT),
                new Dish("beef", false, 700, Dish.Type.MEAT),
                new Dish("chicken", false, 400, Dish.Type.MEAT),
                new Dish("french fries", true, 530, Dish.Type.OTHER),
                new Dish("rice", true, 350, Dish.Type.OTHER),
                new Dish("season fruit", true, 120, Dish.Type.OTHER),
                new Dish("pizza", true, 550, Dish.Type.OTHER),
                new Dish("prawns", false, 300, Dish.Type.FISH),
                new Dish("salmon", false, 450, Dish.Type.FISH)
        );
    }

    @Test
    void collect() {
        List<Dish> collect = menu.stream().collect(new ToListCollector<>());

        assertThat(menu.size()).isEqualTo(collect.size());
    }

    @Test
    void primeNumber() {
        final int number = 13;

        Map<Boolean, List<Integer>> collect = IntStream.rangeClosed(2, number)
                .boxed()
                .collect(new PrimeNumbersCollector());

        assertThat(collect.get(true)).isEqualTo(new ArrayList<>(){{
            this.add(2);
            this.add(3);
            this.add(5);
            this.add(7);
            this.add(11);
            this.add(13);
        }});
    }
}

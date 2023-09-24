import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ReducingAndSummarizing {

    private List<Dish> menu;
    private List<Dish> specialMenu;

    @BeforeEach
    void setUp() {
        menu = Arrays.asList(
                new Dish("pork" , false , 800 , Dish.Type.MEAT),
                new Dish("beef" , false , 700 , Dish.Type.MEAT),
                new Dish("chicken" , false , 400 , Dish.Type.MEAT),
                new Dish("french fries" , true , 530 , Dish.Type.OTHER),
                new Dish("rice" , true , 350 , Dish.Type.OTHER),
                new Dish("season fruit" , true , 120 , Dish.Type.OTHER),
                new Dish("pizza" , true , 550 , Dish.Type.OTHER),
                new Dish("prawns" , false , 300 , Dish.Type.FISH),
                new Dish("salmon" , false , 450 , Dish.Type.FISH)
        );

        specialMenu = Arrays.asList(
                new Dish("seasonal fruit" , true , 120 , Dish.Type.OTHER),
                new Dish("prawns" , false , 300 , Dish.Type.FISH),
                new Dish("rice" , true , 350 , Dish.Type.OTHER),
                new Dish("chicken" , false , 400 , Dish.Type.MEAT),
                new Dish("french fires" , true , 530 , Dish.Type.OTHER)
        );
    }

    @Test
    @DisplayName("Collectors.counting")
    void counting() {
        assertThat((Long) menu.stream().count()).isEqualTo(menu.size());
    }

    @Test
    @DisplayName("칼로리가 가장 높은,낮은 요리찾기")
    void minmax() {
        Comparator<Dish> dishComparator = Comparator.comparingInt(Dish::calories);
        Optional<Dish> collect1 = menu.stream().collect(maxBy(dishComparator));
        Optional<Dish> collect2 = menu.stream().max(dishComparator);

        assertThat(collect1.get().calories()).isEqualTo(800);
        assertThat(collect2.get().calories()).isEqualTo(800);

        Optional<Dish> collect3 = menu.stream().collect(minBy(dishComparator));
        Optional<Dish> collect4 = menu.stream().min(dishComparator);

        assertThat(collect3.get().calories()).isEqualTo(120);
        assertThat(collect4.get().calories()).isEqualTo(120);
    }

    @Test
    @DisplayName("요약 연산 : 합계, 평균")
    void summarizing() {
        int totalCalories1 = menu.stream().collect(summingInt(Dish::calories));
        int totalCalories2 = menu.stream().mapToInt(Dish::calories).sum();

        assertThat(totalCalories1).isEqualTo(4200);
        assertThat(totalCalories2).isEqualTo(4200);

        double avgCalories = menu.stream().collect(averagingInt(Dish::calories));
        assertThat(avgCalories).isEqualTo(466.6666666666667);

        IntSummaryStatistics collect = menu.stream().collect(summarizingInt(Dish::calories));
        assertThat(collect.getSum()).isEqualTo(4200);
        assertThat(collect.getAverage()).isEqualTo(466.6666666666667);
        assertThat(collect.getCount()).isEqualTo(menu.size());
        assertThat(collect.getMin()).isEqualTo(120);
        assertThat(collect.getMax()).isEqualTo(800);
    }

    @Test
    @DisplayName("문자열 joining 연산")
    void string() {

    }
}

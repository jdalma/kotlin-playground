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
        String collect = menu.stream().map(Dish::name).collect(joining(", "));
        assertThat(collect).isEqualTo("pork, beef, chicken, french fries, rice, season fruit, pizza, prawns, salmon");
    }

    @Test
    @DisplayName("범용 리듀싱 요약 연산")
    void reducingTest() {
        int collect1 = menu.stream().collect(reducing(0,Dish::calories,(i, j) -> i + j));
        int collect2 = menu.stream().map(Dish::calories).reduce(0, (i, j) -> i + j);
        int collect3 = menu.stream().map(Dish::calories).reduce(0, Integer::sum);

        assertThat(collect1).isEqualTo(4200);
        assertThat(collect2).isEqualTo(4200);
        assertThat(collect3).isEqualTo(4200);

        Optional<Dish> max1 = menu.stream().collect(reducing((d1, d2) -> d1.calories() > d2.calories() ? d1 : d2));
        Optional<Dish> max2 = menu.stream().reduce((d1, d2) -> d1.calories() > d2.calories() ? d1 : d2);

        assertThat(max1.get().calories()).isEqualTo(800);
        assertThat(max2.get().calories()).isEqualTo(800);
    }

    @Test
    @DisplayName("음식 타입별로 그룹화")
    void grouping() {
        Map<CaloricLevel, List<Dish>> collect = menu.stream().collect(groupingBy(dish -> {
            if (dish.calories() <= 400) return CaloricLevel.DIET;
            else if (dish.calories() <= 700) return CaloricLevel.NORMAL;
            else return CaloricLevel.FAT;
        }));

        Map<Dish.Type, List<Dish>> collect1 = menu.stream().filter(dish -> dish.calories() > 500)
                .collect(groupingBy(Dish::type));

//        {
//          MEAT=[Dish{name='pork', vegetarian=false, calories=800, type=MEAT}, Dish{name='beef', vegetarian=false, calories=700, type=MEAT}]
//          OTHER=[Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
//        }
        
        Map<Dish.Type, List<Dish>> collect2 = menu.stream()
                .collect(groupingBy(
                        Dish::type,
                        filtering(dish -> dish.calories() > 500, toList())
                ));
//        {
//          FISH=[],
//          MEAT=[Dish{name='pork', vegetarian=false, calories=800, type=MEAT}, Dish{name='beef', vegetarian=false, calories=700, type=MEAT}],
//          OTHER=[Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
//        }

        Map<Boolean, Map<Dish.Type, List<Dish>>> collect3 = menu.stream().collect(partitioningBy(Dish::vegetarian, groupingBy(Dish::type)));
//      {
//        false={
//          MEAT=[Dish{name='pork', vegetarian=false, calories=800, type=MEAT}, Dish{name='beef', vegetarian=false, calories=700, type=MEAT}, Dish{name='chicken', vegetarian=false, calories=400, type=MEAT}],
//          FISH=[Dish{name='prawns', vegetarian=false, calories=300, type=FISH}, Dish{name='salmon', vegetarian=false, calories=450, type=FISH}]
//        },
//        true={
//          OTHER=[Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, Dish{name='rice', vegetarian=true, calories=350, type=OTHER}, Dish{name='season fruit', vegetarian=true, calories=120, type=OTHER}, Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
//        }
//      }

        Map<Dish.Type, List<String>> collect4 = menu.stream().collect(groupingBy(Dish::type, mapping(Dish::name, toList())));
//{
//  FISH=[prawns, salmon],
//  MEAT=[pork, beef, chicken],
//  OTHER=[french fries, rice, season fruit, pizza]
//}
        Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel = menu.stream().collect(
                groupingBy(Dish::type,
                        groupingBy(dish -> {
                            if (dish.calories() <= 400) return CaloricLevel.DIET;
                            else if (dish.calories() <= 700) return CaloricLevel.NORMAL;
                            else return CaloricLevel.FAT;
                        }))
        );
//        {
//          FISH={
//              DIET=[Dish{name='prawns', vegetarian=false, calories=300, type=FISH}],
//              NORMAL=[Dish{name='salmon', vegetarian=false, calories=450, type=FISH}]
//          },
//          MEAT={
//              FAT=[Dish{name='pork', vegetarian=false, calories=800, type=MEAT}],
//              DIET=[Dish{name='chicken', vegetarian=false, calories=400, type=MEAT}],
//              NORMAL=[Dish{name='beef', vegetarian=false, calories=700, type=MEAT}]
//          },
//          OTHER={
//              DIET=[Dish{name='rice', vegetarian=true, calories=350, type=OTHER}, Dish{name='season fruit', vegetarian=true, calories=120, type=OTHER}],
//              NORMAL=[Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
//          }
//       }

        Map<Dish.Type, Long> typesCount = menu.stream().collect(
                groupingBy(Dish::type, Collectors.counting())
        );
//         {FISH=2, MEAT=3, OTHER=4}

        Map<Dish.Type, Optional<Dish>> mostCaloricType = menu.stream().collect(
                groupingBy(Dish::type, maxBy(Comparator.comparingInt(Dish::calories)))
        );
//        {
//          FISH=Optional[Dish{name='salmon', vegetarian=false, calories=450, type=FISH}],
//          MEAT=Optional[Dish{name='pork', vegetarian=false, calories=800, type=MEAT}],
//          OTHER=Optional[Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
//        }
    }
}

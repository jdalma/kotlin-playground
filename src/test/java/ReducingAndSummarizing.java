import modernJava.CaloricLevel;
import modernJava.Dish;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    void takeWhile() {
        List<Dish> sliceMenu1 =
                specialMenu.stream()
                        .takeWhile(dish -> dish.calories() < 320)
                        .toList();
        assertThat(sliceMenu1.size()).isEqualTo(2);
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
    void grouping() {
        Map<CaloricLevel, List<Dish>> collect = menu.stream().collect(groupingBy(dish -> {
            if (dish.calories() <= 400) return CaloricLevel.DIET;
            else if (dish.calories() <= 700) return CaloricLevel.NORMAL;
            else return CaloricLevel.FAT;
        }));

        Map<Dish.Type, List<Dish>> collect1 = menu.stream().filter(dish -> dish.calories() > 500)
                .collect(groupingBy(Dish::type));

//        {
//          MEAT=[modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT}, modernJava.Dish{name='beef', vegetarian=false, calories=700, type=MEAT}]
//          OTHER=[modernJava.Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, modernJava.Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
//        }
        
        Map<Dish.Type, List<Dish>> collect2 = menu.stream()
                .collect(groupingBy(
                        Dish::type,
                        filtering(dish -> dish.calories() > 500, toList())
                ));
//        {
//          FISH=[],
//          MEAT=[modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT}, modernJava.Dish{name='beef', vegetarian=false, calories=700, type=MEAT}],
//          OTHER=[modernJava.Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, modernJava.Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
//        }

        Map<Boolean, Map<Dish.Type, List<Dish>>> collect3 = menu.stream().collect(partitioningBy(Dish::vegetarian, groupingBy(Dish::type)));
//      {
//        false={
//          MEAT=[modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT}, modernJava.Dish{name='beef', vegetarian=false, calories=700, type=MEAT}, modernJava.Dish{name='chicken', vegetarian=false, calories=400, type=MEAT}],
//          FISH=[modernJava.Dish{name='prawns', vegetarian=false, calories=300, type=FISH}, modernJava.Dish{name='salmon', vegetarian=false, calories=450, type=FISH}]
//        },
//        true={
//          OTHER=[modernJava.Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, modernJava.Dish{name='rice', vegetarian=true, calories=350, type=OTHER}, modernJava.Dish{name='season fruit', vegetarian=true, calories=120, type=OTHER}, modernJava.Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
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
//              DIET=[modernJava.Dish{name='prawns', vegetarian=false, calories=300, type=FISH}],
//              NORMAL=[modernJava.Dish{name='salmon', vegetarian=false, calories=450, type=FISH}]
//          },
//          MEAT={
//              FAT=[modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT}],
//              DIET=[modernJava.Dish{name='chicken', vegetarian=false, calories=400, type=MEAT}],
//              NORMAL=[modernJava.Dish{name='beef', vegetarian=false, calories=700, type=MEAT}]
//          },
//          OTHER={
//              DIET=[modernJava.Dish{name='rice', vegetarian=true, calories=350, type=OTHER}, modernJava.Dish{name='season fruit', vegetarian=true, calories=120, type=OTHER}],
//              NORMAL=[modernJava.Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, modernJava.Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
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
//          FISH=Optional[modernJava.Dish{name='salmon', vegetarian=false, calories=450, type=FISH}],
//          MEAT=Optional[modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT}],
//          OTHER=Optional[modernJava.Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}]
//        }

        Map<Dish.Type, Dish> mostCaloricByType1 = menu.stream().collect(
                groupingBy(
                    Dish::type,
                    collectingAndThen(maxBy(Comparator.comparingInt(Dish::calories)),Optional::get)
                )
        );
//{
//  FISH=modernJava.Dish{name='salmon', vegetarian=false, calories=450, type=FISH},
//  MEAT=modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT},
//  OTHER=modernJava.Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}
//}
        Map<Dish.Type, IntSummaryStatistics> sumCaloricByType = menu.stream().collect(
                groupingBy(Dish::type, summarizingInt(Dish::calories))
        );
//{
// FISH=IntSummaryStatistics{count=2, sum=750, min=300, average=375.000000, max=450},
// MEAT=IntSummaryStatistics{count=3, sum=1900, min=400, average=633.333333, max=800},
// OTHER=IntSummaryStatistics{count=4, sum=1550, min=120, average=387.500000, max=550}
//}
        Map<Dish.Type, Set<CaloricLevel>> caloricLevelsByType = menu.stream().collect(
                groupingBy(
                        Dish::type,
                        mapping(dish -> {
                                    if (dish.calories() <= 400) return CaloricLevel.DIET;
                                    else if (dish.calories() <= 700) return CaloricLevel.NORMAL;
                                    else return CaloricLevel.FAT;
                                },
                                toSet()
                        )
                )
        );
//{
// FISH=[DIET, NORMAL],
// MEAT=[FAT, DIET, NORMAL],
// OTHER=[DIET, NORMAL]
//}

        Map<Dish.Type, Set<CaloricLevel>> caloricLevelsByType2 = menu.stream().collect(
                groupingBy(
                        Dish::type,
                        mapping(dish -> {
                                    if (dish.calories() <= 400) return CaloricLevel.DIET;
                                    else if (dish.calories() <= 700) return CaloricLevel.NORMAL;
                                    else return CaloricLevel.FAT;
                                },
                                toCollection(HashSet::new)
                        )
                )
        );
    }
    @Test
    void partitioning() {
        Map<Boolean, List<Dish>> partitionVegetarian = menu.stream().collect(partitioningBy(Dish::vegetarian));
//{
// false=[modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT}, modernJava.Dish{name='beef', vegetarian=false, calories=700, type=MEAT}, ...],
// true=[modernJava.Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, modernJava.Dish{name='rice', vegetarian=true, calories=350, type=OTHER}, ...]
//}
        Map<Boolean, Map<Dish.Type, List<Dish>>> vegetarianDishesByType =
                menu.stream().collect(
                    partitioningBy(
                        Dish::vegetarian,
                        groupingBy(Dish::type)
                    )
                );

//{
// false={
//      FISH=[modernJava.Dish{name='prawns', vegetarian=false, calories=300, type=FISH}, modernJava.Dish{name='salmon', vegetarian=false, calories=450, type=FISH}],
//      MEAT=[modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT}, modernJava.Dish{name='beef', vegetarian=false, calories=700, type=MEAT}, ...]
// },
// true={
//      OTHER=[modernJava.Dish{name='french fries', vegetarian=true, calories=530, type=OTHER}, modernJava.Dish{name='rice', vegetarian=true, calories=350, type=OTHER}, ...]
//  }
//}
        Map<Boolean, Dish> mostCaloricPartitionedByVegetarian = menu.stream().collect(
            partitioningBy(
                Dish::vegetarian,
                collectingAndThen(
                    maxBy(Comparator.comparingInt(Dish::calories)),
                    Optional::get
                )
            )
        );
//{
// false=modernJava.Dish{name='pork', vegetarian=false, calories=800, type=MEAT},
// true=modernJava.Dish{name='pizza', vegetarian=true, calories=550, type=OTHER}
//}
    }

    private boolean isPrime(int candidate) {
        int candidateRoot = (int) Math.sqrt((double) candidate);
        return IntStream
                .rangeClosed(2, candidateRoot)
                .noneMatch(i -> candidate % i == 0);
    }

    @Test
    void isPrimeTest() {
        int n = 11;
        Map<Boolean, List<Integer>> collect = IntStream
                .rangeClosed(2, n)
                .boxed()
                .collect(partitioningBy(this::isPrime));
//{
// false=[4, 6, 8, 9, 10],
// true=[2, 3, 5, 7, 11]
//}
    }
}

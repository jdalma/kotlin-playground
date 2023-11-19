package modernJava.streamforker;


import modernJava.Dish;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

public class StreamForker<T> {
    private final Stream<T> stream;
    private final Map<Object, Function<Stream<T>, ?>> forks = new HashMap<>();

    public StreamForker(Stream<T> stream) {
        this.stream = stream;
    }

    public StreamForker<T> fork(Object key, Function<Stream<T>, ?> block) {
        // 스트림에 적용할 함수를 저장하고, 이 메서드를 여러 번 호출할 수 있도록 자기 자신을 반환한다.
        forks.put(key, block);
        return this;
    }

    public Results getResults() {
        ForkingStreamConsumer<T> consumer = build();
        try {
            stream.sequential().forEach(consumer);
        } finally {
            consumer.finish();
        }
        return consumer;
    }

    public ForkingStreamConsumer<T> build() {
        List<BlockingQueue<T>> queues = new ArrayList<>();
        Map<Object, Future<?>> actions =
                forks.entrySet().stream().reduce(
                        new HashMap<Object, Future<?>>(),
                        (map, e) -> {
                            map.put(e.getKey(),
                                    getOperationResult(queues, e.getValue()));
                            return map;
                        },
                        (m1, m2) -> {
                            m1.putAll(m2);
                            return m1;
                        });
        return new ForkingStreamConsumer<>(queues, actions);
    }

    private Future<?> getOperationResult(List<BlockingQueue<T>> queues, Function<Stream<T>, ?> block) {
        BlockingQueue<T> queue = new LinkedBlockingQueue<>();
        queues.add(queue);

        // 큐의 요소를 탐색하는 Spliterator 생성
        Spliterator<T> spliterator = new BlockingQueueSpliterator<>(queue);

        // Spliterator를 소스로 갖는 스트림을 생성
        Stream<T> source = StreamSupport.stream(spliterator, false);

        // 스트림에서 주어진 함수를 비동기로 적용해서 결과를 얻을 Future 생성
        return CompletableFuture.supplyAsync(() -> block.apply(source));
    }

    public static interface Results {
        public <R> R get(Object key);
    }

    private static class ForkingStreamConsumer<T> implements Consumer<T>, Results {
        static final Object END_OF_STREAM = new Object();

        private final List<BlockingQueue<T>> queues;
        private final Map<Object, Future<?>> actions;

        public ForkingStreamConsumer(List<BlockingQueue<T>> queues, Map<Object, Future<?>> actions) {
            this.queues = queues;
            this.actions = actions;
        }

        @Override
        public void accept(T t) {
            // 스트림에서 탐색한 요소를 모든 큐로 전달
            queues.forEach(queue -> queue.add(t));
        }

        @Override
        public <R> R get(Object key) {
            try {
                // 키에 대응하는 동작의 결과를 반환, Future의 계산 완료 대기
                return ((Future<R>) actions.get(key)).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void finish() {
            // 스트림의 끝을 알리는 마지막 요소를 큐에 삽입
            accept((T) END_OF_STREAM);
        }
    }

    private record BlockingQueueSpliterator<T>(BlockingQueue<T> queue) implements Spliterator<T> {

        @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                T t;
                while (true) {
                    try {
                        t = queue.take();
                        break;
                    } catch (InterruptedException ignored) {
                    }
                }

                if (t != ForkingStreamConsumer.END_OF_STREAM) {
                    action.accept(t);
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return 0;
            }

            @Override
            public int characteristics() {
                return 0;
            }
        }

    public static void main(String[] args) {
        List<Dish> menu = asList(
                new Dish("pork", false, 800, Dish.Type.MEAT),
                new Dish("beef", false, 700, Dish.Type.MEAT),
                new Dish("chicken", false, 400, Dish.Type.MEAT),
                new Dish("french fries", true, 530, Dish.Type.OTHER),
                new Dish("rice", true, 350, Dish.Type.OTHER),
                new Dish("season fruit", true, 120, Dish.Type.OTHER),
                new Dish("pizza", true, 550, Dish.Type.OTHER),
                new Dish("prawns", false, 400, Dish.Type.FISH),
                new Dish("salmon", false, 450, Dish.Type.FISH)
        );

        Stream<Dish> menuStream = menu.stream();

        StreamForker.Results results = new StreamForker<Dish>(menuStream)
                .fork("shortMenu", s -> s.map(Dish::getName).collect(joining(", ")))
                .fork("totalCalories", s -> s.mapToInt(Dish::getCalories).sum())
                .fork("mostCaloricDish", s -> s.reduce((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2).get())
                .fork("dishesByType", s -> s.collect(groupingBy(Dish::getType)))
                .getResults();

        String shortMenu = results.get("shortMenu");
        int totalCalories = results.get("totalCalories");
        Dish mostCaloricDish = results.get("mostCaloricDish");
        Map<Dish.Type, List<Dish>> dishesByType = results.get("dishesByType");

        System.out.println("Short menu: " + shortMenu);
        System.out.println("Total calories: " + totalCalories);
        System.out.println("Most caloric dish: " + mostCaloricDish);
        System.out.println("Dishes by type: " + dishesByType);
    }
}

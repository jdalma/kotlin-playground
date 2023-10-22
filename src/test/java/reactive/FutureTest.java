package reactive;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FutureTest {

    private int sum(int n) {
        return IntStream.rangeClosed(1, n).sum();
    }

    @Test
    @DisplayName("Thread 시작, 조인")
    void thread() throws InterruptedException {
        Map<String, Integer> resultMap = new HashMap<>();
        Thread t1 = new Thread(() -> resultMap.put("thread1", sum(100)));
        Thread t2 = new Thread(() -> resultMap.put("thread2", sum(1000)));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        Assertions.assertThat(resultMap.get("thread1")).isEqualTo(5050);
        Assertions.assertThat(resultMap.get("thread2")).isEqualTo(500500);
    }

    @Test
    @DisplayName("Future 적용")
    void future() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Integer> result1 = executorService.submit(() -> {
            System.out.println("thread1");
            Thread.sleep(3000);
            return sum(100);
        });
        Future<Integer> result2 = executorService.submit(() -> {
            System.out.println("thread2");
            Thread.sleep(3000);
            return sum(1000);
        });

        Integer integer = result1.get();
        System.out.println("1");
        Integer integer1 = result2.get();
        System.out.println("2");
        test(integer,integer1);
        System.out.println("3");

//        만약 스레드 풀의 스레드가 한 개라면 위의 작업은 당연히 총 6초가 걸린다.
//        thread1
//        thread2
//        1
//        2
//        5050 500500
//        3
        Assertions.assertThat(result1.get()).isEqualTo(5050);
        Assertions.assertThat(result2.get()).isEqualTo(500500);
        executorService.shutdown();
    }
    private void test(int test1, int test2) {
        System.out.println(test1 + " " + test2);
    }

    void callbackMethod(int n, IntConsumer dealWithResult) {
        dealWithResult.accept(n);
    }
    @Test
    @DisplayName("콜백 적용")
    void callback() {
        Map<String, Integer> resultMap = new HashMap<>();

        callbackMethod(100, (int number) -> {
            resultMap.put("thread1", number);
        });

        callbackMethod(200, (int number) -> {
            resultMap.put("thread2", number);
        });

        Assertions.assertThat(resultMap.get("thread1")).isEqualTo(100);
        Assertions.assertThat(resultMap.get("thread2")).isEqualTo(200);
    }

    private void doSomething(String text) {
        System.out.printf("do Something ... %s\n", text);
    }

    @Test
    @DisplayName("태스크 스케줄링")
    void scheduling() throws InterruptedException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        doSomething("first");
        executorService.schedule(() -> doSomething("second"), 5, SECONDS);
        executorService.shutdown();
        Thread.sleep(10000);
    }

    @Test
    @DisplayName("CompletableFuture 적용")
    void completableFutureCombine() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Integer> result1 = executorService.submit(() -> sum(100));
        Future<Integer> result2 = executorService.submit(() -> sum(1000));

        Integer sum = Integer.sum(result1.get(), result2.get());
        Assertions.assertThat(sum).isEqualTo(505550);

        CompletableFuture<Integer> completableFuture1 = new CompletableFuture<>();
        CompletableFuture<Integer> completableFuture2 = new CompletableFuture<>();
        CompletableFuture<Integer> completableFuture3 = completableFuture1.thenCombine(completableFuture2, Integer::sum);
        executorService.submit(() -> completableFuture1.complete(sum(100)));
        executorService.submit(() -> completableFuture2.complete(sum(1000)));

        Assertions.assertThat(completableFuture3.get()).isEqualTo(505550);
        executorService.shutdown();
    }

    interface Publisher<T> {
        void subscribe(Subscriber<? super T> subscriber);
    }

    interface Subscriber<T> {
        void onNext(T t);
    }
    private class SimpleCell implements Publisher<Integer>, Subscriber<Integer> {
        private int value = 0;
        private String name;
        private List<Subscriber> subscribers = new ArrayList<>();

        public SimpleCell(String name) {
            this.name = name;
        }

        public int getValue() {
            return this.value;
        }

        private void notifyAllSubscribers() {
            this.subscribers.forEach(subscriber -> subscriber.onNext(this.value));
        }

        @Override
        public void subscribe(Subscriber<? super Integer> subscriber) {
            this.subscribers.add(subscriber);
        }

        @Override
        public void onNext(Integer integer) {
            this.value = integer;
            System.out.printf("%s : %d\n", this.name, this.value);
            notifyAllSubscribers();
        }
    }

    private class ArithmeticCell extends SimpleCell {
        private int left;
        private int right;

        public ArithmeticCell(String name) {
            super(name);
        }

        public void setLeft(int left) {
            this.left = left;
            super.onNext(left + this.right);
        }

        public void setRight(int right) {
            this.right = right;
            super.onNext(right + this.left);
        }
    }

    @Test
    @DisplayName("셀 pub-sub")
    void pubsub() {
        SimpleCell c1 = new SimpleCell("C1");
        SimpleCell c2 = new SimpleCell("C2");
        SimpleCell c3 = new SimpleCell("C3");

        c1.subscribe(c3);
        c2.subscribe(c3);
        c1.onNext(10);
        Assertions.assertThat(c3.value).isEqualTo(10);
        c2.onNext(20);
        Assertions.assertThat(c3.value).isEqualTo(20);

        ArithmeticCell c4 = new ArithmeticCell("C4");
        c1.subscribe(c4::setLeft);
        c2.subscribe(c4::setRight);

        c1.onNext(10);
        Assertions.assertThat(c4.getValue()).isEqualTo(10);
        c2.onNext(20);
        Assertions.assertThat(c4.getValue()).isEqualTo(30);

//        C1 : 10
//        C3 : 10
//        C2 : 20
//        C3 : 20
//        C1 : 10
//        C3 : 10
//        C4 : 10
//        C2 : 20
//        C3 : 20
//        C4 : 30
    }
}

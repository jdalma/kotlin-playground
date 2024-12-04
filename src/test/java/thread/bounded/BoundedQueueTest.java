package thread.bounded;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static thread.MyLogger.*;

public class BoundedQueueTest {

    @Test
    void boundedQueueV1() throws InterruptedException {
        BoundedQueue queue = new BoundedQueueV1(2);

//        producerFirst(queue);
        consumerFirst(queue);

    }

    private static void producerFirst(BoundedQueue queue) throws InterruptedException {
        log("== [생산자 먼저 실행] 시작, " + queue.getClass().getSimpleName() + " ==");
        List<Thread> threads = new ArrayList<>();

        startProducer(queue, threads);
        printAllState(queue, threads);
        startConsumer(queue, threads);
        printAllState(queue, threads);

        log("== [생산자 먼저 실행] 종료, " + queue.getClass().getSimpleName() + " ==");
    }

    private static void consumerFirst(BoundedQueue queue) throws InterruptedException {
        log("== [소비자 먼저 실행] 시작, " + queue.getClass().getSimpleName() + " ==");
        List<Thread> threads = new ArrayList<>();

        startConsumer(queue, threads);
        printAllState(queue, threads);
        startProducer(queue, threads);
        printAllState(queue, threads);

        log("== [소비자 먼저 실행] 종료, " + queue.getClass().getSimpleName() + " ==");
    }

    private static void printAllState(BoundedQueue queue, List<Thread> threads) {
        System.out.println();
        log("현재 상태 출력, 큐 데이터: " + queue);
        for (Thread t : threads) {
            log(t.getName() + ": " + t.getState());
        }
    }

    private static void startProducer(BoundedQueue queue, List<Thread> threads) throws InterruptedException {
        System.out.println();
        log("생산자 시작");
        for (int i = 0 ; i < 3 ; i++) {
            Thread producer = new Thread(new ProducerTask(queue, "data" + i), "producer" + i);
            threads.add(producer);
            producer.start();

            Thread.sleep(100);
        }
    }

    private static void startConsumer(BoundedQueue queue, List<Thread> threads) throws InterruptedException {
        System.out.println();
        log("소비자 시작");
        for (int i = 0 ; i < 3 ; i++) {
            Thread consumer = new Thread(new ConsumerTask(queue), "consumer" + i);
            threads.add(consumer);
            consumer.start();

            Thread.sleep(100);
        }
    }
}

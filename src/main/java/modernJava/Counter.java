package modernJava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Counter {

    volatile int count = 0;

    public void increase() {
        count++;
    }

    public static void main(String[] args) throws InterruptedException {
        final Counter counter = new Counter();
        final ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0 ; i < 10_000; i++) {
            executorService.submit(counter::increase);
        }
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        executorService.shutdown();
        System.out.println(counter.count);
    }
}

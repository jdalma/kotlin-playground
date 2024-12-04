package modernJava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SynchronizedClass {
    private final Object monitor;
    int blockCount = 0;
    int methodCount = 0;
    SynchronizedClass(Object monitor) {
        this.monitor = monitor;
    }

    public void synchronizedBlock() {
        synchronized (monitor) {
            blockCount++;
        }
    }

    public synchronized void synchronizedMethod() {
        methodCount++;
    }


    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Object lock = new Object();
        SynchronizedClass test = new SynchronizedClass(lock);
        for (int i = 0; i < 100000; i++) {
            executor.submit(() -> {
                test.synchronizedBlock();
                test.synchronizedMethod();
            });
        }
    }
}

package _udemy_Multithreading;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static _udemy_Multithreading.HackerGame.MAX_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

public class ThreadTest {

    @Test
    void thread() throws InterruptedException {
        Thread thread = new Thread(() -> System.out.println("now child thread before starting : " + Thread.currentThread().getName()));

        System.out.println("now thread before starting : " + Thread.currentThread().getName());
        thread.start();
        System.out.println("now thread after starting : " + Thread.currentThread().getName());

        // 이 시간이 지날 때 까지는 현재 스레드를 스케줄링하지 말라고 운영체제에게 알리는 것이다.
        // 메인 스레드는 이 시간동안 CPU를 사용하지 않는다.
        Thread.sleep(1000);
    }

    @Test
    void setPriority() throws InterruptedException {
        Thread thread = new Thread(() -> {
            System.out.println("now child thread before starting : " + Thread.currentThread().getName());
            System.out.println("Current Thread priority is " + Thread.currentThread().getPriority());
        });

        thread.setName("New Worker Thread");
        thread.setPriority(Thread.MAX_PRIORITY);
        System.out.println("now thread before starting : " + Thread.currentThread().getName());
        thread.start();
        System.out.println("now thread after starting : " + Thread.currentThread().getName());

        Thread.sleep(1000);

//        now thread before starting : Test worker
//        now thread after starting : Test worker
//        now child thread before starting : New Worker Thread
//        Current Thread priority is 10
    }

    @Test
    void setExceptionHandler() throws InterruptedException {
        Thread thread = new Thread(() -> {
            throw new RuntimeException("자식 스레드에서 예외 발생");
        });

        thread.setName("New Worker Thread");
        // 스레드 내에서 발생한 예외가 어디서도 캐치되지 않으면 이 예외가 실행된다.
        thread.setUncaughtExceptionHandler((t, e) -> {
            System.out.println("[ExceptionHandler] error happened in thread : " + t.getName() + " the error is " + e.getMessage());
        });
        System.out.println("now thread before starting : " + Thread.currentThread().getName());
        thread.start();
        System.out.println("now thread after starting : " + Thread.currentThread().getName());

        Thread.sleep(1000);

        try {
            thread.start();
        } catch (RuntimeException e) {
            System.out.println("try/catch error");
        }

//        now thread before starting : Test worker
//        now thread after starting : Test worker
//        [ExceptionHandler] error happened in thread : New Worker Thread the error is 자식 스레드에서 예외 발생
//        try/catch error
    }

    @Test
    @DisplayName("스레드 확장")
    void threadExtend() throws InterruptedException {
        Random random = new Random();

        HackerGame.Vault vault = new HackerGame.Vault(random.nextInt(MAX_PASSWORD) % 10_000);

        List<Thread> threads = new ArrayList<>(){{
            this.add(new HackerGame.AscendingHackerThread(vault));
            this.add(new HackerGame.DescendingHackerThread(vault));
            this.add(new HackerGame.PoliceThread());
        }};
//        threads.forEach(Runnable::run);
        threads.forEach(Thread::start);
        Thread.sleep(15000);
    }

    @Test
    void interrupted() {
        Runnable blockingTask = () -> {
            try {
                Thread.sleep(50_000);
            } catch (InterruptedException e) {
                System.out.println("Blocking Task Exit");
                throw new RuntimeException(e);
            }
        };

        Thread thread = new Thread(blockingTask);
        thread.start();
        thread.interrupt();
    }

    private record LongComputationTask(BigInteger base, BigInteger power) implements Runnable {
        @Override
        public void run() {
            System.out.printf("%d^%d = %d\n", base, power, pow(base, power));
        }

        private BigInteger pow(BigInteger base, BigInteger power) {
            BigInteger result = BigInteger.ONE;
            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Interrupted !!!");
                    return BigInteger.ZERO;
                }
                result = result.multiply(base);
            }
            return result;
        }
    }

    @Test
    void interrupted2() {
        LongComputationTask longComputationTask = new LongComputationTask(
            new BigInteger("200"),
            new BigInteger("1000")
        );

        Thread thread = new Thread(longComputationTask);
        thread.start();
        thread.interrupt();
    }

    @Test
    void daemonThread() {
        LongComputationTask longComputationTask = new LongComputationTask(
                new BigInteger("200"),
                new BigInteger("1000")
        );

        Thread thread = new Thread(longComputationTask);
        thread.setDaemon(true);
        thread.start();
    }

    @Test
    void sharedMonitor() {
        final Object monitor = new Object();
        final StateThread stateThread = new StateThread(monitor);
        final StateThread stateThread2 = new StateThread(monitor);

        try {
            System.out.println("Thread state = " + stateThread.getState());
            stateThread.start();
            stateThread2.start();
            System.out.println("Thread state (after start) = " + stateThread.getState());
            System.out.println("Thread2 state (after start) = " + stateThread2.getState());


            Thread.sleep(1000);
            System.out.println("Thread state (after 0.1 sec) = " + stateThread.getState());
            System.out.println("Thread2 state (after 0.1 sec) = " + stateThread2.getState());

            synchronized (monitor) {
                monitor.notifyAll();
            }
            System.out.println("Thread state (after notify) = " + stateThread.getState());
            System.out.println("Thread2 state (after notify) = " + stateThread2.getState());

            stateThread.join();
            System.out.println("Thread state (after join) = " + stateThread.getState());
            stateThread2.join();
            System.out.println("Thread2 state (after join) = " + stateThread2.getState());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class StateThread extends Thread {
    private final Object monitor;
    StateThread(Object monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10000; i++) {
                String a = "a";
            }
            synchronized (monitor) {
                // 이 monitor에 대한 주인이 된 후에 wait을 호출하면, 이 객체에 대한 잠금을 포기한다.
                // 그렇기에 다른 곳에서 이 monitor의 주인이 될 수 있다.
                monitor.wait();
            }
            System.out.println(super.getName() + " is notified " + System.currentTimeMillis());
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

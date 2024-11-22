package thread;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static thread.HackerGame.MAX_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

public class ThreadTest {

    @Test
    @DisplayName("자식 스레드 생성 후 실행")
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
    @DisplayName("자식 스레드 생성 후 우선순위 지정")
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
    @DisplayName("자식 스레드 예외 핸들링")
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
    @DisplayName("스레드 인터럽트")
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

        LongComputationTask longComputationTask = new LongComputationTask(
                new BigInteger("2"),
                new BigInteger("10")
        );
        Thread thread2 = new Thread(longComputationTask);
        thread2.start();
        thread2.interrupt();
    }

    @Test
    @DisplayName("데몬 스레드 지정")
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
    @DisplayName("동기화 객체를 공유하여 메인 스레드가 자식 스레드 여러 개를 깨우는 방법")
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
                System.out.println("메인 스레드 동기화 블록 진입");
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

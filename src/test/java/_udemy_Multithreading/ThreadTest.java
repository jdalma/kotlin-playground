package _udemy_Multithreading;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static _udemy_Multithreading.HackerGame.MAX_PASSWORD;

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
}

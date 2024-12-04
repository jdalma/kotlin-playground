package thread;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static thread.MyLogger.*;

public class InterruptTest {

    @Test
    void interrupt() throws InterruptedException {
        Thread thread = new Thread(new MyTask(), "work");
        thread.start();

        Thread.sleep(2000);
        log("작업 중단 지시");
        thread.interrupt();

        Assertions.assertThat(thread.isInterrupted()).isTrue();
    }

    @Test
    void interruptStatusCheck() {
        Thread thread = new Thread(new ImprovedMyTask(), "work");
        thread.start();

        log("작업 중단 지시");
        thread.interrupt();

        Assertions.assertThat(thread.isInterrupted()).isTrue();
    }

    private static class MyTask implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    log("progressing ...");
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                log("work 스레드 인터럽트 상태 : " + Thread.currentThread().isInterrupted());
                log("interrupt message : " + e.getMessage());
                log("state : " + Thread.currentThread().getState());

                // 인터럽트를 받은 스레드는 대기 상태에서 꺠어나 RUNNABLE 상태가 되고, 코드를 정상 수행한다.
                // 이때 InterruptedException을 catch로 잡아서 정상 흐름으로 변경하면 된다.
                // interrupt()를 호출했다고 해서 예외가 즉각 발생하는 것은 아니다. 오직 sleep()처럼 InterruptedException을 던지는 메소드를 호출하거나 또는 호출 중일 때 예외가 발생한다.

                Assertions.assertThat(Thread.currentThread().isInterrupted()).isFalse();
            }
            log("end ...");
        }
    }

    private static class ImprovedMyTask implements Runnable {

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                log("progressing ...");
            }
            Assertions.assertThat(Thread.currentThread().isInterrupted()).isFalse();
            log("end ...");
        }
    }
}

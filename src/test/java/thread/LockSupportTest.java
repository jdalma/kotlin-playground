package thread;

import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.*;
import static thread.MyLogger.*;

public class LockSupportTest {

    @Test
    void park() throws InterruptedException {
        Runnable parkTask = () -> {
            log("park 시작");
            LockSupport.park();     // 대기 상태로 빠진다.

            // park 상태를 탈출한 이후에 아래가 실행되기에 RUNNABLE 상태가 된다.
            assertThat(Thread.currentThread().getState()).isEqualTo(Thread.State.RUNNABLE);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        };

        Thread thread = new Thread(parkTask, "park thread");
        thread.start();

        Thread.sleep(1000);
        assertThat(thread.getState()).isEqualTo(Thread.State.WAITING);

        LockSupport.unpark(thread);

//        thread.interrupt();
//        Thread.sleep(1000);
//        assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    void parkNanos() throws InterruptedException {
        Runnable parkNanoTask = () -> {
            log("park 시작");
            LockSupport.parkNanos(1000);     // 대기 상태로 빠진다.

            // park 상태를 탈출한 이후에 아래가 실행되기에 RUNNABLE 상태가 된다.
            assertThat(Thread.currentThread().getState()).isEqualTo(Thread.State.RUNNABLE);
            assertThat(Thread.currentThread().isInterrupted()).isFalse();
        };
        Thread thread = new Thread(parkNanoTask, "park thread");
        thread.start();
        thread.join();

        Thread.sleep(3000);
        assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
    }
}

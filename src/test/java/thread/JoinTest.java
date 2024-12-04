package thread;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static thread.MyLogger.*;

public class JoinTest {

    @Test
    void sumTask() throws InterruptedException {
        log("Start");
        SumTask task1 = new SumTask(1, 50);
        SumTask task2 = new SumTask(51, 100);
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        thread1.join(500);
        thread2.join(500);

        Assertions.assertThat(task1.value).isEqualTo(0);
        Assertions.assertThat(task2.value).isEqualTo(0);

        thread1.join(2000);
        thread2.join(2000);

        Assertions.assertThat(task1.value).isEqualTo(1275);
        Assertions.assertThat(task2.value).isEqualTo(3775);

        log("End");
    }


    static class SumTask implements Runnable {

        private final int start;
        private final int end;
        int value;

        public SumTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            log("task start");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            value = 0;
            for (int i = start ; i <= end ; i++) {
                value += i;
            }
            log("task end : " + value);
        }
    }
}

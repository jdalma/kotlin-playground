import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VolatileTest {

    public static volatile int race = 0;

    public static void increase() {
        race++;
    }

    private static final int THREADS_COUNT = 20;
    private static final int INCREASE_COUNT = 10000;

    @Test
    void volatileTest() throws InterruptedException {
        Thread[] threads = new Thread[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < INCREASE_COUNT; j++) {
                    increase();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Assertions.assertThat(race).isNotEqualTo(THREADS_COUNT * INCREASE_COUNT);
    }
}

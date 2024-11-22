package thread.runnable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RunnableTest {

    @Test
    @DisplayName("Runnable 구현")
    void runnable() {
        System.out.println("main start : " + Thread.currentThread().getName());
        new Thread(new HelloRunnable()).start();
        System.out.println("main end : " + Thread.currentThread().getName());
    }
}

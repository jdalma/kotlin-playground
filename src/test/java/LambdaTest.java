import modernJava.Task;
import org.junit.jupiter.api.Test;

public class LambdaTest {

    public static void doSomething(Runnable r) {
        r.run();
    }

    public static void doSomething(Task t) {
        t.execute();
    }

    @Test
    void sameSign() {
//        doSomething(() -> System.out.println("Danger")); // 컴파일 에러
        doSomething((Runnable) () -> System.out.println("Danger"));
        doSomething((Task) () -> System.out.println("Danger"));
    }
}

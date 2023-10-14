import io.kotest.core.spec.style.AnnotationSpec;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class MultipleInheritanceTest {

    interface A {
        default String hello() {
            return "Hello from A interface";
        }
    }

    interface AA {
        default String hello() {
            return "Hello from AA interface";
        }
    }

    interface B extends A {
        default String hello() {
            return "Hello from B interface";
        }
    }

    class C implements B,A {
        @Override
        public String hello() {
            return "Hello from C class";
        }
    }
    class D implements B,A {}
    class E implements A,B {}
    class F implements A,AA {
        @Override
        public String hello() {
            return A.super.hello();
        }
    }
    class G implements A {}
    class H extends G implements B,A{}

    @Test
    void multipleDefaultMethod() {
        Assertions.assertThat(new C().hello()).isEqualTo("Hello from C class");
        Assertions.assertThat(new D().hello()).isEqualTo("Hello from B interface");
        Assertions.assertThat(new E().hello()).isEqualTo("Hello from B interface");
        Assertions.assertThat(new F().hello()).isEqualTo("Hello from A interface");
        Assertions.assertThat(new H().hello()).isEqualTo("Hello from B interface");
    }
}

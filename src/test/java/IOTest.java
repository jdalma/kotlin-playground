import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.IntBuffer;

public class IOTest {

    @Test
    void intBuffer() {
        try {
            IntBuffer buffer = IntBuffer.allocate(1024);
            for (int i = 0 ; i < 100 ; i++) {
                buffer.put(i);
            }

            // 0 <= mark <= position <= limit <= capacity
            Assertions.assertThat(buffer.capacity()).isEqualTo(1024);
            Assertions.assertThat(buffer.limit()).isEqualTo(1024);
            Assertions.assertThat(buffer.position()).isEqualTo(100);

            buffer.flip();
            Assertions.assertThat(buffer.limit()).isEqualTo(100);
            Assertions.assertThat(buffer.position()).isEqualTo(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void bufferStatus() throws InvocationTargetException, IllegalAccessException {
        IntBuffer buffer = IntBuffer.allocate(1024);

        Assertions.assertThat(bufferCommand("get", buffer)).isEqualTo(
                "position=1,remaining=1023,limit=1024"
        );
        Assertions.assertThat(bufferCommand("mark", buffer)).isEqualTo(
                "position=1,remaining=1023,limit=1024"
        );
        Assertions.assertThat(bufferCommand("get", buffer)).isEqualTo(
                "position=2,remaining=1022,limit=1024"
        );
        Assertions.assertThat(bufferCommand("reset", buffer)).isEqualTo(
                "position=1,remaining=1023,limit=1024"
        );
        Assertions.assertThat(bufferCommand("rewind", buffer)).isEqualTo(
                "position=0,remaining=1024,limit=1024"
        );
        Assertions.assertThat(bufferCommand("get", buffer)).isEqualTo(
                "position=1,remaining=1023,limit=1024"
        );
        Assertions.assertThat(bufferCommand("get", buffer)).isEqualTo(
                "position=2,remaining=1022,limit=1024"
        );
        Assertions.assertThat(bufferCommand("clear", buffer)).isEqualTo(
                "position=0,remaining=1024,limit=1024"
        );
    }

    private String bufferCommand(String methodName, IntBuffer buffer) throws InvocationTargetException, IllegalAccessException {
        for (Method method : IntBuffer.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                method.invoke(buffer);
                break;
            }
        }
        return "position=" + buffer.position() +
                ",remaining=" + buffer.remaining() +
                ",limit=" + buffer.limit();
    }
}

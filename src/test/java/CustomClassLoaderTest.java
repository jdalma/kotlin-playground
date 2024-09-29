import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class CustomClassLoaderTest {

    @Test
    void 서로_다른_클래스_로더가_로딩한_클래스는_서로_다른_클래스이다() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // 고유한 클래스로더 생성
        ClassLoader myLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                try {
                    String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                    InputStream is = getClass().getResourceAsStream(fileName);
                    if (is == null) {
                        return super.loadClass(name);
                    }
                    byte[] b = new byte[is.available()];
                    is.read(b);
                    return defineClass(name, b, 0, b.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name);
                }
            }
        };

        Object o = myLoader.loadClass("CustomClassLoaderTest").getDeclaredConstructor().newInstance();

//        System.out.println(o.getClass().getClassLoader());      // CustomClassLoaderTest$1@2c7b5824
//        System.out.println(this.getClass().getClassLoader());   // jdk.internal.loader.ClassLoaders$AppClassLoader@5ffd2b27

        Assertions.assertThat(o.getClass()).isNotEqualTo(CustomClassLoaderTest.class);
        Assertions.assertThat(o instanceof CustomClassLoaderTest).isFalse();
    }
}

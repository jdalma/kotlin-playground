package map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapTest {

    @Test
    void hashCollisionTest() {
        Map<CustomKey, Integer> map = new HashMap<>();

        List<Integer> givenList = List.of(
                // 해시값 0 후보
                0, 3, 6, 9, 12, 15, 18, 21, 24,
                // 해시값 1 후보
                1, 4, 7, 10, 13, 16, 19, 22, 25, 28,
                // 해시값 2 후보
                2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35, 38
        );
        for (var i :  givenList) {
            CustomKey key = new CustomKey(i);
            map.put(key, i);
            if (i == 24) {
                map.remove(key);
            }
        }
        Assertions.assertThat(map.keySet().size()).isEqualTo(3);
    }
}

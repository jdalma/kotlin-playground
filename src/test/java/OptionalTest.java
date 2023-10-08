import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Properties;

public class OptionalTest {

    public class Person {
        public Optional<Car> car;

        public Optional<Car> getCar() {
            return car;
        }
    }
    public class Car {
        public Optional<Insurance> insurance;

        public Car(Optional<Insurance> insurance) {
            this.insurance = insurance;
        }

        public Optional<Insurance> getInsurance() {
            return insurance;
        }
    }
    public class Insurance {
        private String name;

        public Insurance(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.car = Optional.of(new Car(Optional.of(new Insurance("insurance name"))));
    }

    @Test
    void map() {
        Optional<Optional<Insurance>> insurance = person.car.map(Car::getInsurance);
    }

    @Test
    void flatMap() {
        // Optional<String> s = person.car.map(Car::getName).map(Insurance::getName); // 컴파일 에러
        Optional<Person> personOpt = Optional.of(person);
        String name = personOpt.flatMap(Person::getCar)
                .flatMap(Car::getInsurance)
                .map(Insurance::getName)
                .orElse("Unknown");
        Assertions.assertThat(name).isEqualTo("insurance name");
    }

    @Test
    void property() {
        Properties prop = new Properties() {{
            this.put("a" , 10);
            this.put("b" , 20);
            this.put("c" , "null");
        }};
        Assertions.assertThat(readDuration(prop , "a")).isEqualTo(10);
        Assertions.assertThat(readDuration(prop , "b")).isEqualTo(20);
        Assertions.assertThat(readDuration(prop , "c")).isEqualTo(0);
    }

    int readDuration(Properties properties, String name) {
        return Optional.ofNullable(properties.get(name))
                .flatMap(text -> parseInt(String.valueOf(text)))
                .orElse(0);
    }

    Optional<Integer> parseInt(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

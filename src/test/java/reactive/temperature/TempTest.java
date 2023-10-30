package reactive.temperature;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Flow;

public class TempTest {

    @Test
    void start() {
        getCelsiusTemperatures("New York").subscribe(new TempSubscriber());
    }

    private Flow.Publisher<TempInfo> getCelsiusTemperatures(String town) {
        return subscriber -> {
            TempProcessor processor = new TempProcessor();
            processor.subscribe(subscriber);
            // Processor를 Publisher와 Subscriber 사이로 연결한다.
            processor.onSubscribe(new TempSubscription(processor, town));
        };
    }

    private Flow.Publisher<TempInfo> getTemperatures(String town) {
        return subscriber -> subscriber.onSubscribe(new TempSubscription(subscriber, town));
    }
}

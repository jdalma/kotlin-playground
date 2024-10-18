package reactive.temperature;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

/**
 * Subscription은 Subscriber가 구독한 데이터의 개수를 요청하거나 구독을 해지하는 역할을 한다.
 */
public class TempSubscription implements Flow.Subscription {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Flow.Subscriber<? super TempInfo> subscriber;
    private final String town;

    public TempSubscription(Flow.Subscriber<? super TempInfo> subscriber, String town) {
        this.subscriber = subscriber;
        this.town = town;
    }

    @Override
    public void request(long n) {
        executor.submit(() -> {
            for (long i = 0L ; i < n ; i++) {
                try {
                    // 현재 온도를 Subscriber 에게 전달
                    subscriber.onNext(TempInfo.fetch(town));
                } catch (Exception e) {
                    // 온도 가져오기를 실패하면 Subscriber 로 에러를 전달
                    subscriber.onError(e);
                    break;
                }
            }
        });
    }

    @Override
    public void cancel() {
        // 구독이 취소되면 onComplete 신호를 Subscriber 로 전달
        subscriber.onComplete();
    }
}

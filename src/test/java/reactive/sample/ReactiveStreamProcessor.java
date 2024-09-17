package reactive.sample;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class ReactiveStreamProcessor extends SubmissionPublisher<Integer> implements Flow.Subscriber<Integer> {

    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Integer item) {
        this.submit(item * 10);
        System.out.println("PROCESSOR published to subscriber item [" + item + "]");
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        closeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        System.out.println("ReactiveStreamProcessor Completed!");
        close();
    }
}

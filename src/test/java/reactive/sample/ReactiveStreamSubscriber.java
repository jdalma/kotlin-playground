package reactive.sample;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static java.lang.Thread.sleep;

public class ReactiveStreamSubscriber implements Flow.Subscriber<Integer> {

    private Flow.Subscription subscription;
    private String name;
    private boolean completed;

    public ReactiveStreamSubscriber(String name) {
        this.name = name;
        this.completed = false;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Integer item) {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[" + name + "] Received Item :" + item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error occurred: " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        this.completed = true;
        System.out.println("[" + name + "] ReactiveStreamSubscriber is complete");
    }

    public boolean isCompleted() {
        return completed;
    }
}

class TestCode {

    @Test
    void reactiveStream() {
        try (final SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
            ReactiveStreamSubscriber subscriber1 = new ReactiveStreamSubscriber("subscriber1");
            ReactiveStreamSubscriber subscriber2 = new ReactiveStreamSubscriber("subscriber2");
            publisher.subscribe(subscriber1);
            publisher.subscribe(subscriber2);

            System.out.println("Submitting items...");

            for (int i = 0 ; i < 5 ; i++) {
                publisher.submit(i);
                System.out.println("published: " + i);
            }
            // publisher를 닫아야 subscriber의 onComplete이 호출된다.
            publisher.close();
            while (true) {
                if (subscriber1.isCompleted() && subscriber2.isCompleted()) {
                    break;
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void reactiveStreamWithProcessor() {
        try (
            final SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
            final ReactiveStreamProcessor processor = new ReactiveStreamProcessor()
        ) {
            ReactiveStreamSubscriber subscriber1 = new ReactiveStreamSubscriber("subscriber1");
            ReactiveStreamSubscriber subscriber2 = new ReactiveStreamSubscriber("subscriber2");

            publisher.subscribe(processor);
            publisher.subscribe(subscriber1);
            publisher.subscribe(subscriber2);

            System.out.println("Submitting items...");

            for (int i = 0 ; i < 5 ; i++) {
                publisher.submit(i);
                System.out.println("PUBLISHER published to process item [" + i + "]");
                Thread.sleep(1000);
            }
            publisher.close();
            while (true) {
                if (subscriber1.isCompleted() && subscriber2.isCompleted()) {
                    break;
                }
                Thread.sleep(100);
            }
            System.out.println("Done!!!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

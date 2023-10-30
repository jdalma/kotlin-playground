package reactive.temperature;

import java.util.concurrent.Flow;

public class TempProcessor implements Flow.Processor<TempInfo, TempInfo> {

    private Flow.Subscriber<? super TempInfo> subscriber;

    @Override
    public void subscribe(Flow.Subscriber<? super TempInfo> subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * 섭씨로 변환한 다음 구독자에게 전달한다.
     */
    @Override
    public void onNext(TempInfo item) {
        this.subscriber.onNext(new TempInfo(item.getTown(), toCelsius(item.getTemp())));
    }

    /**
     * 아래의 onSubscribe, onError, onComplete 의 신호는 업스트림 구독자에게 전달한다.
     */
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscriber.onSubscribe(subscription);
    }

    @Override
    public void onError(Throwable throwable) {
        subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }

    // 섭씨로 변경
    private int toCelsius(int temperature) {
        return (temperature - 32) * 5 / 9;
    }
}

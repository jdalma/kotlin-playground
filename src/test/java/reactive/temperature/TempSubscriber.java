package reactive.temperature;

import java.util.concurrent.Flow;

public class TempSubscriber implements Flow.Subscriber<TempInfo> {

    private Flow.Subscription subscription;

    /**
     * 구독 시작 시점에 어떤 처리를 하는 역할을 한다.
     * Subscription 객체를 통해 Publisher에게 요청할 데이터의 개수를 지정하거나 구독을 해지할 수 있다.
     */
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    /**
     * Publisher가 통지한 데이터를 처리하는 역할을 한다.
     */
    @Override
    public void onNext(TempInfo item) {
        System.out.println(item);
        subscription.request(1);
    }

    /**
     * Publisher가 데이터 통지를 위한 처리 과정에서 에러가 발생했을 때 해당 에러를 처리하는 역할을 한다.
     */
    @Override
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
    }

    /**
     * Publisher가 데이터 통지를 완료했음을 알릴 때 호출되는 메서드다.
     * 데이터 통지가 정상적으로 완료될 경우에 어떤 후 처리를 작성하면 된다.
     */
    @Override
    public void onComplete() {
        System.out.println("Done!");
    }
}

package reactive.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static reactive.shop.ExchangeService.*;

public class AsyncShop {

    public static void delay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Random random = new Random();
    public static void randomDelay() {
        int delay = 500 + random.nextInt(1000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public class Shop {

        final String name;
        public Shop(String name) {
            this.name = name;
        }

        public double getPrice(String product) {
            return calculatePrice(product);
        }

        public String getPriceOfCode(String product) {
            double price = calculatePrice(product);
            Discount.Code code = Discount.Code.values()[new Random().nextInt(5)];
            return String.format("%s:%.2f:%s", name, price, code);
        }

        public String getPriceOfCodeAndRandomDelay(String product) {
            double price = calculatePrice(product);
            Discount.Code code = Discount.Code.values()[new Random().nextInt(5)];
            randomDelay();
            return String.format("%s:%.2f:%s", name, price, code);
        }

        public Future<Double> getPriceAsync(String product) {
            CompletableFuture<Double> futurePrice = new CompletableFuture<>();
            new Thread(() -> {
                double price = calculatePrice(product);
                futurePrice.complete(price);
            }).start();
            return futurePrice;
        }

        public Future<Double> getPriceAsyncExceptionally(String product) {
            CompletableFuture<Double> futurePrice = new CompletableFuture<>();
            new Thread(() -> {
                try {
                    double price = calculatePrice(product);
                    futurePrice.complete(price);
                } catch(Exception ex) {
                    futurePrice.completeExceptionally(ex);
                }
            }).start();
            return futurePrice;
        }

        private double calculatePrice(String product) {
            if (Objects.equals(product, "Exception")) {
                throw new RuntimeException("Timeout Exception!!!");
            }
            delay();
            return new Random().nextDouble() * product.charAt(0) + product.charAt(1);
        }
    }

    @Test
    void getPriceAsync() {
        Shop shop = new Shop("test");
        long start = System.nanoTime();
        Future<Double> myFavorite = shop.getPriceAsync("my favorite");
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("invocation returned after " + invocationTime + " msecs");

        System.out.println("doSomethingElse");

        try {
            Double price = myFavorite.get();
            System.out.printf("Price is %.2f\n", price);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Price returned after " + retrievalTime + " msecs");
    }

    @Test
    void getPriceAsyncExceptionally() throws ExecutionException, InterruptedException {
        Shop shop = new Shop("test");
//        자식 스레드에서 예외 발생
//        shop.getPriceAsync("Exception");

        Future<Double> myFavorite = shop.getPriceAsyncExceptionally("Exception");
        assertThatThrownBy(myFavorite::get)
                .isInstanceOf(ExecutionException.class)
                .hasMessage("java.lang.RuntimeException: Timeout Exception!!!");
    }

    @Test
    @DisplayName("팩토리 메서드 supplyAsync로 CompletableFuture 만들기")
    void supplyAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Double> myFavorite1 = CompletableFuture
                .supplyAsync(() -> new Shop("test").calculatePrice("my favorite"))
                .exceptionally(exceptionType -> 0D);
        assertThat(myFavorite1.get()).isGreaterThan(0);

        CompletableFuture<Double> myFavorite2 = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return new Shop("test").getPriceAsyncExceptionally("Exception").get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }).exceptionally(throwable -> 0D);
        assertThat(myFavorite2.get()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("여러 Shop의 가격을 계산할 때 비블록 코드로 작성하기")
    void nonblock() {
        List<Shop> shops = List.of(
            new Shop("a"),
            new Shop("b"),
            new Shop("c"),
            new Shop("d"),
            new Shop("e")
        );
        long start = System.nanoTime();
        String product = "iPhone";
        List<String> single = shops.stream()
                .map(shop -> String.format("%s price is %.2f", shop.name, shop.getPrice(product)))
                .toList();

        long duration = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("stream process duration " + duration + " msecs");
        // 위의 결과는 각 상점마다 1초씩 딜레이가 존재하여 최소 5초 이상이다.
        // stream process duration 5041 msecs


        long start1 = System.nanoTime();
        List<String> blockingParallel = shops.parallelStream()
                .map(shop -> String.format("%s price is %.2f", shop.name, shop.getPrice(product)))
                .toList();

        long duration1 = ((System.nanoTime() - start1) / 1_000_000);
        System.out.println("blockingParallel process duration1 " + duration1 + " msecs");
        // blockingParallel process duration1 1009 msecs

        // 리스트의 CompletableFuture는 각각 계산 결과가 끝난 상점의 이름 문자열을 포함한다.
        // 하지만 필요한 반환 타입은 List<String>이므로 모든 CompletableFuture의 동작이 완료되고 결과를 추출한 다음에 리스트를 반환해야 한다.
        // 즉, 리스트의 모든 CompletableFuture에 join을 호출해서 모든 동작이 끝나기를 기다린다.
        // CompletableFuture의 join메서드는 Future인터페이스의 get 메서드와 같은 의미를 갖는다.
        // 다만 join은 아무 예외도 발생시키지 않는다는 점이 다르다.
        // 따라서 map의 람다 표현식을 try/catch로 감쌀 필요가 없는 것이다.
        long start2 = System.nanoTime();
        List<CompletableFuture<String>> futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() -> String.format("%s price is %.2f", shop.name, shop.getPrice(product))))
                .toList();
        List<String> strings = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        strings.forEach(System.out::println);
        long duration2 = ((System.nanoTime() - start2) / 1_000_000);
        System.out.println("futures process duration2 " + duration2 + " msecs");
        // futures process duration2 1010 msecs
    }

    @Test
    void discount() {
        List<Shop> shops = List.of(
                new Shop("a"),
                new Shop("b"),
                new Shop("c")
        );
        final Executor executor = Executors.newFixedThreadPool(shops.size());
        final String product = "iPhone";

        long start1 = System.nanoTime();
        List<String> collect = shops.stream()
                .map(shop -> shop.getPriceOfCode(product))
                .map(Quote::parse)
                .map(Discount::applyDiscount)
                .toList();
        long duration1 = ((System.nanoTime() - start1) / 1_000_000);
        System.out.println("shops stream process " + duration1 + " msecs");
//        shops stream process 6044 msecs

        long start2 = System.nanoTime();
        List<CompletableFuture<String>> collect1 = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(
                        () -> shop.getPriceOfCode(product), executor))
                .peek( (it) -> System.out.println("step1 : " + it))
                .map(future -> future.thenApply(Quote::parse))
                .peek( (it) -> System.out.println("step2 : " + it))
                .map(future -> future.thenCompose(quoteFuture ->
                        CompletableFuture.supplyAsync(() -> {
                            System.out.println("discount!!!");
                            return Discount.applyDiscount(quoteFuture);
                        }, executor)))
                .peek( (it) -> System.out.println("step3 : " + it))
                .toList();

//        step1 : java.util.concurrent.CompletableFuture@76b1e9b8[Not completed]
//        step2 : java.util.concurrent.CompletableFuture@5d0bf09b[Not completed]
//        step3 : java.util.concurrent.CompletableFuture@563f38c4[Not completed]
//        step1 : java.util.concurrent.CompletableFuture@54422e18[Not completed]
//        step2 : java.util.concurrent.CompletableFuture@117159c0[Not completed]
//        step3 : java.util.concurrent.CompletableFuture@3e27ba32[Not completed]
//        step1 : java.util.concurrent.CompletableFuture@3b0fe47a[Not completed]
//        step2 : java.util.concurrent.CompletableFuture@202b0582[Not completed]
//        step3 : java.util.concurrent.CompletableFuture@235ecd9f[Not completed]
//        discount!!!
//        discount!!!
//        discount!!!
//        shops completable future process 2015 msecs

        for(CompletableFuture<String> future : collect1) {
            assertThat(future.isDone()).isEqualTo(false);
        }

        List<String> list = collect1.stream()
                .map(CompletableFuture::join)
                .toList();

        long duration2 = ((System.nanoTime() - start2) / 1_000_000);
        System.out.println("shops completable future process " + duration2 + " msecs");

        for (int i = 0; i < collect1.size(); i++) {
            CompletableFuture<String> future = collect1.get(i);
            assertThat(future.isDone()).isEqualTo(true);

            String e = list.get(i);
            assertThat(e).isNotEmpty();
        }
    }

    @Test
    void futurePrice() {
        final Shop shop = new Shop("a");
        final String product = "iPhone";
        CompletableFuture<Double> doubleCompletableFuture = CompletableFuture
                .supplyAsync(() -> shop.getPrice(product))
                .thenCombine(
                    CompletableFuture.supplyAsync(() -> getRate(Money.EUR, Money.USD))
                                    .completeOnTimeout(DEFAULT_RATE, 1, TimeUnit.SECONDS),
                    (price, rate) -> price * rate
                )
                .orTimeout(3, TimeUnit.SECONDS);
        Double result = doubleCompletableFuture.join();
    }

    @Test
    void java7_futurePrice() {
        final Shop shop = new Shop("a");
        final String product = "iPhone";
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<Double> futureRate = executorService.submit(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                return getRate(Money.EUR, Money.USD);
            }
        });
        Future<Double> futureResult = executorService.submit(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                double priceInUSD = shop.getPrice(product);
                return priceInUSD * futureRate.get();
            }
        });
    }

    @Test
    void findPricesStream() {
        List<Shop> shops = List.of(
                new Shop("a"),
                new Shop("b"),
                new Shop("c"),
                new Shop("d"),
                new Shop("e")
        );
        final String product = "iPhone";
        ExecutorService executorService = Executors.newCachedThreadPool();

        long start = System.nanoTime();
        CompletableFuture[] futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPriceOfCodeAndRandomDelay(product), executorService))
                .map(future -> future.thenApply(Quote::parse))
                .map(future -> future.thenCompose(futureQuote ->
                        CompletableFuture.supplyAsync(() -> Discount.applyDiscount(futureQuote), executorService))
                )
                .map(future -> future.thenAccept(it -> System.out.printf("%s (done in %s msecs)\n", it, (System.nanoTime() - start) / 1_000_000)))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
//        첫 번쨰
//        d price is 68.425 (done in 2777 msecs)
//        a price is 81.605 (done in 2887 msecs)
//        e price is 118.61 (done in 2910 msecs)
//        b price is 73.967 (done in 3169 msecs)
//        c price is 154.60649999999998 (done in 3350 msecs)
//        두 번째
//        c price is 162.22 (done in 2558 msecs)
//        e price is 135.43200000000002 (done in 2588 msecs)
//        a price is 132.752 (done in 2762 msecs)
//        d price is 114.08800000000001 (done in 3188 msecs)
//        b price is 125.229 (done in 3345 msecs)
    }
}

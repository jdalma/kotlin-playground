package parallelTest;

import modernJava.ForkJoinSumCalculator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Parallel {

    private long parallelSum(long n) {
        return Stream.iterate(1L, i -> i + 1)
                .limit(n)
                .parallel()
                .reduce(0L, Long::sum);
    }

    @Test
    void forkJoin() {
        long[] numbers = LongStream.rangeClosed(1, 100_000).toArray();
        ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
        Long result = new ForkJoinPool().invoke(task);
        Assertions.assertThat(result).isEqualTo(5000050000L);
//        ForkJoinPool-1-worker-1 start: 0, end: 100000
//        ForkJoinPool-1-worker-1 start: 50000, end: 100000
//        ForkJoinPool-1-worker-2 start: 0, end: 50000
//        ForkJoinPool-1-worker-1 start: 75000, end: 100000
//        ForkJoinPool-1-worker-1 start: 87500, end: 100000
//        ForkJoinPool-1-worker-2 start: 25000, end: 50000
//        ForkJoinPool-1-worker-1 start: 75000, end: 87500
//        ForkJoinPool-1-worker-3 start: 0, end: 25000
//        ForkJoinPool-1-worker-4 start: 50000, end: 75000
//        ForkJoinPool-1-worker-2 start: 37500, end: 50000
//        ForkJoinPool-1-worker-4 start: 62500, end: 75000
//        ForkJoinPool-1-worker-4 start: 50000, end: 62500
//        ForkJoinPool-1-worker-3 start: 12500, end: 25000
//        ForkJoinPool-1-worker-5 start: 25000, end: 37500
//        ForkJoinPool-1-worker-6 start: 0, end: 12500
    }
}

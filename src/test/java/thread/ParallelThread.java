package thread;

import modernJava.ForkJoinSumCalculator;
import modernJava.WordCounter;
import modernJava.WordCounterSpliterator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ParallelThread {

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

    private int countWordsIteratively(String s) {
        int counter = 0;
        boolean lastSpace = true;
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c)) {
                lastSpace = true;
            } else {
                if (lastSpace) counter++;
                lastSpace = false;
            }
        }
        return counter;
    }

    @Test
    void whiteSpaceCount() {
        // 1. 차례대로 탐색
        final String content = "Lorem Ipsum is    simply   dummy text of   the   printing and typesetting industry.";
        int count = this.countWordsIteratively(content);

        Assertions.assertThat(count).isEqualTo(12);

        // 2. Stream의 리듀스 연산
        Stream<Character> stream = IntStream.range(0, content.length()).mapToObj(content::charAt);
        WordCounter reduce = stream.reduce(
                new WordCounter(0, true),
                WordCounter::accumulate,
                WordCounter::combine
        );

        Assertions.assertThat(reduce.getCounter()).isEqualTo(12);

        // 3. 병렬로 연산
        WordCounterSpliterator wordCounterSpliterator = new WordCounterSpliterator(content);
        Stream<Character> stream2 = StreamSupport.stream(wordCounterSpliterator, true); // 병렬 스트림 생성 여부 true
        WordCounter reduce2 = stream2.reduce(
                new WordCounter(0, true),
                WordCounter::accumulate,
                WordCounter::combine
        );

        Assertions.assertThat(reduce2.getCounter()).isEqualTo(12);
    }

}

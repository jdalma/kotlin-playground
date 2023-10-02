package modernJava;

import java.util.Spliterator;
import java.util.function.Consumer;

public class WordCounterSpliterator implements Spliterator<Character> {
    private final String string;
    private int currentIndex = 0;

    public WordCounterSpliterator(String string) {
        this.string = string;
    }

    /**
     * 문자열에서 현재 인덱스에 해당하는 문자를 Consumer에 제공한 다음에 인덱스를 증가시킨다.
     * 인수로 전달된 Consumer는 스트림을 탐색하면서 적용해야 하는 함수 집합이 작업을 처리할 수 있도록 소비한 문자를 전달하는 자바 내부 클래스다.
     * 여기에서는 스트림을 탐색하면서 하나의 리듀싱 함수, 즉 WordCounter.accumulate 메서드만 적용한다.
     * 새로운 커서 위치가 전체 문자열 길이보다 작으면 참을 반환하며 반복 탐색해야 할 문자가 남아있음을 의미한다.
     */
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
        action.accept(string.charAt(currentIndex++));    // 현재 문자를 소비
        return currentIndex < string.length();           // 소비할 문자가 남아있으면 true를 리턴
    }

    /**
     * 반복될 자료구조를 분할하는 로직을 포함하므로 가장 중요한 메서드다.
     * RecursiveTask.compute 메서드 에서 했던 것 처럼 우선 분할 동작을 중단할 한계를 설정해야 한다.
     * 여기서는 10개의 문자를 사용했지만 실전에서는 너무 많은 태스크를 만들지 않도록 더 높은 한계값을 사용해야 할 것이다.
     * 분할이 필요한 상황에서는 파싱해야 할 문자열 청크의 중간 위치를 기준으로 분할하도록 지시한다.
     * 이때 단어 중간을 분할하지 않도록 빈 문자가 나올때까지 분할 위치를 이동시킨다.
     * 분할할 위치를 찾았으면 새로운 Spliterator를 만든다.
     * 새로 만든 Spliterator는 현재 위치 currentIndex 부터 분할된 위치까지의 문자를 탐색한다.
     */
    @Override
    public Spliterator<Character> trySplit() {
        int currentSize = string.length() - currentIndex;
        System.out.printf("string.length : %d , currentIndex : %d\n", string.length(), currentIndex);
        if (currentSize < 10) {
            return null;    // 파싱할 문자열을 순차 처리할 수 있을 만큼 충분히 작아졌음을 알리는 null을 반환
        }
        for (int splitPos = currentSize / 2 + currentIndex;
             splitPos < string.length() ; splitPos++) {     // 분할 시작 위치를 중간으로 지정
            if (Character.isWhitespace(string.charAt(splitPos))) {      // 다음 공백이 나올때까지 분할 위치를 뒤로 이동
                // 처음부터 분할 위치까지 문자열을 파싱할 새로운 WordCountSpliterator 를 생성
                Spliterator<Character> spliterator =
                        new WordCounterSpliterator(string.substring(currentIndex, splitPos));

                currentIndex = splitPos; // 시작 위치를 분할 위치로 설정
                return spliterator;     // 공백을 찾았고 문자열을 분리했으므로 루프를 종료
            }
        }
        return null;
    }

    /**
     * Spliterator가 파싱할 문자열 전체 길이와 현재 반복 중인 위치의 차이다.
     */
    @Override
    public long estimateSize() {
        return string.length() - currentIndex;
    }

    @Override
    public int characteristics() {
        return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
}

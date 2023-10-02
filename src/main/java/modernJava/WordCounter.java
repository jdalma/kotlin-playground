package modernJava;

/**
 * 스트림에 리듀싱을 실행하면서 단어 수를 계산할 수 있다.
 * 지금까지 발견한 단어 수를 계산하는 counter와 마지막 문자가 공백이였는지 여부를 기억하는 lastSpace 변수가 있다.
 */
public class WordCounter {
    private final int counter;
    private final boolean lastSpace;
    public WordCounter(int counter, boolean lastSpace) {
        this.counter = counter;
        this.lastSpace = lastSpace;
    }

    /**
     * WordCounter 클래스를 어떤 상태로 생성할 것인지 정의
     * 파라미터가 공백이라면 lastSpace 의 값을 보고 어떤 상태로 전이할지 정의한 것이다.
     */
    public WordCounter accumulate(Character c) {
        if (Character.isWhitespace(c)) {
            return lastSpace ? this : new WordCounter(counter, true);
        } else {
            return lastSpace ? new WordCounter(counter + 1, false) : this;
        }
    }

    /**
     * 문자열 서브 스트림을 처리한 WordCounter의 결과를 합친다.
     * WordCounter의 내부 counter값을 서로 합친다.
     */
    public WordCounter combine(WordCounter wordCounter) {
        return new WordCounter(
                counter + wordCounter.counter,
                wordCounter.lastSpace
        );
    }

    public int getCounter() {
        return this.counter;
    }
}

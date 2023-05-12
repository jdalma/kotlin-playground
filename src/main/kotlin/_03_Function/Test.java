package _03_Function;

public class Test {

    public static void main(String[] args) {
        MemoryClosure closure = param -> {
            final Integer[] memory = {param};
            return value2 -> {
                Integer result;
                if (value2 == 0) {
                    result = memory[0];
                } else {
                    Integer tmp = memory[0];
                    memory[0] = value2;
                    result = tmp;
                }
                return result;
            };
        };

        ClosureFunction function = closure.invoke(10);
        System.out.println(function.invoke(0));
        System.out.println(function.invoke(11));
        System.out.println(function.invoke(12));
    }
}

package _03_Function;

@FunctionalInterface
public interface MemoryClosure {
    ClosureFunction invoke(int value);
}

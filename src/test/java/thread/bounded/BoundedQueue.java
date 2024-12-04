package thread.bounded;

public interface BoundedQueue {
    boolean put(String data);
    String take();
}

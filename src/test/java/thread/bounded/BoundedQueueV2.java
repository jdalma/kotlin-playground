package thread.bounded;

import thread.MyLogger;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 *
 */
public class BoundedQueueV2 implements BoundedQueue {

    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;

    public BoundedQueueV2(int max) {
        this.max = max;
    }

    @Override
    public synchronized boolean put(String data) {
        if (queue.size() == max) {
            MyLogger.log("[생산 실패] 큐가 가득 참, 버림: " + data);
            return false;
        }
        queue.offer(data);
        return true;
    }

    @Override
    public synchronized String take() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.poll();
    }

    @Override
    public String toString() {
        return queue.toString();
    }
}

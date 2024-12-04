package thread.bounded;

import thread.MyLogger;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 생산자가 데이터를 생산할 때 버퍼가 꽉 차있으면 데이터를 버리고, 소비자가 데이터를 꺼내려할 때 데이터가 없으면 null을 반환한다.
 * 데이터 유실 가능성이 높다.
 */
public class BoundedQueueV1 implements BoundedQueue {

    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;

    public BoundedQueueV1(int max) {
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

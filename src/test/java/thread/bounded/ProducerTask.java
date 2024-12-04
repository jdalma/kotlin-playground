package thread.bounded;

import thread.MyLogger;

public class ProducerTask implements Runnable {

    private final BoundedQueue queue;
    private final String request;

    public ProducerTask(BoundedQueue queue, String request) {
        this.queue = queue;
        this.request = request;
    }

    @Override
    public void run() {
        MyLogger.log("[생산 시도] " + request + " -> " + queue);
        boolean result = queue.put(request);
        if (result) {
            MyLogger.log("[생산 완료] " + request + " -> " + queue);
        }
    }
}

package thread;

import java.time.LocalTime;

public class StateThread extends Thread {
    private final Object monitor;
    StateThread(Object monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            int i = 0;
            while (i < 10000) {
                // no-op
                i++;
            }
            synchronized (monitor) {
                // 이 monitor에 대한 주인이 된 후에 wait을 호출하면, 이 객체에 대한 잠금을 포기한다.
                // 그렇기에 다른 곳에서 이 monitor의 주인이 될 수 있다.
                monitor.wait();
                Thread.sleep(1000);
            }
            System.out.println(super.getName() + " is notified " + LocalTime.now());
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

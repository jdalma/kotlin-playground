package thread.runnable;

public class HelloRunnable implements Runnable {

    @Override
    public void run() {
        System.out.println("Hello Runnable " + Thread.currentThread().getName());
    }
}

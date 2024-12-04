package modernJava;

public class VolatileTest implements Runnable {

    volatile boolean runnable = true;
    int count = 0;

    @Override
    public void run() {
        while (runnable) {
            count++;
        }
        System.out.println(Thread.currentThread().getName() + " end, count = " + count);
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileTest task = new VolatileTest();
        Thread thread = new Thread(task);

        thread.start();

        Thread.sleep(500);
        task.runnable = false;
        System.out.println("task runnable false, count = " + task.count);
    }
}

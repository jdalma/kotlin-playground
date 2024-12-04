package thread;

import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import static thread.MyLogger.*;

public class PrinterTest {

    public static void main(String[] args) {
        MyPrinter myPrinter = new MyPrinter();
        Thread thread = new Thread(myPrinter, "printer");

        thread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            log("입력하세요.");
            String s = scanner.nextLine();
            if (Objects.equals(s, "q")) {
                thread.interrupt();
                break;
            }
            myPrinter.addMsg(s);
        }
    }

    private static class MyPrinter implements Runnable {
        private final Queue<String> jobQueue = new ConcurrentLinkedQueue<>();

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                if (jobQueue.isEmpty()) {
                    Thread.yield();
                    continue;
                }

                String msg = jobQueue.poll();
                log("출력 시작 " + msg + ", 대기 건수 : " + jobQueue.size());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log("프린터 인터럽트 발생 !!!");
                    break;
                }
                log("출력 완료");
            }
            log("프린터 종료");
        }

        public void addMsg(String s) {
            this.jobQueue.add(s);
        }
    }
}

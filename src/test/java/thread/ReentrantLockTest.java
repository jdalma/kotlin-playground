package thread;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static thread.MyLogger.log;

public class ReentrantLockTest {

    @Test
    void reentrantLock() throws InterruptedException {
        Account account = new Account(5000);

        Thread thread1 = new Thread(new WithdrawTask(account, 3000));
        Thread thread2 = new Thread(new WithdrawTask(account, 4000));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        log("잔액 : " + account.getBalance());
        Assertions.assertThat(account.getBalance()).isIn(1000L, 2000L);
    }

    static class Account {
        private final Lock lock = new ReentrantLock();
        private long balance;

        public Account(long balance) {
            this.balance = balance;
        }

        public void withdraw(long amount) throws InterruptedException {
            lock.lock();    // ReentrantLock을 이용하여 lock을 건다.

            try {
                if (this.balance < amount) {
                    log("출금 실패 : " + amount);
                    return;
                }
                log("출금 실행 : " + amount);
                this.balance -= amount;
            } finally {
                lock.unlock();
            }
        }

        public long getBalance() {
            lock.lock();
            try {
                return this.balance;
            } finally {
                lock.unlock();
            }
        }
    }

    static class WithdrawTask implements Runnable {
        private final Account account;
        private final long amount;

        public WithdrawTask(Account account, long amount) {
            this.account = account;
            this.amount = amount;
        }

        @Override
        public void run() {
            try {
                log("출금 실행 전 잔액 : " + account.getBalance());
                this.account.withdraw(amount);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

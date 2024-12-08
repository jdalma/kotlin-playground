package thread;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static thread.MyLogger.*;

public class SynchronizedTest {

    @Test
    void test() throws InterruptedException {
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
        private long balance;

        public Account(long balance) {
            this.balance = balance;
        }

        public synchronized void withdraw(long amount) {
            if (this.balance < amount) {
                log("출금 실패 : " + amount);
                return;
            }
            log("출금 실행 : " + amount);
            this.balance -= amount;
        }

        public long getBalance() {
            return this.balance;
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
            this.account.withdraw(amount);
        }
    }
}

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class p1_sistema_bancario {
    
    public static class BankAcc {
        private int saldo;
        private final Lock accLock = new ReentrantLock();

        public BankAcc(int saldo){
            this.saldo = saldo;
        }

        public void deposito(int value) {
            System.err.println("tentando acessar...");
            accLock.lock();
            try { 
                saldo += value;
                System.err.println("depositado " +value);
                System.out.println("saldo apos deposito: "+saldo);
            } finally {
                accLock.unlock();
            }
        }
        
        public void saque(int value) {
            System.err.println("tentando acessar...");
            accLock.lock();
            try { 
                if (saldo>value){
                    saldo-=value;
                    System.err.println("saque realizado: "+value);
                    System.out.println("saldo apos o saque: "+saldo);
                }
                else {
                    System.out.println("Sem saldo ("+saldo+") suficiente para realizar saque de: "+value);
                }                
            } finally {
                accLock.unlock();
            }
        }
    }
    
    public static void main(String[] args) {
        BankAcc acc = new BankAcc(1000);
        Thread pessoa1 = new Thread((() -> acc.deposito(500)));
        Thread pessoa2 = new Thread((() -> acc.deposito(400)));
        Thread pessoa3 = new Thread((() -> acc.saque(1111)));
        Thread pessoa4 = new Thread((() -> acc.saque(800)));

        pessoa1.start();
        pessoa2.start();
        pessoa3.start();
        pessoa4.start();
    }
}

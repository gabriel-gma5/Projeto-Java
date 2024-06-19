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
                if (saldo>=value){
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
        Thread client1 = new Thread((() -> acc.deposito(500)));
        Thread client2 = new Thread((() -> acc.deposito(400)));
        Thread client3 = new Thread((() -> acc.saque(1111)));
        Thread client4 = new Thread((() -> acc.saque(800)));
        Thread client5 = new Thread((() -> acc.deposito(11)));

        client1.start();
        client2.start();
        client3.start();
        client4.start();
        client5.start();

        // **Teste de codigo com mais manipulacoes**
        // for(int i = 0; i<10; i++){
        //     int j = i+1;
        //     int rdAmount = (int) (Math.random()*(200-100+1)+100); //gera valores inteiros no intervalo [100,200]
        //     if (i%2 == 0) { // par -> deposito
        //         new Thread (() -> acc.deposito((j)*rdAmount)).start();
        //     }
        //     else{ // impar -> saque
        //         new Thread (() -> acc.saque((j)*rdAmount)).start();
        //     }
        // }
    }
}
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class p1_banco {

    public static class BankAcc {
        public int saldo;
        private final Lock accLock = new ReentrantLock();

        public BankAcc(int saldo){
            this.saldo = saldo;
        }

        public void deposito(int value) throws InterruptedException {
            System.err.println(Thread.currentThread().getName()+" tentando acessar..."); 
            Thread.sleep(100);
            accLock.lock();
            System.out.println("\nThread travada: "+Thread.currentThread().getName());
            try { 
                saldo += value;
                Thread.sleep(700);
                System.err.println("Depositado: " +value+" | Saldo atual: "+saldo);
            } finally {
                accLock.unlock();
            }
        }

        public void saque(int value) throws InterruptedException {
            System.err.println(Thread.currentThread().getName()+" tentando acessar...");
            Thread.sleep(100);
            accLock.lock();
            System.out.println("\nThread travada: "+Thread.currentThread().getName());
            try { 
                Thread.sleep(700);
                if (saldo>=value){
                    saldo-=value;
                    System.err.println("Saque realizado: "+value+" | Saldo atual: "+saldo);
                }
                else {
                    System.out.println("Sem saldo suficiente ("+saldo+") para realizar saque de: "+value);
                }                
            } finally {
            accLock.unlock();
            }
        }
    }

    
    public static class Deposito implements Runnable {
        private final int amount; 
        private final BankAcc account;
        public Deposito(int amnt, BankAcc acc){amount = amnt; account = acc;}
        @Override public void run(){
        try {
            account.deposito(amount);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
            }
        }
    }

    public static class Saque implements Runnable {
        private final int amount; 
        private final BankAcc account;
        public Saque(int amnt, BankAcc acc){amount = amnt; account = acc;}
        @Override public void run(){    
        try {
            account.saque(amount);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        BankAcc acc = new BankAcc(1000);
        
        // **Teste conciso de codigo** 
        // Thread client0 = new Thread(new Deposito(11, acc));
        // Thread client1 = new Thread(new Deposito(500, acc));
        // Thread client2 = new Thread(new Deposito(400, acc));
        // Thread client3 = new Thread(new Saque(1111, acc));
        // Thread client4 = new Thread(new Saque(800, acc));
        
        // client0.start();
        // client1.start();
        // client2.start();
        // client3.start();
        // client4.start();
        
        // client0.join();
        // client1.join();
        // client2.join();
        // client3.join();
        // client4.join();
        
        
        // **Teste de codigo com mais manipulacoes**
        int max = 200, min = 100;
        Thread[] clients = new Thread[15];
        for(int i = 0; i<clients.length; i++){ // numero igual de saques e depositos
            //gera valores inteiros no intervalo [100,200] e multiplica por i+1
            final int rdAmount = (i+1)*((int)(Math.random()*(max-min+1)+min)); 
            if (i%2 == 0) {
                clients[i] = new Thread(new Deposito(rdAmount, acc));
            }
            else{ 
                clients[i] = new Thread(new Saque(rdAmount, acc));
            }
        }

        for (Thread client : clients) {client.start();}
        
        for (Thread client : clients) {client.join();}

        System.out.println("\nSaldo final apos operacoes: "+acc.saldo);
    }
}
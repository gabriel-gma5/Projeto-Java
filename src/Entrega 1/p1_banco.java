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
            System.err.println(Thread.currentThread().getName()+" tentando acessar conta..."); 
            accLock.lock();
            Thread.sleep(1); //sleep curto para alinar output (tentativas de acesso -> Operacoes)
            System.out.println("\nThread pronta para depositar: "+Thread.currentThread().getName());
            try { 
                saldo += value;
                Thread.sleep(700);
                System.err.println("Depositado: " +value+" | Saldo atual: "+saldo);
            } finally {
                accLock.unlock();
            }
        }

        public void saque(int value) throws InterruptedException {
            System.err.println(Thread.currentThread().getName()+" tentando acessar conta...");
            accLock.lock();
            Thread.sleep(1); //sleep curto para alinar output (tentativas de acesso -> Operacoes)
            System.out.println("\nThread pronta para sacar: "+Thread.currentThread().getName());
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
        
        Thread[] clients = new Thread[10];
        int rdAmount, max = 200, min = 100;
        for(int i = 0; i<clients.length; i++){ 
            //gera valores inteiros no intervalo [100,200] e multiplica por i+1
            rdAmount = (i+1)*((int)(Math.random()*(max-min+1)+min)); 
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
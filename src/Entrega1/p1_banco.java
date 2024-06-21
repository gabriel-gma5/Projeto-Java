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
            Thread.sleep(1); //sleep curto apenas para alinar output (tentativas de acesso -> Operacoes)
            System.out.println("\nCliente pronto para depositar: "+Thread.currentThread().getName());
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
            System.out.println("\nCliente pronto para sacar: "+Thread.currentThread().getName());
            try { 
                Thread.sleep(800);
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
    
    public static class Client extends Thread {
        private final int amount; 
        private final BankAcc account;
        private final boolean isDepo;
        public Client(int amnt, BankAcc acc, boolean isDeposit){
            amount = amnt; 
            account = acc;
            isDepo = isDeposit;
        }
        @Override public void run(){    
            try {
                if(isDepo) {account.deposito(amount);}
                else {account.saque(amount);}   
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        BankAcc acc = new BankAcc(1000);
        
        int testNums = 10;
        Thread[] clients = new Thread[testNums];
        int rdAmount, max = 200, min = 100;
        for(int i = 0; i<clients.length; i++){ 
            //gera valores inteiros no intervalo [100,200] e multiplica por i+1
            rdAmount = (i+1)*((int)(Math.random()*(max-min+1)+min)); 
            if (i%2 == 0) {
                clients[i] = new Client(rdAmount, acc, true);
            }
            else{ 
                clients[i] = new Client(rdAmount, acc, false); 
            }
            clients[i].setName("Cliente-"+i);
        }

        for (Thread client : clients) {client.start();}
        
        for (Thread client : clients) {client.join();}

        System.out.println("\nSaldo final apos operacoes: "+acc.saldo);
    }
}
package Entrega1;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class p2_ponte_com_sincronizacao {

    public static class Ponte{
        private final Lock lock = new ReentrantLock();

        public void atravessarPonte(String sentido) throws InterruptedException {
            System.out.println("Vem aí um carro (" + Thread.currentThread().getName()+ ") da " + sentido + " querendo atravessar.");
            lock.lock();
            try { 
                System.out.println("Tem um carro (" + Thread.currentThread().getName()+ ") atravessando a ponte.");
            } finally{
                Thread.sleep(2000);
                System.out.println("O carro atravessou a ponte com sucesso!");
                lock.unlock();
            }
        }
    }
  
    public static class Carro implements Runnable{
        private final String sentido; //sentido diz se vem da direita ou esquerda
        private final Ponte ponte;

        public Carro(String sentido, Ponte ponte){
            this.sentido = sentido;
            this.ponte = ponte;
        }

        public void run() {
            try {
                this.ponte.atravessarPonte(this.sentido);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Ponte ponte = new Ponte();
        for (int i = 0; i < 10; i++) {
            new Thread(new Carro(i % 2 == 0 ? "esquerda":"direita", ponte)).start();
        }
    }
}

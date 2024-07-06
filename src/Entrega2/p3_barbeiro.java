package Entrega2;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Barbearia {
    private final int numCadeiras;
    private final Semaphore cadeirasDisponiveis;
    private final Semaphore barbeiroSem = new Semaphore(1);
    private final Semaphore clienteSem = new Semaphore(1);
    private final Lock lock = new ReentrantLock();
    private final Lock dormirlock = new ReentrantLock();
    private boolean barbeiroDormindo = true;
    private int clientesVist = 0;

    public Barbearia(int numCadeiras) {
        this.numCadeiras = numCadeiras; 
        this.cadeirasDisponiveis = new Semaphore(numCadeiras);
    }

    public void cortarCabelo(String cliente) throws InterruptedException {
        Thread.sleep(600);
        lock.lock();
        if (cadeirasDisponiveis.tryAcquire()) {
            lock.unlock();
            dormirlock.lock();
            if (barbeiroDormindo) {
                System.out.println(cliente + " acorda o barbeiro e senta em uma cadeira de espera.");
                barbeiroDormindo = false;
            } else {
                System.out.println(cliente + " conseguiu uma cadeira");
            }
            dormirlock.unlock();

            barbeiroSem.release();
            clienteSem.acquire();
            System.out.println(cliente + " está cortando o cabelo");
            System.out.println(cliente + " terminou de cortar o cabelo");
        } else {
            lock.unlock();
            Thread.sleep(500);
            System.out.println(cliente + " sai da loja porque não há cadeiras disponíveis");
        }
        clientesVist+=1;
    }

    public void barbeiro(int clientes) throws InterruptedException {
        while (clientesVist<clientes) {
            barbeiroSem.acquire();    
            lock.lock();
                if (cadeirasDisponiveis.availablePermits() == numCadeiras) {
                    lock.unlock();
                    barbeiroDormindo = true;
                    System.out.println("Barbeiro dormindo... ");
                    Thread.sleep(500);
                }
                else{
                    lock.unlock();
                    System.out.println("Barbeiro cortando cabelo");
                    cadeirasDisponiveis.release();
                    clienteSem.release();
                    
                }
            if (!barbeiroDormindo) {
                barbeiroSem.release();
            }
        }
    }
}

class Cliente extends Thread {
    private Barbearia barb;
    public Cliente(Barbearia barbearia) {
        barb = barbearia;
    }

    @Override
    public void run() {
        try {
            barb.cortarCabelo(getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class p3_barbeiro {

    public static void main(String[] args) {
        final int cadeiras = 10, clientes = 50; 
        Barbearia barbearia = new Barbearia(cadeiras);
        Thread Barbeiro = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    barbearia.barbeiro(clientes);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Barbeiro.start();
        for (int i = 1; i <= clientes; i++) {
            Thread thread =  new Cliente(barbearia);
            thread.setName("Cliente-"+(i));
            thread.start();
        }
    }
}

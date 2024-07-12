package Entrega3;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;

class paradaOnibus {
    private final int limitePessoas = 50;
    private int pessoasParada = 0; //quantidade de pessoas na parada
    private int passageirosOnibus = 0;
    private Lock lock = new ReentrantLock();
    private Condition onibusChegou = lock.newCondition();
    private Semaphore capacidadeOnibus = new Semaphore(limitePessoas, true); //pra gerenciar quantos passageiros podem entrar

    //passageiro chega na parada
    public void chegouParada() throws InterruptedException {
        lock.lock();
        try {
            pessoasParada++;
            System.out.println("Agora tem " + pessoasParada + " pessoas na parada esperando.");
            onibusChegou.await(); //espera o onibus chegar
        } finally {
            lock.unlock();
        }
    }

    //passageiro tenta entrar no onibus
    public void entrarPassageiro() throws InterruptedException {
        capacidadeOnibus.acquire();
        lock.lock();
        try {
            pessoasParada--;
            System.out.println("Um passageiro acabou de entrar no ônibus.");
            passageirosOnibus++;
            if (pessoasParada == 0 ){
                onibusChegou.signalAll(); //indica que caso não tenha mais ninguem na parada, é pro onibus partir
                System.out.println("O ônibus recebeu ordem de partir, e vai sair com " + passageirosOnibus + " passageiros.");
            }else if (passageirosOnibus == limitePessoas){
                System.out.println("O ônibus recebeu ordem de partir, e vai sair com " + passageirosOnibus + " passageiros.");
            }
        } finally {
            lock.unlock();
        }
    }

    //onibus chega na parada
    public void onibusChegou() throws InterruptedException {
        lock.lock();
        try {
            System.out.println("Um ônibus acabou de chegar na parada, que tem " + pessoasParada + " passageiros esperando.");
            onibusChegou.signalAll();
        } finally {
            lock.unlock();
        }

        //tempo que o onibus fica parado na parada
        //eita acho que tenho que ajeitar isso
        Thread.sleep(1000 + new Random().nextInt(2000));

        passageirosOnibus = 0;

        capacidadeOnibus.release(limitePessoas);
    }

    //quantidade de passageiros esperando na parada
    public int passageirosEsperando() {
        lock.lock();
        try {
            return pessoasParada;
        } finally {
            lock.unlock();
        }
    }
}

class Passageiro extends Thread {
    private paradaOnibus parada;

    public Passageiro(paradaOnibus parada) {
        this.parada = parada;
    }

    @Override
    public void run() {
        try {
            parada.chegouParada();
            parada.entrarPassageiro();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Onibus extends Thread {
    private paradaOnibus parada;

    public Onibus(paradaOnibus parada) {
        this.parada = parada;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (parada.passageirosEsperando() > 0) {
                    parada.onibusChegou();
                } else {
                    //quando o onibus chega e nao tem ninguem na parada
                    Thread.sleep(1000 + new Random().nextInt(2000));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class p5_transporte {
    public static void main(String[] args) {
        paradaOnibus parada = new paradaOnibus();

        Onibus bus = new Onibus(parada);
        bus.start();

        //100 passageiros chegando
        for (int i = 0; i < 100; i++) {
            System.out.println("Chegou mais um passageiro.");
            Passageiro passenger = new Passageiro(parada);
            passenger.start();

            //tempo entre passageiros
            try {
                Thread.sleep(new Random().nextInt(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

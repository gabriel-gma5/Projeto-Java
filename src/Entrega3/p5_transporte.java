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
    private Semaphore capacidadeOnibus = new Semaphore(limitePessoas, true); //pra gerenciar quantos passageiros podem entrar
    private Condition onibusChegada = lock.newCondition();
    private Condition onibusPartida = lock.newCondition();
    public int pessoasEmbarque = 0;

    //passageiro chega na parada
    public void chegouParada() throws InterruptedException {
        lock.lock();
        try {
            pessoasParada++;
            System.out.println(Thread.currentThread().getName() + " (Passageiro) chegou na parada. Agora tem " + pessoasParada + " pessoas na parada esperando pelo ônibus.");
            onibusChegada.await(); //espera o onibus chegar
        } finally {
            lock.unlock();
        }
    }

    //passageiro tenta entrar no onibus
    public void entrarPassageiro() throws InterruptedException {
        lock.lock();
        //ao tentar entrar, há a chance de não ter mais cadeiras; necessário esperar o próximo ônibus
        while (!capacidadeOnibus.tryAcquire())
        {onibusChegada.await();}

        try {
            pessoasParada--;
            System.out.println("Um passageiro saiu da parada e acabou de entrar no ônibus |  pessoas na parada: "+pessoasParada);
            passageirosOnibus++;
            pessoasEmbarque--;
            if (pessoasEmbarque == 0 || passageirosOnibus == limitePessoas){ //caso não tenha mais ninguem na parada ou lotem os assentos, é pro onibus partir
                System.out.println("O ônibus recebeu ordem de partir, e vai sair com " + passageirosOnibus + " passageiros.");
                onibusPartida.signal();
            }
        } finally {
            Thread.sleep(new Random().nextInt(15));
            lock.unlock();
        }
    }

    //onibus chega na parada
    public void onibusChegada() throws InterruptedException {
        lock.lock();
        try {
            System.out.println("Um ônibus acabou de chegar na parada, que tem " + pessoasParada + " passageiros esperando.");
            pessoasEmbarque = pessoasParada;
            onibusChegada.signalAll();
            System.out.println("Onibus esperando os passageiros subirem");
            onibusPartida.await();

            System.out.println("Ônibus partiu com " + passageirosOnibus + " pessoas nos assentos");

            capacidadeOnibus.release(passageirosOnibus);
            passageirosOnibus = 0;
        } finally {
            lock.unlock();
        }


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
                    parada.onibusChegada();
                } else {
                    System.out.println("Ônibus partindo. Sem pessoas na parada");
                    //quando o onibus chega e nao tem ninguem na parada
                }
                int val = 1000 + new Random().nextInt(2001);
                Thread.sleep(val);
                System.out.println("Outro ônibus chegou em " + val + " milissegundos");
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

        //passageiros chegando
        for (int i = 0; i < 200; i++) {
            Passageiro passenger = new Passageiro(parada);
            passenger.start();

            //tempo entre passageiros
            try {
                Thread.sleep(new Random().nextInt(100)+5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

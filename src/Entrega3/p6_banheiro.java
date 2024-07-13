package Entrega3;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Bathroom {
    private final int capacity = 3;
    private final Lock bathKey = new ReentrantLock();
    private final Lock genderlock = new ReentrantLock();
    private final Condition sameGender = bathKey.newCondition();
    private final Semaphore bathStalls = new Semaphore(3);
    private String currentGender = null;
    private int count = 0; 


    private void updateGend(String gender){
        genderlock.lock();
        if(currentGender==null | currentGender == gender){
            currentGender = gender;   
        }
        genderlock.unlock();
    }

    public void enter(Person person) throws InterruptedException{
        bathKey.lock();
        try {
            updateGend(person.gender);
            // Duas condicoes fazem uma pessoa esperar:
            while (  
                    bathStalls.availablePermits() == 0 // Nao existem cabines disponiveis    
                    | (bathStalls.availablePermits()>0 & currentGender!=null & currentGender != person.gender) 
                    // Ou existem cabines disponiveis, no entanto ha alguem com "gender" diferente e nao nulo no banheiro     
                ) {
                sameGender.await();
                updateGend(person.gender);
            }
            bathStalls.acquire();
            count++;
            System.out.println(person.getName() + " entrou  |   Pessoas no banheiro: " + count + "            "+ currentGender);
        } finally {
            bathKey.unlock();
        }
    }

    public void leave(Person person) {
        bathKey.lock();
        System.out.println(person.getName() + "   saindo");
        try {
            bathStalls.release();
            count--;
            System.out.println(person.getName() + " saiu    |   Pessoas no banheiro: " + count);
            if (bathStalls.availablePermits() == capacity) {
                currentGender = null;
                sameGender.signalAll();
            }
        } finally {
            bathKey.unlock();
        }
    }
}

class Person extends Thread {
    private final Bathroom bathroom;
    public final String gender;

    public Person(Bathroom bathroom, String gender) {
        this.bathroom = bathroom;
        this.gender = gender;
    }

    @Override
    public void run() {
        try {
            bathroom.enter(this);
            Thread.sleep((int) (Math.random() * 1200 + 100)); // simula o tempo que a pessoa passa no banheiro
            bathroom.leave(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

public class p6_banheiro {
    public static Random rdG = new Random();
    public static void main(String[] args) throws InterruptedException {
        Bathroom bathroom = new Bathroom();
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 40; i++) {
            String gender = ( rdG.nextInt((i+1)*11+2) % 2 == 0) ? "Homem" : "Mulher";
            Person t = new Person(bathroom, gender);
            t.setName(gender+"-"+i); 
            executor.execute(t);
            Thread.sleep(rdG.nextInt(201)+10);
        }
        executor.shutdown();
    }
}


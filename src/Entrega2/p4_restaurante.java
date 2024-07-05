package Entrega2;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * --- Criar metodo para ficar tentando adquirir uma cadeira ---
 *  - Precisa considerar quando estiverem cheias (0 disponiveis)
 *  - Precisa considerar a fila, o primeiro dela e se ja esta na fila 
*     - Cheque se o status "recentemente lotado" esta ativo:
*       - Nao ativo (5 clientes juntos ja terminaram de jantar ou ainda nao tiveram 5 clientes que jantaram juntos):
*           Tente adquirir uma cadeira disponivel (while 2 com reg.crit?)
*       - Ativo (5 clientes acabaram de comecar a jantar juntos (available=5) ou /
*               ainda tem clientes de um grupo de 5 que estao terminando de jantar (available<5)):
*           Va para a fila
*           -- OBS.: Esta segunda verificacao eh necessaria para manter justica com os clientes que
*                   que estao na fila. Caso contrario, toda vez que um cliente novo chegasse e
*                   tivesse uma cadeira disponivel, esse iria adquiri-la e os clientes que chegaram antes e estao
*                   esperando as 5 cadeiras liberarem (fila), iriam esperar ate que todos os clientes novos 
*                   terminassem de jantar para, entao, ter sua vez (starving).
*/

class Restaurant {
    public static int availableChairs = 5;
    public static Semaphore dinningSemaphore = new Semaphore(availableChairs, true);
    public static boolean recentlyFull = false;
    private static final int totalChairs = 5;
    private static final Lock mutex = new ReentrantLock();
    public static int servedCust = 0;
    public static int totalCust;     
    
    public static boolean tryGetChair(Customer customer){
        try {
            mutex.lock();
            if (!Restaurant.recentlyFull) {
                System.out.println(Thread.currentThread().getName()+" conseguiu uma cadeira e agora ira jantar");
                availableChairs-=1;
                if(availableChairs == totalChairs){recentlyFull = true;}
                // se esta na fila, entao sai
                if(customer.prev!=null | customer == WaitingQueue.tail){WaitingQueue.leaveQ();}
                return true;
            }else{
                // se nao esta na fila, entao entra
                if(customer.prev == null & customer!=WaitingQueue.tail){WaitingQueue.insert(customer);}
                System.out.println(Thread.currentThread().getName()+" nao conseguiu uma cadeira e vai ter que esperar na fila");
                return false;    
                }
            }
        finally{
            mutex.unlock();
            }
    }

    public static void dinner(Customer customer){
        try {
            dinningSemaphore.acquire();
            System.out.println(Thread.currentThread().getName()+" Esta jantando");
            Thread.sleep(100);
            System.out.println(Thread.currentThread().getName()+" terminou de jantar");
            mutex.lock();
                availableChairs+=1;
                if(availableChairs == 0){recentlyFull = false;}
                servedCust+=1;
            mutex.unlock();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally{
            dinningSemaphore.release();
        }
    }
}

class WaitingQueue extends Thread {
    public static Customer head = null, tail = null;
    
    public static Customer leaveQ(){
        Customer first = head;
        head = head.prev; 
        return first;
    }
    public static void insert(Customer newC){
        if(head == null){
            head = newC;
            tail = newC;}
        else{
            tail.prev = newC;
            tail = newC;}
    }

    @Override
    public void run(){
        while(Restaurant.servedCust<Restaurant.totalCust){
            while(head!=null){
                head.run();
            }
        }
    }
} 

class Customer extends Thread {
    public Customer prev = null;
    @Override
    public void run(){
        if(Restaurant.tryGetChair(this)){
            Restaurant.dinner(this);
        };
    }
}


public class p4_restaurante {
    public static void main(String[] args) throws InterruptedException {
        Restaurant.totalCust = 20;
        WaitingQueue queue = new WaitingQueue();
        Customer[] customers = new Customer[Restaurant.totalCust];
        for (int i = 0; i < Restaurant.totalCust; i++) {
            customers[i] = new Customer();
            customers[i].setName("Cliente-"+(i+1));
        }
        
        queue.start();
        for (Customer customer : customers) {
            customer.start();
        }    
       
        queue.join(); 
        System.out.println("Todos os clientes foram atendidos.");
       
    }
}
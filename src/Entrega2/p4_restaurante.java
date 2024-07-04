package Entrega2;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * --- Criar metodo para ficar tentando adquirir uma cadeira ---
 *  - Precisa considerar quando estiverem cheias (0 disponiveis):
*   - Se cheia: espera ter 5 disponiveis (fila - while1 com reg.crit?)    
*   - Caso contrario:
*     - cheque se o status "recentemente lotado" esta ativo:
*       - Nao ativo (5 clientes juntos ja terminaram de jantar):
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
    public static Semaphore dinningSemaphore = new Semaphore(5, true);
    public static int availableChairs = 5;
    private static final int total = 5;
    public static boolean recentlyFull = false;
    private static final Lock mutex = new ReentrantLock();
    public static void tryGetChair(Customer customer){
        try {
            mutex.lock();
            if (!Restaurant.recentlyFull) {
                System.out.println(Thread.currentThread().getName()+" conseguiu uma cadeira e agora ira jantar");
                availableChairs+=1;
                if(availableChairs == total){recentlyFull = true;}
            else{
                WaitingQueue.insert(customer);
                System.out.println(Thread.currentThread().getName()+" nao conseguiu uma cadeira e vai ter que ir para a fila");
                }
            }
            else{
            }
        }
        finally{
            mutex.unlock();
            }
        }

    public static void dinner(Customer customer){

    }
}

class WaitingQueue extends Thread {
    private static Customer head = null, tail = null;
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
        head.run();
        
    }
} 

class Customer extends Thread {
    public Customer prev = null, next = null;
    private Restaurant rest_;
    private WaitingQueue queue;
    public Customer(Restaurant Rest_, WaitingQueue Queue){
        this.rest_ = Rest_;
        this.queue = Queue;
    }
    @Override
    public void run(){
        Restaurant.tryGetChair(this);
    }
}


public class p4_restaurante {
    public static void main(String[] args) {
        
    }
}

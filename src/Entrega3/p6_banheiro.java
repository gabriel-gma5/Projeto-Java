package Entrega3;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Bathroom {
    private final int capacity = 3;
    private final Lock bathKey = new ReentrantLock();
    private final Condition sameGender = bathKey.newCondition();
    private final Semaphore bathStalls = new Semaphore(3);
    private final List<Person> queue = new LinkedList<>(); 
    private String currentGender = null;
    private int count = 0; 
    

    private void updateGend(String gender){
        if(currentGender == null){
            currentGender = gender;   
        }
    }

    public void enter(Person person) throws InterruptedException{
        bathKey.lock();
        try {
            updateGend(person.gender);
            if (count == capacity || person.gender!=currentGender) {
                System.out.println(person.getName()+"  entrou na fila");
                queue.add(person);
                while ( // Duas condicoes fazem uma pessoa esperar:
                        bathStalls.availablePermits() == 0 // Nao existem cabines disponiveis    
                        || (currentGender!=null & currentGender != person.gender) // Ou ha alguem com "gender" diferente e nao nulo no banheiro 
                        || queue.getFirst() != person // Sai da fila, em qualquer um dos casos, somente se for o primeiro
                    ) {
                    sameGender.await();
                    updateGend(person.gender);
                }
                queue.removeFirst();
                System.out.println("\n"+person.getName()+"   saiu da fila e vai ao banheiro...");
            }
            bathStalls.acquire();
            count++;
            System.out.println(person.getName() + "   entrou  |   Pessoas no banheiro: " + count + "       |      ("+ currentGender+" entrou)");
        } finally {
            bathKey.unlock();
        }
    }

    public void leave(Person person) {
        bathKey.lock();
        System.out.println("\n"+person.getName() + "   saindo do banheiro...");
        try {
            bathStalls.release();
            count--;
            System.out.println(person.getName() + "   saiu    |   Pessoas no banheiro: " + count);
            if (bathStalls.availablePermits() == capacity) {
                currentGender = null;
                sameGender.signalAll();
            }
            else if(!queue.isEmpty()){
                if(queue.getFirst().gender == currentGender)
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
    private final Random personrd = new Random();
    
    public Person(Bathroom bathroom, String gender) {
        this.bathroom = bathroom;
        this.gender = gender;
    }

    @Override
    public void run() {
        try {
            bathroom.enter(this);
            if (personrd.nextInt(11)% 2 == 0) { // simula o tempo que a pessoa passa no banheiro  
                Thread.sleep(personrd.nextInt(101)+50);               
            }
            else {
                Thread.sleep(personrd.nextInt(1101)+100);                            
            }
            bathroom.leave(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class p6_banheiro {
    
    public static void main(String[] args) throws InterruptedException {
        Bathroom bathroom = new Bathroom();
        Random rdG = new Random();
        int tests = 100;
        Thread[] a = new Person[tests];

        for (int i = 1; i < tests+1; i++) {
            String gender = (rdG.nextInt(i*11+2) % 2 == 0) ? "Homem" : "Mulher";
            Thread t = new Person(bathroom, gender);
            t.setName(gender+"-"+i);
            t.start();
            a[i-1] = t;
            Thread.sleep(rdG.nextInt(301)+10);
        } 
        
        for (Thread person : a) {
            person.join();
        }

        System.out.println("Todas as pessoas foram ao banheiro!");

    }
}
/* Caracteristicas do codigo
 * - Existe uma fila gerenciada pelas proprias threads com Condition
 * - Existe um tempo variavel entre a chegada das pessoas ao banheiro
 * - O tempo que uma pessoa pode passar no banheiro varia
 *      - o tempo pode vir de um intervalo grande ou pequeno
 *        -- nao necessariamente o intervalo grande gera um valor maior que o do intervalo pequeno
 * - Quem esta na fila pode ser avisado a entrar por quem acabou de sair
 *      - Nesse caso, o atual primeiro da fila tera sua vez 
 * - Quando o banheiro esta vazio, "currentGender" eh null
 *      - O ultimo a sair sempre avisa aos demais da fila 
 * - Pessoas que acabaram de chegar podem entrar imediatamente: 
 *      - cabines vazias = 3 ou 
 *      - cabines vazias>0 e currentGender = gender       
 */


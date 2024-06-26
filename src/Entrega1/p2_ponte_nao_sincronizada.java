package Entrega1;


public class p2_ponte_nao_sincronizada {
                
    public static class Bridge {
        public int left;
        public int right;
    }

    public static class Carro extends Thread{
        private final String sentido; //sentido diz se veio da direita ou esquerda
        private final Bridge ponte;
        public Carro(String sentido, Bridge ponte){
            this.sentido = sentido;
            this.ponte = ponte;
        }

        public void run() {
            try {
                atravessar(ponte);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void atravessar(Bridge ponte) throws InterruptedException {
            System.out.println("Vem aí um carro (" + Thread.currentThread().getName()+ ") da " + sentido + ".");
            
            if(sentido == "esquerda") {ponte.left +=1;}
            else {ponte.right += 1;}
            Thread.sleep(0,100000);

            int otherSideStatus = (sentido == "esquerda")? ponte.right : ponte.left;
        
            Thread.sleep(1000);
            if(otherSideStatus<1){
                System.out.println("O carro (" + Thread.currentThread().getName()+ ") atravessou a ponte com sucesso!");
            }
            else{
                System.out.println(Thread.currentThread().getName()+" tentou atravessar da "+sentido+" mas tinha "+otherSideStatus+" carros atrapalhando!");
            }
            if(sentido == "esquerda") {ponte.left -=1;}
            else {ponte.right -= 1;}
        }
    }


    public static void main(String[] args) {
        Bridge ponte = new Bridge();
        Thread carro;
        for (int i = 0; i < 10; i++) {
            carro = new Carro(i % 2 == 0 ? "esquerda":"direita", ponte);
            carro.setName("Carro-"+i);
            carro.start();
        }
        // os carros, por não estarem sincronizados, não conseguem atravessar a ponte um de cada vez.
        // então, ao tentar atravessar a ponte sem garantir a sua vez, ocorre um deadlock
        // em que nenhum deles libera o espaço (subtrai contador do seu lado) para o próximo passar
    }
}

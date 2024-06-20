public class p2_ponte_nao_sincronizada {
    public static class Carro implements Runnable{
        private final String sentido; //sentido diz se veio da direita ou esquerda

        public Carro(String sentido){
            this.sentido = sentido;
        }

        public void run() {
            try {
                atravessar();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void atravessar() throws InterruptedException {
            System.out.println("Vem aí um carro (" + Thread.currentThread().getName()+ ") da " + sentido + ".");
            Thread.sleep(1000);
            System.out.println("O carro (" + Thread.currentThread().getName()+ ") atravessou a ponte com sucesso!");
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(new Carro(i % 2 == 0 ? "esquerda":"direita")).start();
        }
        // os carros não sincronizam, então todos atravessam ao mesmo tempo.
        // isto é, depois de 1000ms é mostrado que todos eles passaram, o que não deveria ser possível.
        // o comportamento esperado era o de um carro atravessar por vez.
    }
}

import java.util.concurrent.Semaphore;

class Barbearia {
    private static final int MAX_CLIENTES = 2; // Maximo de clientes que podem se sentar em cadeiras
    private Semaphore semCadeira; // Semáforo para controlar acesso à cadeira do barbeiro
    private Semaphore semBarbeiro; // Semáforo para indicar disponibilidade de barbeiros
    private Semaphore semMutex; // Semáforo para garantir exclusão mútua em operações críticas
    private int clientesEsperando; // Contador de clientes esperando
    private int clientesRejeitados; // Contador de clientes rejeitados

    // Construtor da barbearia
    public Barbearia() {
        semCadeira = new Semaphore(1);
        semBarbeiro = new Semaphore(0);
        semMutex = new Semaphore(1);
        clientesEsperando = 0;
        clientesRejeitados = 0;
    }

    // O barbeiro corta o cabelo de um cliente
    public void cortarCabelo(Cliente cliente) throws InterruptedException {
        System.out.println("O cliente " + cliente.getId() + " chegou ");
        semMutex.acquire();
        if (clientesEsperando < MAX_CLIENTES) { // Verifica se ha lugar na barbearia
            clientesEsperando++;
            semMutex.release();

            semCadeira.acquire();
            System.out.println("");
            System.out.println("  >  Cliente " + cliente.getId() + " sentou na cadeira do barbeiro.");
            semBarbeiro.release();

            // Barbeiro corta o cabelo
            Thread.sleep(1000);

            semCadeira.release();
            System.out.println("  >  Cliente " + cliente.getId() + " foi antendido e saiu da cadeira do barbeiro.");
            System.out.println(" -- Uma cadeira foi liberada -- ");
            System.out.println("");
        } else { // Não há lugar na barbearia, logo rejeita o cliente e incrementa o contador de rejeições
            clientesRejeitados++; 
            semMutex.release();
            System.out.println("");
            System.out.println("  >  Cliente " + cliente.getId() + " foi embora, não há lugar na barbearia.");
            System.out.println("     Total de clientes rejeitados: " + clientesRejeitados);
            System.out.println("");
        }
    }

    // O barbeiro atende um cliente (simula o tempo de cortar o cabelo)
    public void atenderCliente(Barbeiro barbeiro) throws InterruptedException {
        semBarbeiro.acquire();
        semMutex.acquire();

        clientesEsperando--;
        System.out.println("Barbeiro " + barbeiro.getId() + " está cortando o cabelo de um cliente.");

        semMutex.release();
        Thread.sleep(1500); // Simulando o tempo que o barbeiro leva para cortar o cabelo

        System.out.println("Barbeiro " + barbeiro.getId() + " terminou de cortar o cabelo.");
    }

    // Retorna o total de clientes que foram rejeitados até então
    public int getClientesRejeitados() {
        return clientesRejeitados;
    }
}

// Classe que representa um cliente
class Cliente implements Runnable {
    private int id;
    private Barbearia barbearia;

    // Construtor do cliente
    public Cliente(int id, Barbearia barbearia) {
        this.id = id;  // ID do cliente, adquirido na ordem da sua entrada
        this.barbearia = barbearia; // Referência para a barbearia
    }

    // Retorna o ID do cliente
    public int getId() {
        return id;
    }

    // Ao ser atendido, requisita um corte de cabelo
    @Override
    public void run() {
        try {
            barbearia.cortarCabelo(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Classe que representa um barbeiro
class Barbeiro implements Runnable {
    private int id; // ID do barbeiro
    private Barbearia barbearia; // Referência para a barbearia

    public Barbeiro(int id, Barbearia barbearia) {
        this.id = id; 
        this.barbearia = barbearia;
    }

    // Retorna o ID do barbeiro
    public int getId() {
        return id;
    }

    // Ao ser acordado, atende um cliente
    @Override
    public void run() {
        try {
            while (true) {
                barbearia.atenderCliente(this);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("-----------------------------------");
        System.out.println("Bem vindo à Barbearia Thales Cortes");
        System.out.println("-----------------------------------");
        
        Barbearia barbearia = new Barbearia();

        Thread[] clientes = new Thread[5];
        Thread[] barbeiros = new Thread[1];

        for (int i = 0; i < barbeiros.length; i++) {
            barbeiros[i] = new Thread(new Barbeiro(i + 1, barbearia));
            barbeiros[i].start();
        }

        for (int i = 0; i < clientes.length; i++) {
            clientes[i] = new Thread(new Cliente(i + 1, barbearia));
            clientes[i].start();

            try {
                // Tempo entre a chegada de clientes
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

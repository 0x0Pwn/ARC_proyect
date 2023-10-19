package proyectoarc;

import java.util.concurrent.locks.ReentrantLock;

public class Recurso {
    private int clientesRestantes;
    private long tiempo;
    public Recurso(int clientes) {
        clientesRestantes = clientes;
        tiempo = 0;
    }

    public synchronized void accederVariable() {
        // En este punto, solo un hilo puede acceder a la variable compartida a la vez,
        // gracias al synchronized, un bloqueo basicamente
        System.out.println("Hilo " + Thread.currentThread().getId() + " esta¡ accediendo a la variable compartida.");
        // Realiza operaciones en la variable compartida y nos indica que el cliente ha
        // entrado y se ha consumido
        clientesRestantes--;
    }

    public int GetClientes() {
        return clientesRestantes;
    }
    
    public synchronized void Suma_Tiempo (long t){
        tiempo += t;
    }
    
    public double ObtenerTiempoMedio (int n){
        double aux = (tiempo)/(n);
        double aux2 = aux/1000.0;
        
        return (aux2) / 1000000000.0;
    }
}

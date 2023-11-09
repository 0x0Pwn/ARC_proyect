package Redes3;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class Recurso implements Serializable {
    private int clientesRestantes;
    private long tiempo ;
    private boolean acabar = false;
      private boolean continua = false;
    private int desc;

    public Recurso() {
        
        tiempo = 0;
        desc = 0;
    }

    public synchronized void accederVariable() {
        // En este punto, solo un hilo puede acceder a la variable compartida a la vez,
        // gracias al synchronized, un bloqueo basicamente
       
        // Realiza operaciones en la variable compartida y nos indica que el cliente ha
        // entrado y se ha consumido
        
            clientesRestantes--;
            System.err.println("Quedan: "+clientesRestantes);
            System.out.println(clientesRestantes);
     
    }
    
    public synchronized void  clientetot(int cl)
    {
        clientesRestantes = cl;
    }
    public synchronized void acabarCliente() {  
            desc++; 
    }
       public synchronized int retacabado() {  
            return desc; 
    }
       
    public synchronized int GetClientes() {
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
    
    
    public synchronized void Acabar(boolean ac){
        acabar = ac;
    }
     
     public synchronized void cont( boolean r){
        continua = r;
    }
     public synchronized boolean Retcont(){
        return continua;
    }
    public synchronized boolean RetAcabar(){
        return acabar;
    }
     
    public synchronized void Reenviar(Socket[][] matriz, int fila, Socket s, String mensaje) {
		for(int i = 0; i < matriz[fila].length; i++) {
			if (matriz[fila][i] != s) { // Esto es para ver que no te reenvias a ti mismo
                        
				try {

					if (!matriz[fila][i].isClosed()) {// Compruebo si no esta cerrado el socket

						DataOutputStream dataOutputStream = new DataOutputStream(matriz[fila][i].getOutputStream());
						dataOutputStream.writeUTF(mensaje);
					}
                                
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
}

package Redes3;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

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
    // Distribuir mensajes a traves de una red de clientes.
    // Para reenviar un mensaje a todos los sockets de una fila especifica en la matriz, menos el socket que origino el mensaje
    public synchronized void Reenviar(Socket[][] matriz, int fila, Socket s, String mensaje) {
            for (int i = 0; i < matriz[fila].length; i++) {
                    // Comprobamos si el socket actual no es el socket de origen
                    if (matriz[fila][i] != s) { // Esto es para ver que no te reenvias a ti mismo         
                        try {
                            // Antes de enviar, comprobamos si el socke no esta cerrado
                            if (!matriz[fila][i].isClosed()) {// Compruebo si no esta cerrado el socket
                                // Obtenemos el flujo de salida del socket actual y lo metemos en un DataOutputStream
                                DataOutputStream dataOutputStream = new DataOutputStream(matriz[fila][i].getOutputStream());
                                // Escribe el mensaje al flujo de salida del socket
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

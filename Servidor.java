/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Redes3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class Servidor extends Thread {
    
                private int nclientes, nvecinos, nciclos;
		private int clientesActuales = 0;
		private int filas = 0;
		private ArrayList<ClientHandler> lmanejadores = new ArrayList<>();
		private int columnas = 0;
		private Recurso r;
              
    public Servidor( Recurso rec){
        r = rec;
    }
    
    public void run() {
        
        try {
			ServerSocket serverSocket = new ServerSocket(10571, 1000, InetAddress.getByName("127.0.0.1"));
                        

			Scanner entrada = new Scanner(System.in);

			System.out.println("Numero de clientes en la simulacion (n) ");
			nclientes = entrada.nextInt();
                        r.clientetot(nclientes);

			System.out.println("Numero de vecinos (Divisor de n) ");
			nvecinos = entrada.nextInt();

			// En caso de que no sea divisible perfecto
			while (nclientes % nvecinos != 0) {
				System.out.println("Numero de vecinos (Divisor de n) ");
				nvecinos = entrada.nextInt();
			}
                        System.out.println("Numero de ciclos ");
			nciclos = entrada.nextInt();

			Socket[][] matriz = new Socket[nclientes / nvecinos][nvecinos];
                        
                        r.cont(true);
                        System.out.println(" Servidor activo.... ");
                        
                        while (clientesActuales < nclientes) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress() + " "
						+ clientSocket.getPort());
				DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                                
				matriz[filas][columnas] = clientSocket;

				++columnas;

				if (columnas == nvecinos) {
					columnas = 0;
					++filas;
				}

				dataOutputStream.writeUTF("" + clientesActuales + "," + nciclos + "," + (nvecinos - 1) * nciclos);
                              
				clientesActuales++;

			}
                        
                        
                        System.out.println("Cargando................");
                        Thread.sleep(2000);
                        
                        
                        for (int i = 0; i < (nclientes / nvecinos); i++) {
				for (int j = 0; j < nvecinos; j++) {
					ClientHandler manejador = new ClientHandler(matriz[i][j], nciclos, r, matriz);
                                       
					manejador.start();
					lmanejadores.add(manejador);

				}
			}
                        
                        while (r.GetClientes() > 0) {
				Thread.sleep(100);
                                
			}
                        r.Acabar(true);
                        
                        while( r.retacabado()!= nclientes)
                        {
                            Thread.sleep(2000);
                        }
                        
                        System.out.println("Todos los clientes han terminado su trabajo.");
                
                        
                        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   catch (InterruptedException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    }
    
    }
           
class ClientHandler extends Thread {
	private Socket clientSocket;
	private int clienteId;
	private int nciclos;
	Recurso rcompartido;
	Socket[][] matr;

	// Constructor
	public ClientHandler(Socket clientSocket, int nciclos, Recurso r, Socket[][] matr) {
		this.clientSocket = clientSocket;

		this.nciclos = nciclos;
		rcompartido = r;
		this.matr = matr;

	}

	public void PonMatriz(Socket[][] matr) {
		this.matr = matr;
	}

	@Override
	public void run() {
            try{
		try {
			DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
			String respuesta = "";

			dos.writeUTF("COMIENZO");

			while (rcompartido.GetClientes() > 0) {
				if (dis.available() != 0) {
					respuesta = dis.readUTF();
					System.out.println(respuesta);
                                        if(respuesta.equals("ACABO"))
                                        {
                                            rcompartido.accederVariable();
                                            
                                        }
                                        else 
                                        {
                                            rcompartido.Reenviar(matr, buscarSocketEnMatriz(clientSocket, matr), clientSocket, respuesta);
                                        }
				}
                        
			}
                } catch (SocketException w)
                {
                                rcompartido.accederVariable();
                }      
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int buscarSocketEnMatriz(Socket socket, Socket[][] matriz) {
		for (int i = 0; i < matriz.length; i++) {
			for (int j = 0; j < matriz[i].length; j++) {
				Socket s = matriz[i][j];
				if (s != null && !s.isClosed() && s.equals(socket)) {
					return i;
				}
			}
		}
		return 0;
	}

	
}

}

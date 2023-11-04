package Redes3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
	public static void main(String[] args) {
		int nclientes, nvecinos, nciclos;
		int clientesActuales = 0;
		int filas = 0;
		ArrayList<ClientHandler> lmanejadores = new ArrayList<>();
		int columnas = 0;
		Recurso r;
		try {
			ServerSocket serverSocket = new ServerSocket(10571, 1000, InetAddress.getByName("127.0.0.1"));
                        

			Scanner entrada = new Scanner(System.in);

			System.out.println("Numero de clientes en la simulacion (n) ");
			nclientes = entrada.nextInt();

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

			while (clientesActuales < nclientes) {
                            System.out.println("Clientes actuales:" + clientesActuales);
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
			r = new Recurso(clientesActuales);
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
                        
                        for (int i = 0; i < (nclientes / nvecinos); i++) {
				for (int j = 0; j < nvecinos; j++) {
                                    DataOutputStream dos = new DataOutputStream(matriz[i][j].getOutputStream());
                                    dos.writeUTF("FIN");
				}
			}
                       
			//serverSocket.close();
                        System.out.println("Todos los clientes han terminado su trabajo.");
                        String valorEnDecimal = String.format("%.10f", r.ObtenerTiempoMedio(nclientes));
                        System.out.println("Tiempo medio de los clientes: "+ valorEnDecimal + "s");
                
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Servidor en línea. Esperando conexiones...");
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

			while (rcompartido.GetClientes() > 0 && !(clientSocket.isClosed())) {
				if (dis.available() != 0) {
					respuesta = dis.readUTF();
					System.out.println(respuesta);
                                        if(respuesta.equals("ACABO") && rcompartido.GetClientes()>=0)
                                        {
                                            rcompartido.accederVariable();
                                        }
                                        else 
                                        {Reenviar(matr, buscarSocketEnMatriz(clientSocket, matr), clientSocket, respuesta);}
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

	public void Reenviar(Socket[][] matriz, int fila, Socket s, String mensaje) {
		for (int i = 0; i < matriz[fila].length; i++) {
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

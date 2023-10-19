package proyectoarc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Server {
	private static int clientesTerminados = 0;

	public static void main(String[] args) {
		/*
		 * EL SERVER TIENE QUE ENVIAR LA CANTIDAD DE CICLOS (MENSAJES QUE TIENE QUE
		 * RECIBIR CADA CLIENTE)
		 */
		try {
			int nclientes, nvecinos, nciclos;
			int clientesActuales = 0;
			int filas = 0;
			int columnas = 0;

			ServerSocket serverSocket = new ServerSocket(10578);
			System.out.println("Servidor en li­nea. Esperando conexiones...");

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

			// Creamos una matriz de sockets, basicamente para que cada cliente este en un
			// grupo distinto
			Socket[][] matriz = new Socket[nclientes / nvecinos][nvecinos];
			// Creamos un objeto Recurso, que mas adelante utilizaremos para que solo entre
			// un hilo cada vez
			Recurso r = new Recurso(nclientes);

			/**
			 * Este bucle es para rellenar la matriz con los sockets y para enviar a cada
			 * cliente su id y los ciclos a realizar
			 */
			while (clientesActuales < nclientes) {
				Socket clientSocket = serverSocket.accept(); // Espera y acepta conexiones entrantes
				System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress() + " "
						+ clientSocket.getPort()); // Confirmacion del cliente

				// Crear un hilo para manejar la conexion de este cliente
				Thread clientThread = new Thread(new ClientHandler(clientSocket, clientesActuales, nciclos, r, matriz));
				clientThread.start();

				//Llenamos la matriz
				matriz[filas][columnas] = clientSocket;

				//Para que pase a la siguiente columna
				++columnas;
				//Para hacer grupos
				if (columnas == nvecinos) {
					columnas = 0;
					++filas;
				}

				//Incrementamos contador para ver cuando acaba el while
				clientesActuales++;
			}

			System.out.println("COMIENZA LA SIMULACION!!!");
			while (r.GetClientes() > 0) {
				Thread.sleep(100); // Puedes ajustar este valor
			}

			System.out.println("Todos los clientes han terminado su trabajo.");
                        
                        String valorEnDecimal = String.format("%.10f", r.ObtenerTiempoMedio(nclientes));
                        System.out.println("Tiempo medio de los clientes: "+ valorEnDecimal + "s");
		} catch (

		IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class ClientHandler implements Runnable {
	private Socket clientSocket;
	private int clienteId;
	private int nciclos;
	Recurso rcompartido;
	Socket[][] matr;

	//Constructor
	public ClientHandler(Socket clientSocket, int clienteId, int nciclos, Recurso r, Socket[][] matr) {
		this.clientSocket = clientSocket;
		this.clienteId = clienteId;
		this.nciclos = nciclos;
		rcompartido = r;
		this.matr = matr;

	}

	@Override
	public void run() {
		try {
			// Obtener un flujo de entrada para recibir datos del cliente
			InputStream inputStream = clientSocket.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);

			// Obtener un flujo de salida para enviar datos al cliente
			OutputStream outputStream = clientSocket.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

			// Enviar al cliente su ID y la cantidad de ciclos a realizar
			dataOutputStream.writeUTF(clienteId + "," + nciclos + "," + (matr[0].length - 1));
			dataOutputStream.writeUTF("COMIENZO");

			String mensaje = "";

			
			while (rcompartido.GetClientes() > 0) {
				if (dataInputStream.available() > 0) { // Mientras que me llguen datos
					mensaje = dataInputStream.readUTF();

					int fila = buscarSocketEnMatriz(clientSocket, matr); //Busca la fila de ese socket (El grupo)
					Reenviar(matr, fila, clientSocket, mensaje);

					if (mensaje.equals("ACABO")) {
						rcompartido.accederVariable();
                                                mensaje = dataInputStream.readUTF();
                                                rcompartido.Suma_Tiempo(Long.valueOf(mensaje));
						System.out.println("Quedan: " + rcompartido.GetClientes());

					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	public static void Reenviar(Socket[][] matriz, int fila, Socket s, String mensaje) {
		for (int i = 0; i < matriz[fila].length; i++) {
			if (matriz[fila][i] != s) { //Esto es para ver que no te reenvias a ti mismo
				OutputStream outputStream;
				try {
					outputStream = matriz[fila][i].getOutputStream();
					if (!matriz[fila][i].isClosed()) {//Compruebo si no esta cerrado el socket
						System.out.println("Reenviendo al cliente de la fila " + fila + " y columna " + i);
						DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
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

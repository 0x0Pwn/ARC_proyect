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

public class Server 
{
	public static void main(String[] args) 
	{
		int nclientes, nvecinos, nciclos;
		// Contador para los clientes conectados actualmente
		int clientesActuales = 0;
		// Para gestionar la fila/columna actual en una matriz de Sockets
		int filas = 0;
		int columnas = 0;
		// Para almacenar los Sockets
		ArrayList<ClientHandler> lmanejadores = new ArrayList<>();
		
		Recurso r;
		try 
		{
			// Creamos un ServerSocket que escuche el puerto 10571 con un backlog max = 1s y se enlaza a la direccion
			ServerSocket serverSocket = new ServerSocket(10571, 1000, InetAddress.getByName("127.0.0.1"));
            // Se crea un Scanner para leer la entrada del usuario
			Scanner entrada = new Scanner(System.in);

			System.out.println("Numero de clientes en la simulacion (n) ");
			nclientes = entrada.nextInt();

			System.out.println("Numero de vecinos (Divisor de n) ");
			nvecinos = entrada.nextInt();

			// Bucle para asegurarse de que el numero de vecinos es un divisor del num de clientes
			while (nclientes % nvecinos != 0) {
				System.out.println("Numero de vecinos (Divisor de n) ");
				nvecinos = entrada.nextInt();
			}

			System.out.println("Numero de ciclos ");
			nciclos = entrada.nextInt();
			// Declaramos una matriz de Sockets para manejar las conexiones
			Socket[][] matriz = new Socket[nclientes / nvecinos][nvecinos];

			// Bucle para que los clientes se conecten
			while (clientesActuales < nclientes) {
                System.out.println("Clientes actuales:" + clientesActuales);
				// Aceptamos una conexion del cliente
				Socket clientSocket = serverSocket.accept();
				// Imprime el puerto del cliente conectado
				System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress() + " "
						+ clientSocket.getPort());
				//Creamos un DataOutputStream para enviar datos al cliente
				DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
				// Se almacena el socket del cliente en la matriz
				matriz[filas][columnas] = clientSocket;
				// Actualizamos la cuenta de columnas
				++columnas;

				// Si se alcanza el num de vecinos
				if (columnas == nvecinos) {
					// Se reinicia el contador de columnas
					columnas = 0;
					// Se incrementa la fila para empezar a llenar la siguiente fila
					++filas;
				}
				// Envia datos a los clientes sobre el num actual de clientes, num ciclos y el num vecinos-1
				dataOutputStream.writeUTF("" + clientesActuales + "," + nciclos + "," + (nvecinos - 1) * nciclos);
				clientesActuales++;

			}

			System.out.println("Cargando................");
			// Recurso con el numero actual de clientes
			r = new Recurso(clientesActuales);
			// El hilo se pone en pausa durante 5'
			Thread.sleep(2000);
			for (int i = 0; i < (nclientes / nvecinos); i++) {
				for (int j = 0; j < nvecinos; j++) {
					// Se crea un ClientHandler pasandole un sockete del cliente con todos los datos
					ClientHandler manejador = new ClientHandler(matriz[i][j], nciclos, r, matriz);
                                       
					manejador.start();
					lmanejadores.add(manejador);

				}
			}
			// El bucle se ejecuta mientras haya clientes que estan siendo atendidos
			while (r.GetClientes() > 0) {
				// El hilo se pone a dormir durante 1'
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
                
		} 
		// IOException se lanza por los errores en la entrada/salida de red
		// InterruptedException se lanza si cualquier hilo fue interrumpido mientras estaba dormido
		catch (IOException | InterruptedException e) 
		{
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
	public void run() 
	{
		try
		{
			try 
			{
				// Creamos un flujo de entrada para leer datos del socket del cliente
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
				// Creamos un flujo de salida para enviar datos al socket del clente
				DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
				// Respuesta del cliente
				String respuesta = "";

				// Enviamos un mensaje al cliente para indicar que la comunicacion ha comenzado
				dos.writeUTF("COMIENZO");

				// Mientras que haya clientes activos y el socket del cliente no este cerrado
				while (rcompartido.GetClientes() > 0 && !(clientSocket.isClosed())) 
				{
					// Comprobamos si hay datos disponibles para leer del cliente
					if (dis.available() != 0) {
						// Lee los datos enviados por el cliente
						respuesta = dis.readUTF();
						System.out.println(respuesta);
							// Comprobamos si el mensaje del cliente es "ACABO" y hya aun clientes activos
							if(respuesta.equals("ACABO") && rcompartido.GetClientes()>=0)
								rcompartido.accederVariable(); // Accede a una variable en el recurso compartido para actualizarse
							else
								Reenviar(matr, buscarSocketEnMatriz(clientSocket, matr), clientSocket, respuesta);
					}
							
				}
			} 
			catch (SocketException w)
			{
				rcompartido.accederVariable();
			}      
		} 
		catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}

	}
	// Buscamos un Socket espeficido
	public int buscarSocketEnMatriz(Socket socket, Socket[][] matriz) {
		for (int i = 0; i < matriz.length; i++) {
			for (int j = 0; j < matriz[i].length; j++) {
				Socket s = matriz[i][j];
				// Comprobamos si el socket no esta cerrado y es igual al socket que estamos buscado
				if (s != null && !s.isClosed() && s.equals(socket)) {
					return i;
				}
			}
		}
		return 0;
	}
	// Distribuir mensajes a traves de una red de clientes.
	// Para reenviar un mensaje a todos los sockets de una fila especifica en la matriz, menos el socket que origino el mensaje
	public void Reenviar(Socket[][] matriz, int fila, Socket s, String mensaje) {
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

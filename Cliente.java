package proyectoarc;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class Persona extends Thread {
	protected Socket sk;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	private int id;

	public Persona(int id) {
		this.id = id;
	}

	@Override
	public void run() {
		try {
			int ciclos = 0;
			sk = new Socket("127.0.0.1", 10578);
			dos = new DataOutputStream(sk.getOutputStream());
			dis = new DataInputStream(sk.getInputStream());

			// EnvÃ­a "empieza" al servidor
			dos.writeUTF("empieza");

			// Espera y muestra la respuesta del servidor
			String respuesta = dis.readUTF();
			String[] partes = respuesta.split(",");
			id = Integer.parseInt(partes[0]);
			ciclos = Integer.parseInt(partes[1]);
			int mensajesEsperados = Integer.parseInt(partes[2]);

			while (!respuesta.equals("COMIENZO")) {
				respuesta = dis.readUTF();
			}
                        long tiempoInicio = System.nanoTime();
			while (ciclos > 0 || mensajesEsperados > 0) {
				ciclos--;
				dos.writeUTF(generarCoordenadasAleatorias());

				while (mensajesEsperados > 0) {
					System.out.println("Cliente(" + id + ") tiene que recibir " + mensajesEsperados + " mensajes");
					respuesta = dis.readUTF();

					if (!respuesta.equals("empieza")) {
						mensajesEsperados--;
						System.out.println("Cliente(" + id + ") ha recibido " + respuesta);
					}

				}
				if (ciclos == 0 && mensajesEsperados == 0) {
					dos.writeUTF("ACABO");
                                        long tiempoFin = System.nanoTime();
                                        long tiempo_total = tiempoFin - tiempoInicio;
                                        dos.writeUTF(""+tiempo_total);
					System.out.println("Cliente(" + id + ") acabando ");
				}
			}

			dis.close();
			dos.close();
			sk.close();
		} catch (IOException ex) {
			Logger.getLogger(Persona.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static String generarCoordenadasAleatorias() {
		Random rand = new Random();
		int x = rand.nextInt(101); // NÃºmeros aleatorios entre 0 y 100
		int y = rand.nextInt(101);
		int z = rand.nextInt(101);

		String coordenadas = x + "," + y + "," + z;
		return coordenadas;
	}
}

public class Cliente {
	public static void main(String[] args) {
		// Crear multiples hilos de Persona (clientes)
		int numClientes = 4;
		for (int i = 0; i < numClientes; i++) {
			new Persona(i).start();
		}
	}
}

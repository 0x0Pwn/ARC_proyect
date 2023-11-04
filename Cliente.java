package Redes3;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class Persona extends Thread {
	protected Socket sk;
	protected DataOutputStream dos;
	protected DataInputStream dis;
        Recurso r = new Recurso(200);;
        private int id;
	private int ciclos;
	private int mensajes;

	public Persona() {
		this.id = 0;
		ciclos = 0;
		mensajes = 0;
	}

	@Override
	public void run() {
          try{
            try {
                int mensajesPorCiclo = 0;
                String hostname = "127.0.0.1";
                InetAddress addr = InetAddress.getByName(hostname);
                InetSocketAddress inet = new InetSocketAddress(addr, 10571);
                sk = new Socket();
                sk.connect(inet, 1000);
                sk.setSoTimeout(5000);
                dos = new DataOutputStream(sk.getOutputStream());
                dis = new DataInputStream(sk.getInputStream());
                FasePrimera(dos, dis);
                
                FaseDos(dos,dis);
                
                
                while((ciclos != 0 || mensajes >0) && !sk.isClosed())
                {
                    mensajesPorCiclo = mensajes / ciclos;
                    dos.writeUTF(generarCoordenadasAleatorias());
                    
                    while(mensajesPorCiclo >0)
                    {
                        System.out.println("cliente: "+ id);
                        if(r.GetClientes()>=0)
                        {
                            dis.readUTF();
                            mensajesPorCiclo--;
                            mensajes--;
                           }
                        
                        
                    }
                    System.out.println("Cliente "+id+" a recibir "+mensajes);
                    ciclos--;
                }
                
                
                System.out.println("Cliente "+id+" acabando");
                dos.writeUTF("ACABO");
                while((!dis.readUTF().equals("FIN")) && !sk.isClosed())
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                System.out.println("HOLAAAAAAAA");
                
                dis.close();
                dos.close();
                sk.close();
                
            }catch (SocketTimeoutException e)
            {
                dis.close();
                dos.close();
                sk.close();
            }
            
            } catch (Exception e) {
                e.printStackTrace();
                
            }     
           
	}

	public void FasePrimera(DataOutputStream dos, DataInputStream dis) {
		try {

			dos.writeUTF("saludo");
			String respuesta = dis.readUTF();
			
			String[] partes = respuesta.split(",");
			this.id = Integer.parseInt(partes[0]);
			this.ciclos = Integer.parseInt(partes[1]);
			this.mensajes= Integer.parseInt(partes[2]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void FaseDos(DataOutputStream dos, DataInputStream dis)
	{
            
		try {
			while(!dis.readUTF().equals("COMIENZO"))
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String generarCoordenadasAleatorias() {
		Random rand = new Random();
		int x = rand.nextInt(101); 
		int y = rand.nextInt(101);
		int z = rand.nextInt(101);

		String coordenadas = x + "," + y + "," + z;
		return coordenadas;
	}
}

public class Cliente {
	public static void main(String[] args) {
		// Crear multiples hilos de Persona (clientes)
		int numClientes = 200;
		for (int i = 0; i < numClientes; i++) {
			new Persona().start();
                        
		}
	}
}
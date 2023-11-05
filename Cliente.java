package Redes3;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

class Persona extends Thread {
	// Socket para establecer una conexion red
	protected Socket sk;
	// Flujo para enviar datos
	protected DataOutputStream dos;
	// Para recibir datos a traves del socket
	protected DataInputStream dis;
	Recurso r;
	private int id;
	private int ciclos;
	private int mensajes;

	public Persona(Recurso r) {
            this.id = 0;
            ciclos = 0;
            mensajes = 0;
            this.r = r;
	}

	@Override
	public void run() {
            try
            {
                try 
                {
                    // Inicializa una variable que determina la cantidad de mensajes que se envian por ciclo
                    int mensajesPorCiclo = 0;
                    String hostname = "127.0.0.1";
                    // Obtenemos la direccion de Internet para el nombre de host
                    InetAddress addr = InetAddress.getByName(hostname);
                    // Creamos InetSocketAddress que combina la direccion IP con el puerto 10571
                    InetSocketAddress inet = new InetSocketAddress(addr, 10571);
                    sk = new Socket();
                    // Intentamos conectar el socket a InetSocketAddress
                    sk.connect(inet, 1000);
                    // tiempo maximo de espera para la respuesta del socket de 5'
                    sk.setSoTimeout(5000);
                    // Inicializamos los flujos
                    dos = new DataOutputStream(sk.getOutputStream());
                    dis = new DataInputStream(sk.getInputStream());

                    FasePrimera(dos, dis);
                    FaseDos(dos,dis);

                    long tiempoInicio = System.currentTimeMillis();

                    // Mientras que el Socket no este cerrado
                    while((ciclos != 0 || mensajes >0) && !sk.isClosed())
                    {
                        // Dividimos los mensajes por los ciclos para determinar cuantos msjs procesar por cada ciclo
                        mensajesPorCiclo = mensajes / ciclos;
                        // Envia una cadena de texto con coordenadas aleatorias al servidor a traves del socket
                        dos.writeUTF(generarCoordenadasAleatorias());

                        while(mensajesPorCiclo >0)
                        {
                            // Imprimimos el ID del cliente
                            System.out.println("cliente: "+ id);
                            if(r.GetClientes()>=0)
                            {
                                // lee una cadena UTF del flujo de entrada del socket
                                dis.readUTF();
                                mensajesPorCiclo--;
                                mensajes--;
                            }
                        }
                        // Inidica cuantos mensajes queda por recibir para el cliente con el ID dado
                        System.out.println("Cliente "+id+" a recibir "+mensajes);
                        ciclos--;
                    }
                    // Envia ACABO al servidor para indicar que ha terminado su proceso
                    dos.writeUTF("ACABO");
                    while(r.RetAcabar() == false)
                    {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    // Indica que el cliente con el ID esta terminando su ejecucion
                    System.out.println("Cliente "+id+" acabando");

                    // Cerramos todo
                    r.acabarCliente();
                    dis.close();	// Entrada
                    dos.close();	// Salida
                    sk.close();		// Socket
                    long tiempoFin = System.currentTimeMillis();
                    double segundos = tiempoFin-tiempoInicio / 1000.0;

                    System.out.println(" Ha durado : " + segundos);
                }
                catch (SocketTimeoutException e)
                {
                    dis.close();
                    dos.close();
                    sk.close();
                    long tiempoFin = System.currentTimeMillis();
                }
            
            } 
            catch (Exception e)
            {
                e.printStackTrace();  
            }   
	}
	// 
	public void FasePrimera(DataOutputStream dos, DataInputStream dis) throws ClassNotFoundException {
		try 
		{
			// Envia HOLA al servidor.
			// Se usa para enviar datos a traves de DataOutputStream asociado al socket
			dos.writeUTF("saludo");
			// Espera y lee una respuesta del servidor a traves de DataInputStream.
			String respuesta = dis.readUTF();
			// Divide la respuesta recibida del servidor en partes usando la coma como delimitador
			String[] partes = respuesta.split(",");
			// 
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
                    // Mientras que no reciba COMIENZO
                    // readUTF bloquera hasta que haya datos para leer
                    while(!dis.readUTF().equals("COMIENZO"))
                    {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        // Lanzado cuando otro hilo interrumpe al hilo actual
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
package Redes3;

public class main {

    public static void main(String[] args) throws InterruptedException 
    {
        Recurso r = new Recurso();
        Server s = new Server(r);
        s.start();

        while(r.Retcont() == false)
        {
           Thread.sleep(1000);
        }
        int clientes = r.GetClientes();
        int numClientes = 200;
        for (int i = 0; i < numClientes; i++) {
            new Persona(r).start();
        }

    }
}
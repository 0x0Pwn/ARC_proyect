
package Redes3;

/**
 *
 * @author Usuario
 */
public class main {
    
     public static void main(String[] args) throws InterruptedException {
         Recurso r = new Recurso();
         Servidor s = new Servidor(r);
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


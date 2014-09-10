package server;

import java.rmi.RemoteException;

public class ServerManager {
	
	public ServerManager(String serverName) {
		try {
			Server server = new ConcreteServer(serverName);
		} catch (RemoteException e) {
			System.out.println("Sono occorsi problemi nella creazione del Server. Verificare che il comando rmiregistry sia stato avviato correttamente.");
			e.printStackTrace();
		} catch (AlreadyBoundedException e) {
			System.out.println("Il Server di nome " + serverName + " risulta gia' presente. La pubblicazione di uno stesso\n"
					+ "Server non e' consentita.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 0 || args[0] == "--help") {
			System.out.println("\nIl comando di avvio necessita di un argomento String valido da assegnare come nome del Server.");
			try {
				Thread.sleep(2000);
			} catch(InterruptedException ex) {}
			System.exit(0);				
		}	
		ServerManager manager = new ServerManager(args[0]);
	}

}

package server;

import java.rmi.RemoteException;

public class ServerManager {
	
	public ServerManager(String serverName) {
		try {
			Server server = new ConcreteServer(serverName);
		} catch (RemoteException e) {
			System.out.println("Sono occorsi problemi nella creazione del Server.\n"
					+ "Verificare che il comando rmiregistry sia stato avviato correttamente.");
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch(InterruptedException ex) {}
		} catch (AlreadyBoundedException e) {
			System.out.println("Il Server di nome " + serverName + " risulta gia' presente. La pubblicazione di uno stesso\n"
					+ "Server non e' consentita.");
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch(InterruptedException ex) {}
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 0 || args[0] == "--help") {
			System.out.println("\nIl comando di avvio necessita di un argomento String valido\n"
					+ "da assegnare come nome del Server.");
			System.exit(0);				
		}	
		
		ServerManager manager = new ServerManager(args[0]);
		/*
		//System.out.println("Current working directory: " + System.getProperty("user.dir"));
		Server razor;
		try {
			razor = new ConcreteServer("Razorback");
		} catch(RemoteException e) {
			e.printStackTrace();
			System.out.println("Probabilmente il registro RMI non è stato avviato");
		} catch (AlreadyBoundedException e) {
			e.printStackTrace();
		}
		System.out.println("Finito il main del Server");*/
	}

}

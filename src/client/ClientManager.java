package client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;


public class ClientManager {
	
	public ClientManager(String[] args) throws Exception {
		String clientName = args[0], serverName = args[1];
		int maxDown;
		try {
			maxDown = Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			throw new Exception();
		}
		ArrayList<Resource> resources = new ArrayList<Resource>();
		
		for(int i = 3; i < args.length; i += 2) {
			String name = args[i];
			int parts;
			try {
				parts = Integer.parseInt(args[i + 1]);
			} catch(NumberFormatException e) {
				throw new Exception();
			}
			resources.add(createResource(name, parts));
			
		}
		try {
			Client client = new ConcreteClient(clientName, maxDown, resources, serverName);
		} catch(RemoteException ex) {
			ex.printStackTrace();
			throw new Exception();
		}
	}
	
	private Resource createResource(String name, int parts) {
		ResourceFragment[] frags = new ResourceFragment[parts];
		for(int i = 0; i < parts; i++)
			frags[i] = new ResourceFragment(name, parts, i + 1);
		return new Resource(name, parts, frags);
		
	}

	public static void main(String[] args) {
		if(args.length < 3 || (args.length % 2) == 0 || args[0] == "--help") { //se la lista degli argomenti e' pari, ci sono probabilmente risorse incomplete
			System.out.println("Il comando deve avere come argomenti: nome del client (Stringa) - nome del server (Stringa) - numero di download "
					+ "concorrenti massimi (Intero) - [opzionale] risorse (nome risorsa (Stringa) - parti risorsa (Intero)");
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {}
			System.exit(0);
		}	
		try {
			ClientManager manager = new ClientManager(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("La creazione del Client ha lanciato un'eccezione. Probabilmente le risorse sono in formato sbagliato.");
		}
	}

}

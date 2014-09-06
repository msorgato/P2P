package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Vector;

import client.Client;

public class ConcreteServer extends UnicastRemoteObject implements Server {
	private String name;
	private ArrayList<ClientRegistry> registry = new ArrayList<ClientRegistry>();
	private ArrayList<Server> servers;
	private static final String HOST = "localhost";
	
	private class ClientRegistry {
		private Client client;
		private Vector<String> resources = new Vector<String>(); 
		
		private ClientRegistry(Client c, String res) { 
			client = c;
			String[] resArray = res.split("|");
			for(int i = 0; i < resArray.length; i++) 
				resources.add(resArray[i]);		//DOVREBBE funzionare. testalo.
			//OCCHIO, questo accesso non � sincronizzato
		}	
		
		private boolean addClientResource(String name, int parts) {
			synchronized(resources) {
				if(this.isResHere(name, parts))		//nel caso in cui il client abbia gi� la risorsa
					return false;
				resources.add(name + " " + parts);
			}
			return true;	
		}
		private boolean isResHere(String name, int parts) {
			synchronized(resources) {
				for(int i = 0; i < resources.size(); i++) {
					String[] splitted = resources.get(i).split(" ");
					if(name.equals(splitted[0]) && (parts == Integer.parseInt(splitted[1])))
						return true;
				}
			}
			return false;
		}
	}
	
	protected ConcreteServer(String nm, ArrayList<Server> sr) throws RemoteException {
		name = nm;
		servers = sr;
		String rmiPublish = "rmi://" + HOST + "/" + name;
		try {
			Naming.rebind(rmiPublish, this);		
		} catch(RemoteException e) {
			System.out.println("Lancia una RemoteException");
			e.printStackTrace();
		}
		catch(MalformedURLException exc) {
			exc.printStackTrace();
		}
		System.out.println("Server " + name + " pubblicizzato");
	}
	
	@Override
	public String getName() throws RemoteException {
		return name;
	}
	
	@Override
	public ArrayList<Client> searchResource(String name, int parts) throws RemoteException {
		ArrayList<Client> cli = new ArrayList<Client>();
		synchronized(registry) {
			for(int i = 0; i < registry.size(); i++) {
				if(registry.get(i).isResHere(name, parts))
					cli.add(registry.get(i).client);
			}
		}
		//qui gestisci la richiesta ad altri server per altri client
		return cli;
	}

	@Override
	public void addResource(Client c, String name, int parts) throws RemoteException {
		synchronized(registry) {
			for(int i = 0; i < registry.size(); i++) {
				if(c == registry.get(i).client) {		//se vuoi, raffina i controlli per vedere se quella risorsa non c'era gi�
					boolean ok = registry.get(i).addClientResource(name, parts);
					if(!ok)
						System.out.println("Il client " + c.getName() + " possedeva gi� la risorsa " + name + " " + parts);		//mah, da guardare.
					return;
				}
			}
		}
	}

	@Override
	public boolean connect(Client c) throws RemoteException {
		/*	Dubbio: metto un controllo per vedere se due clients con lo 
		*	stesso nome non possano connettersi a questo o ad altri
		*	server?
		*/
		String res;
		try {
			res = c.getResources();
		} catch(RemoteException e) {
			//Il Client non � raggiungibile
			return false;
		}
		synchronized(registry) {
			registry.add(new ClientRegistry(c, res));	//se arriva ad eseguire questa istruzione, res � stato ottenuto correttamente
			try {
				System.out.println("Client " + c.getName() + " connesso.");
			} catch(RemoteException ex) {	//Problemi di connessione remota
				registry.remove(registry.size());
				return false;
			}
		}
		System.out.println(res);
		return true;	//ha inserito correttamente il registro del Client e non sono apparsi errori di connessione
	}

	@Override
	public boolean disconnect(Client c) {
		synchronized(registry) {
			for(int i = 0; i < registry.size(); i++) {
				if(registry.get(i).client == c) {
					registry.remove(i);
					return true;
				}
			}
			return false;
		}
	}

}

/*
*	Il metodo searchResource(blabla) dovr� ritornare al Client l'ArrayList (o un array normale) contenente
*	i Client che possiedono la risorsa cercata dal chiamante.
*
*	NOTA BENE: Se il Client e il Server di un'applicazione distrbuita risiedono sulla stessa JVM, quando il Client
*	invoca un metodo che dovrebbe essere remoto, sul Server non viene aperto automaticamente un nuovo Thread. OCCHIO.
*/
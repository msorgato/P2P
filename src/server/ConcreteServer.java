package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import client.Client;
import client.ClientGUI;

public class ConcreteServer extends UnicastRemoteObject implements Server {
	private String name;
	private ArrayList<ClientRegistry> registry = new ArrayList<ClientRegistry>();
	private ArrayList<Server> servers = new ArrayList<Server>();
	private static final String HOST = "localhost";
	
	private ServerGUI gui;
	
	private class ClientRegistry {
		private Client client;
		private ArrayList<String> resources = new ArrayList<String>(); 
		
		private ClientRegistry(Client c, String res) { 
			client = c;
			String[] resArray = res.split(":");
			synchronized(resources) {
				for(int i = 0; i < resArray.length; i++) 
					resources.add(resArray[i]);		//DOVREBBE funzionare. testalo.
			}
		}	
		
		private boolean addClientResource(String name, int parts) {
			synchronized(resources) {
				if(this.isResHere(name, parts))		//nel caso in cui il client abbia gia' la risorsa
					return false;
				resources.add(name + " " + parts);
			}
			try {
				gui.addLog("Il Client " + client.getName() + " ha ottenuto la risorsa "+ name + " " + parts); //----------------------------
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return true;	
		}
		//funzione utilizzata dal Server per aggiungere una risorsa in caso di scaricamento
		
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
		//funzione utilizzata per stablire se un Client sia in possesso di una determinata risorsa
	}
	
	protected ConcreteServer(String nm) throws RemoteException, AlreadyBoundedException {
		name = nm;
		String[] serverNames = null;
		try {
			serverNames = Naming.list("rmi://" + HOST + "/");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.out.println("Eccezione lanciata dal metodo list di Naming");
		}
		if(serverNames != null) {
			for(int i = 0; i < serverNames.length; i++)
				if(serverNames[i].contains(nm))
					throw new AlreadyBoundedException();
		}
		String rmiPublish = "rmi://" + HOST + "/" + name;
		try {
			Naming.rebind(rmiPublish, this);		
		} catch(RemoteException e) {
			System.out.println("L'invocazione del metodo rebind del Server " + name + " lancia una RemoteException");
			e.printStackTrace();
		}
		catch(MalformedURLException exc) {
			System.out.println("L'invocazione del metodo rebind lancia una MalformedURLException");
			exc.printStackTrace();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Server " + name + " pubblicizzato");
		if(!(serverNames == null)) {
			for(int i = 0; i < serverNames.length; i++) {
				Server srvr = null;
				try {
					srvr = (Server) Naming.lookup(serverNames[i]);
				} catch(RemoteException ex) {
					ex.printStackTrace();
					//GESTISCI
				} catch(MalformedURLException ecc) {
					ecc.printStackTrace();
					//GESTISCI
				} catch (NotBoundException e) {
					e.printStackTrace();
					//GESTISCI
				}
				if(srvr != null) {
					synchronized(servers) {
						try {
							if(srvr.connectServer(this))
								servers.add(srvr);
						} catch(RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	@Override
	public String getName() throws RemoteException {
		return name;
	}
	
	@Override
	public ArrayList<Client> searchResource(String name, int parts) throws RemoteException {
		ArrayList<Client> cli = this.searchClient(name, parts);
		synchronized(servers) {
			for(int i = 0; i < servers.size(); i++) {
				ArrayList<Client> otherCli = new ArrayList<Client>();
				try {
					otherCli.addAll(servers.get(i).searchClient(name, parts));
				} catch(RemoteException e) {	//Un server nella lista si è disconnesso o ha avuto problemi
					servers.remove(i);
					continue;	//salta la concatenazione dei client provenienti dall'ultimo Server ed esegue l'iterata successiva
				}
				cli.addAll(otherCli);
			}
		}
		return cli;
	}
	
	@Override
	public ArrayList<Client> searchClient(String name, int parts) throws RemoteException {
		ArrayList<Client> cli = new ArrayList<Client>();
		synchronized(registry) {
			for(int i = 0; i < registry.size(); i++) {
				if(registry.get(i).isResHere(name, parts))
					cli.add(registry.get(i).client);
			}
		}
		return cli;
	}

	@Override
	public void addResource(Client c, String name, int parts) throws RemoteException {
		System.out.println("Metodo addResource invocato correttamente");
		synchronized(registry) {
			for(int i = 0; i < registry.size(); i++) {
				if(c.equals(registry.get(i).client)) {
					boolean ok = registry.get(i).addClientResource(name, parts);
					if(!ok)
						gui.addLog("Il client " + c.getName() + " possedeva già la risorsa " + name + " " + parts);		//mah, da guardare.
					return;
				}
			}
		}
	}

	@Override
	public boolean connect(Client c) throws RemoteException {
		String res, clientName = "sconosciuto";
		try {
			res = c.getResources();
			clientName = c.getName();
		} catch(RemoteException e) {
			return false;
		}
		synchronized(registry) {
			registry.add(new ClientRegistry(c, res));	//se arriva ad eseguire questa istruzione, res è stato ottenuto correttamente
			try {
				gui.addClient(c.getName());
			} catch(RemoteException ex) {	//Problemi di connessione remota
				registry.remove(registry.size());
				gui.removeClient(clientName);
				return false;
			}
		}
		return true;	//ha inserito correttamente il registro del Client e non sono apparsi errori di connessione
	}
	
	@Override
	public boolean connectServer(Server s) throws RemoteException {
		String sName;
		try {
			sName = s.getName();
		} catch(RemoteException e ) {
			return false;
		}
		synchronized(servers) {
			if(servers.contains(s))
				return false;
			servers.add(s);
			try {
				gui.addServer(s.getName());
			} catch(RemoteException e) {
				e.printStackTrace();
				gui.removeServer(sName);
				servers.remove(servers.size());
				return false;
			}
		}
		return true;
	}

	//funzione chiamata da un client connesso al server per richiedere la disconnessione e la seguente cancellazione 
	//del registro a lui legato
	@Override
	public boolean disconnect(Client c) {
		String cName = "sconosciuto";
		try {
			cName = c.getName();
		} catch(RemoteException e) {}
		synchronized(registry) {
			for(int i = 0; i < registry.size(); i++) {
				if(registry.get(i).client == c) {
					registry.remove(i);
					gui.removeClient(cName);
					return true;
				}
			}
			return false;
		}
	}
	
	@Override
	public boolean disconnectServer(Server s) throws RemoteException {
		String sName = "sconosciuto";
		try {
			sName = s.getName();
		} catch(RemoteException e) {}
		synchronized(servers) {
			if(servers.contains(s)) {
				servers.remove(s);
				gui.removeServer(sName);
				return true;
			}
			return false;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		for(int i = 0; i < servers.size(); i++) {
			try {
				servers.get(i).disconnectServer(this);
			} catch(RemoteException e) {
				continue;
			}
		}
	}
}

/*
*	Il metodo searchResource(blabla) dovrà ritornare al Client l'ArrayList (o un array normale) contenente
*	i Client che possiedono la risorsa cercata dal chiamante. Ovviamente saranno tutti array di riferimenti Remote.
*
*	NOTA BENE: Se il Client e il Server di un'applicazione distrbuita risiedono sulla stessa JVM, quando il Client
*	invoca un metodo che dovrebbe essere remoto, sul Server non viene aperto automaticamente un nuovo Thread. OCCHIO.
*
*	gestire la connessione di un nuovo server, deve notificare agli altri server online di aggiornare la loro lista dei server attivi
*/
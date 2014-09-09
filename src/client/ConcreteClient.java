package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import server.Server;

public class ConcreteClient extends UnicastRemoteObject implements Client {

	private String name;
	private int maxDownloads;
	private ArrayList<Resource> resources;		
	private Report report = new Report();
	private Server server = null;
	private boolean connected = false;
	private static final String HOST = "localhost";
	private static final int SLEEPTIME = 2000;	//tempo di attesa nell'invio di un frammento di risorsa
		
	private class Report {
		ArrayList<String> report = new ArrayList<String>();
		private synchronized void addReport(String Client, String resName, String resParts, String resPart) {
			report.add(Client + " " + resName + " " + resParts + " " + resPart);
		}
		private synchronized ArrayList<String> getReport() {
			return report;
		}
	}
	
	private class Uploader extends Thread {
		private String resName;
		private int resParts;
		private int fragment;
		private Downloader downloader = null;
		private Client client = null;
		
		private Uploader(String resName, int resParts, int fragment, Downloader downloader, Client client) {
			this.resName = resName;
			this.resParts = resParts;
			this.fragment = fragment;
			this.downloader = downloader;
			this.client = client;
			start();
		}
		
		public void run() {
			try {
				Thread.sleep(SLEEPTIME);
			} catch(InterruptedException e) {
				//lo sleep si è interrotto: che faccio, proseguo e ciccia?
			}
			int resourceIndex = -1;		
			synchronized(resources) {	
				for(int i = 0; i < resources.size() && resourceIndex == -1; i++) {
					if(resources.get(i).equalsResource(resName, resParts))	
						resourceIndex = i;
				}
				if(resourceIndex == -1)
					//chiama il metodo di downloader con fragToSend nullo
				try {
					report.addReport(client.getName(), resName, Integer.toString(resParts), Integer.toString(fragment));
				} catch(RemoteException e) {	//il client target è morto mentre aspettava il frammento
					return;
				}
				// chiama il metodo di downloader con resources.get(resourceIndex).getFragment(fragment);	
			}	
		}
	}
	
	protected ConcreteClient(String cName, int maxD, ArrayList<Resource> res, String sName) throws RemoteException {	
		name = cName;
		maxDownloads = maxD;
		resources = res;
		try {
			server = (Server) Naming.lookup("rmi://" + HOST + "/" + sName);
		} catch(RemoteException ex) {
			ex.printStackTrace();
			System.out.println("Sono stati riscontrati dei problemi nel raggiungimento del server " + sName);
			//probabilmente il server non è stato pubblicato
			//oppure RMI non è stato lanciato
		} 
		catch(NotBoundException e) {
			e.printStackTrace();
			System.out.println("Il client " + name + " cerca di connettersi ad un server che non ha tornato nessun riferimento dal lookup");
			//devo aver pubblicizzato il riferimento sbagliato
		}
		catch(MalformedURLException exc) {
			exc.printStackTrace();
			System.out.println("Client " + name + " probabilmente non trova il server " + sName + " perche' l'indirizzo e' sbagliato");
		}
		catch(Exception exc) {
			System.out.println("Connessione non avvenuta. Uscita dal programma.");
			return;	//questo return e' sbagliato
		}
		try {
			connected = server.connect(this);
			System.out.println("Connesso al server " + server.getName());
		} catch(RemoteException ecc) {
			System.out.println("La connessione al server " + sName + " è caduta");
		}
		catch(Exception exc) {
			connected = false;
			return;
		}
	}
	
	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public String getResources() throws RemoteException {		
		synchronized(resources) {		
			String res = resources.get(0).getName() + " " + resources.get(0).getParts(); 
			for(int i = 1; i < resources.size(); i++) 
				res = res + ":" + resources.get(i).getName() + " " + resources.get(i).getParts(); //splittare la stringa usando il carattere ":" come guardia
			return res;
		}
	}

	@Override
	public void requestFragment(String nm, int prts, int frgm, Downloader d, Client c) throws RemoteException {
		new Uploader(nm, prts, frgm, d, c);			
	}	

	@Override
	public boolean download(String nm, int prts) throws RemoteException{
		if(connected) {
			ArrayList<Client> clients;
			try {
				clients = server.searchResource(nm, prts);
			} catch(RemoteException e) {
				connected = false;
				return false;
			}
			Downloader downloader = new Downloader(nm, prts, clients, this, maxDownloads);
			Resource resToAdd = downloader.process();
			if(resToAdd == null) {
				//QUI abbiamo un bel problema: non siamo riusciti a scaricare la risorsa che ci serviva
				return false;
			}
			synchronized(resources) {
				resources.add(resToAdd);
			}
			try {
				server.addResource(this, nm, prts);
			} catch(RemoteException e) {
				connected = false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean connectToServer() throws RemoteException {
		if(!connected) {
			try {
				server.connect(this);
			} catch(RemoteException e) {
				//il server è down
				return false;
			}
			connected = true;
		}
		return connected;
	}
	
	@Override
	public void disconnectFromServer() throws RemoteException {
		if(connected) {
			try {
				server.disconnect(this);
			} catch(RemoteException e) {
				//Server is already down
			}
			connected = false;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			if(connected)
				server.disconnect(this);
		} catch(RemoteException e) {
			System.out.println("Alla disconnessione del Client " + name + ", anche il Server ha presentato problemi");
		}
	}
	/*
	 * Semplice funzione che invoca, quando l'oggetto viene deallocato, un'ulteriore chiusura delle connessioni "soft".
	 */

}

/*
 * Appunti sulla classe: quando viene chiamato il metodo "connect(Client)", bisogna chiamare
 * anche un metodo del Client per mandare la lista delle risorse presenti nel Client al Server
 * a cui ci si è appena connessi.
 */

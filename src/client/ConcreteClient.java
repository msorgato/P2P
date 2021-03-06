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
	
	private ClientGUI gui;
		
	private class Report {
		ArrayList<String> report = new ArrayList<String>();
		private synchronized void addReport(String Client, String resName, String resParts, String resPart) {
			report.add(Client + " " + resName + " " + resParts + " " + resPart);
		}
		private synchronized ArrayList<String> getReport() {
			return report;
		}
	}
	
	protected ConcreteClient(String cName, int maxD, ArrayList<Resource> res, String sName) throws RemoteException {	
		name = cName;
		maxDownloads = maxD;
		resources = res;
		gui = new ClientGUI(this);
		for(int i = 0; i < resources.size(); i++)
			gui.addResource(resources.get(i).getName() + " " + resources.get(i).getParts());
		try {
			server = (Server) Naming.lookup("rmi://" + HOST + "/" + sName);
		} catch(RemoteException ex) {
			ex.printStackTrace();
			System.out.println("Sono stati riscontrati dei problemi nel raggiungimento del server " + sName);
			//probabilmente il server non e' stato pubblicato
			//oppure RMI non e' stato lanciato
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
			gui.addLog("Connesso al server " + server.getName());
		} catch(RemoteException ecc) {
			gui.addLog("La connessione al server " + sName + " e' caduta");
		}
		catch(Exception exc) {
			connected = false;
			return;
		}
		gui.enableLogout();
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
	public ResourceFragment sendResourceFragment(String nm, int prts, int frgm, Client c) throws RemoteException {
		try {
			Thread.sleep(SLEEPTIME);
		} catch(InterruptedException e) {
			return null;
		}
		int resourceIndex = -1;		
		synchronized(resources) {	
			for(int i = 0; i < resources.size() && resourceIndex == -1; i++) {
				if(resources.get(i).equalsResource(nm, prts))	
					resourceIndex = i;
			}
			if(resourceIndex == -1)
				return null;
			try {
				report.addReport(c.getName(), nm, Integer.toString(prts), Integer.toString(frgm));
			} catch(RemoteException e) {	//il client target e' morto mentre aspettava il frammento
				return null;
			}
			return resources.get(resourceIndex).getFragment(frgm);	
		}	
	}	

	@Override
	public boolean download(String nm, int prts) throws RemoteException{
		if(connected) {
			ArrayList<Client> clients = new ArrayList<Client>();
			try {
				clients = server.searchResource(nm, prts);
			} catch(RemoteException e) {
				connected = false;
				return false;
			}
			if(clients.isEmpty()) {
				gui.addLog("Risorsa " + nm + " " + prts + " non trovata");
				return false;
			}
			Downloader downloader = new Downloader(nm, prts, clients, this, maxDownloads, gui);
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
			gui.addResource(resToAdd.getName() + " " + resToAdd.getParts());
			gui.addLog("Scaricata la risorsa " + resToAdd.getName() + " " + resToAdd.getParts());
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
				//il server e' offline
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
				gui.addLog("Alla disconnessione del Client " + name + ", anche il Server ha presentato problemi");
			}
			connected = false;
			gui.disableLogout();
			gui.addLog("Disconnesso dal Server");
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		disconnectFromServer();
	}
	/*
	 * Semplice funzione che invoca, quando l'oggetto viene deallocato, un'ulteriore chiusura delle connessioni "soft".
	 */

}

/*
 * Appunti sulla classe: quando viene chiamato il metodo "connect(Client)", bisogna chiamare
 * anche un metodo del Client per mandare la lista delle risorse presenti nel Client al Server
 * a cui ci si � appena connessi.
 */

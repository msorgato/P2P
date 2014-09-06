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
	private ArrayList<Resource> resources;		//<----CAMBIA
	private Report report = new Report();
	private Server server;
	private boolean connected = false;
	private static final String HOST = "localhost";
	private static final int SLEEPTIME = 2000;	//tempo di attesa nell'invio di un frammento di risorsa
		
	private class Report {
		/*	
		*	QUI ci va la classe che tiene conto di tutte le risorse scaricate da altri client.
		*	Per ogni parte di risorsa, quanti e quali client hanno scaricato quella	
		*	risorsa.
		*/
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
			System.out.println("Client " + name + " pribabilmente non trova il server " + sName + " perché l'indirizzo è sbagliato");
		}
		catch(Exception exc) {
			System.out.println("Connessione non avvenuta. Uscita dal programma.");
			return;
		}
		try {
			server.connect(this);
			System.out.println("Connesso al server " + server.getName());
		} catch(RemoteException ecc) {
			System.out.println("La connessione al server " + sName + " è caduta");
		}
		catch(Exception exc) {
			return;
		}
		connected = true;
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
				res = res + "|" + resources.get(i).getName() + " " + resources.get(i).getParts(); //splittare la stringa usando il carattere "|" come guardia
			return res;
		}
	}

	@Override
	public ResourceFragment sendResourceFragment(String nm, int prts, int frgm, Client c) throws RemoteException {
		int resourceIndex = -1;		
		synchronized(c) {	//questo mi da un ulteriore controllo lato client di non inviare piu' risorse allo stesso client
			synchronized(resources) {	
				for(int i = 0; i < resources.size() && resourceIndex == -1; i++) {
					if(resources.get(i).equalsResource(nm, prts))	
						resourceIndex = i;
				}
				if(resourceIndex == -1)
					return null;
				return resources.get(resourceIndex).getFragment(frgm);	
			}	
		}
	}	//in questo metodo ci va anche l'update del Report alla fine dell'invio della parte della Risorsa.

	@Override
	public boolean download(String nm, int prts) throws RemoteException{
		try {
			ArrayList<Client> clients = (ArrayList<Client>)server.searchResource(nm, prts);
			//DOMANDA DA UN MILIONE DI DOLLARI: IL CAST SERVE SUL SERIO O E' UNA MIA FISIMA???
		} catch(RemoteException e) {
			//il server e' andato. come si fa?
		}
		//QUI si richiama il metodo di Downloader che ritorna la risorsa.
		//il notify() alla fine del download è la chiave.
		//in piu', io ci metterei anche un bel Downloader.downloading = 0; alla fine.
		return false;
		//ci manca anche il metodo di update del registro del server con la lista delle risorse di ogni client,
		//visto che il registro che ha il server è una copia serializzata della lista locale
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

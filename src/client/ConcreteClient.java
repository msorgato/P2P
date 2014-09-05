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
	private static final String HOST = "localhost";
	private static final int SLEEPTIME = 2000;	//tempo di attesa nell'invio di un frammento di risorsa
		
	private class Report {
		/*	QUI ci va la classe che tiene conto di tutte le risorse scaricate da altri client.
		*	Per ogni parte di risorsa, quanti e quali client hanno scaricato quella	
		*	risorsa.
		*/
	}
	
	protected ConcreteClient(String cName, int maxD, ArrayList<Resource> res, String sName) throws RemoteException {	
		name = cName;
		maxDownloads = maxD;
		resources = res;		//<-----Vector da cambiare
		try {
			server = (Server) Naming.lookup("rmi://" + HOST + "/" + sName);
		} catch(RemoteException ex) {
			ex.printStackTrace();
			System.out.println("Probabilmente non è stato pubblicizzato un Server con nome " + sName);
			//probabilmente il server non e' stato pubblicato
		} 
		catch(NotBoundException e) {
			e.printStackTrace();
			System.out.println("Il client " + name + " cerca di connettersi ad un server che non ha tornato nessun riferimento dal lookup");
			//devo aver pubblicizzato il riferimento sbagliato
		}
		catch(MalformedURLException exc) {
			exc.printStackTrace();
			System.out.println("Client " + name + " pribabilmente non trova il server perché l'indirizzo è sbagliato");
		}
		try {
			server.connect(this); 		//ciccia, ce se prova. se ce la fa, oro.
		} catch(RemoteException ecc) {
			System.out.println("I casi sono due, penso: o il metodo e' sbagliato, o ha preso il riferimento e questo non funzia piu.");
		}
		System.out.println("Connesso al server " + server.getName());
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
	/*
	* In questo metodo ci sono un casino di riferimenti al Vector da cambiare
	*/

	@Override
	public ResourceFragment sendResourceFragment(String nm, int prts, int frgm, Client c) throws RemoteException {
		int resourceIndex = -1;		
		synchronized(c) {	//questo mi da un altro controllo di non inviare piu' risorse allo stesso client
			synchronized(resources) {	//occhio al deadlock ed a cambiare il Vector
				for(int i = 0; i < resources.size() && resourceIndex == -1; i++) {
					if(resources.get(i).equalsResource(nm, prts))	//idem di sopra
						resourceIndex = i;
				}
				if(resourceIndex == -1)
					return null;
				return resources.get(resourceIndex).getFragment(frgm);	//idem qui
			}	
		}
	}	//in questo metodo ci va anche l'update del Report alla fine dell'invio della parte della Risorsa.

	@Override
	public boolean download(String nm, int prts) {
		try {
			Client[] clients = server.searchResource(nm, prts);	//altra merda di Vector
		} catch(RemoteException e) {
			//il server e' andato. come si fa?
		}
		//QUI si richiama il metodo di Downloader che ritorna la risorsa.
		//il notify() alla fine del download è la chiave.
		//in piu', io ci metterei anche un bel Downloader.downloading = 0; alla fine.
		return false;
		//ci manca anche il metodo di update del registro del server con la lista delle risorse di ogni client
	}

}

/*
 * Appunti sulla classe: quando viene chiamato il metodo "connect(Client)", bisogna chiamare
 * anche un metodo del Client per mandare la lista delle risorse presenti nel Client al Server
 * a cui ci si è appena connessi.
 */

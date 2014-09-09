package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
	public String getName() throws RemoteException;
	
	public String getResources() throws RemoteException;  	
	//metodo che crea una stringa con una formattazione precisa contenente le risorse in proprio possesso
	
	public ResourceFragment sendResourceFragment(String nm, int prts, int frgm, Client c) throws RemoteException;	
	//questo metodo manda una parte di risorsa, non una risorsa intera!
	
	public boolean download(String nm, int prts) throws RemoteException;
	
	public boolean connect();
	
	public void disconnect();
}

package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
	public String getName() throws RemoteException;
	
	public String getResources() throws RemoteException;  	
	//metodo che crea una stringa con una formattazione precisa contenente le risorse in proprio possesso
	
	public void requestFragment(String nm, int prts, int frgm, Downloader d, Client c) throws RemoteException;	
	
	public boolean download(String nm, int prts) throws RemoteException;
	
	public boolean connectToServer() throws RemoteException;
	
	public void disconnectFromServer() throws RemoteException;
}

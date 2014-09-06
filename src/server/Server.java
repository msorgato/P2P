package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import client.Client;

public interface Server extends Remote {
	public String getName() throws RemoteException;
	public Client[] searchResource(String name, int parts) throws RemoteException;
	public void addResource(Client c, String name, int parts) throws RemoteException;	
	public boolean connect(Client c) throws RemoteException;
	public boolean disconnect(Client c) throws RemoteException;
}

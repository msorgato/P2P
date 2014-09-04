package server;

import java.rmi.RemoteException;
import java.util.Vector;

public class ServerManager {

private static Vector<Server> servers = new Vector<Server>();		//Questo serve al launcher per tener conto di quanti server sono gia' up.
	
	public static void main(String[] args) {
		try {
			Server razor = new ConcreteServer("Razorback", servers);
		} catch(RemoteException e) {
			e.printStackTrace();
		}

	}

}

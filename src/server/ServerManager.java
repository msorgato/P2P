package server;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class ServerManager {

private static ArrayList<Server> servers = new ArrayList<Server>();		//Questo serve al launcher per tener conto di quanti server sono gia' up.
	
	public static void main(String[] args) {
		//System.out.println("Current working directory: " + System.getProperty("user.dir"));
		try {
			ConcreteServer razor = new ConcreteServer("Razorback", servers);
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}

}

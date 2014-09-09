package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ServerManager {
	
	public static void main(String[] args) {
		//System.out.println("Current working directory: " + System.getProperty("user.dir"));
		Server razor, razor2;
		try {
			razor = new ConcreteServer("Razorback");
		} catch(RemoteException e) {
			e.printStackTrace();
			System.out.println("Probabilmente il registro RMI non è stato avviato");
		} catch (AlreadyBoundedException e) {
			e.printStackTrace();
		}
		System.out.println("Finito il main del Server");
	}

}

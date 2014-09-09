package server;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class ServerManager {
	
	public static void main(String[] args) {
		//System.out.println("Current working directory: " + System.getProperty("user.dir"));
		Server razor, razor2, razor3;
		try {
			razor = new ConcreteServer("Razorback");
		} catch(RemoteException e) {
			e.printStackTrace();
			System.out.println("Probabilmente il registro RMI non è stato avviato");
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			razor2 = new ConcreteServer("Razorback2");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}

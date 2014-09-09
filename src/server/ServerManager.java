package server;

import java.rmi.Naming;
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
		} catch (AlreadyBoundedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			razor2 = new ConcreteServer("Razorback2");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			razor3 = new ConcreteServer("Razorback2");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundedException e) {
			System.out.println("Lancia una bellissima AlreadyBoundedException, fuck yea");
			e.printStackTrace();
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Server rzr = null;
		try {
			rzr = (Server) Naming.lookup("rmi://localhost/Razorback2");
			System.out.println("Lookup");
		} catch(Exception e) {
			System.out.println("Questo metodo fa cose brutte.");
		}
		try {
			System.out.println(rzr.getName());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

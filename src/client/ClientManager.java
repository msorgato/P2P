package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import server.Server;	//ricordati di levarlo

public class ClientManager {

	public static void main(String[] args) {
		ResourceFragment[] frags = new ResourceFragment[5], frags2 = new ResourceFragment[7];
		for(int i = 0; i < 5; i++) {
			frags[i] = new ResourceFragment("uno", 5, i + 1);
		}
		for(int i = 0; i < 7; i++) {
			frags2[i] = new ResourceFragment("due", 7, i + 1);
		}
		Resource resource = new Resource("uno", 5, frags), resource2 = new Resource("due", 7, frags2);	
		ArrayList<Resource> resources = new ArrayList<Resource>();
		resources.add(resource);
		resources.add(resource2);
		try {
			Client client = new ConcreteClient("FooClient", 2, resources, "Razorback");
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}

}

package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import server.Server;	//ricordati di levarlo

public class ClientManager {

	public static void main(String[] args) {
		ResourceFragment[] frags = new ResourceFragment[5], frags2 = new ResourceFragment[7], frags5 = new ResourceFragment[5];
		for(int i = 0; i < 5; i++) {
			frags[i] = new ResourceFragment("uno", 5, i + 1);
			frags5[i] = new ResourceFragment("tre", 5, i + 1);
		}
		for(int i = 0; i < 7; i++) {
			frags2[i] = new ResourceFragment("due", 7, i + 1);
		}
		ResourceFragment[] frags3 = new ResourceFragment[5], frags4 = new ResourceFragment[7];
		for(int i = 0; i < 5; i++) {
			frags3[i] = new ResourceFragment("tre", 5, i + 1);
		}
		for(int i = 0; i < 7; i++) {
			frags4[i] = new ResourceFragment("quattro", 7, i + 1);
		}
		Resource resource = new Resource("uno", 5, frags), resource2 = new Resource("due", 7, frags2), resource3 = new Resource("tre", 5, frags3),
				resource4 = new Resource("quattro", 7, frags4), resource5 = new Resource("tre", 5, frags5);	
		ArrayList<Resource> resources = new ArrayList<Resource>(), resources2 = new ArrayList<Resource>(), resources3 = new ArrayList<Resource>();
		resources.add(resource);
		resources.add(resource2);
		resources2.add(resource3);
		resources2.add(resource4);
		resources3.add(resource5);
		Client client1 = null, client2 = null, client3 = null;
		try {
			client1 = new ConcreteClient("FooClient1", 2, resources, "Razorback");
			client2 = new ConcreteClient("FooClient2", 2, resources2, "Razorback");
			client3 = new ConcreteClient("FooClient3", 2, resources3, "Razorback");
		} catch(RemoteException e) {
			e.printStackTrace();
		}
		try {
			client1.download("tre", 5);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package client;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class ClientManager {

	public static void main(String[] args) {
		ResourceFragment[] frags = { new ResourceFragment("uno", 3, 1), new ResourceFragment("uno", 3, 2), new ResourceFragment("uno", 3, 3) };
		Resource resource = new Resource("uno", 3, frags);		
		ArrayList<Resource> resources = new ArrayList<Resource>();
		resources.add(resource);
		try {
			ConcreteClient client = new ConcreteClient("FooClient", 2, resources, "Razorback");
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}

}

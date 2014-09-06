package client;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class ClientManager {

	public static void main(String[] args) {
		ResourceFragment[] frags = { new ResourceFragment("uno", 3, 1), new ResourceFragment("uno", 3, 2), new ResourceFragment("uno", 3, 3) }, 
				frags2 = { new ResourceFragment("due", 5, 1), new ResourceFragment("due", 5, 2), new ResourceFragment("due", 5, 3), 
				new ResourceFragment("due", 5, 4), new ResourceFragment("due", 5, 5)};
		Resource resource = new Resource("uno", 3, frags), resource2 = new Resource("due", 5, frags2);		
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

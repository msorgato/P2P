package client;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Downloader {
	private	String resourceName;
	private int resourceParts;
	private int maxDownloads;
	private ArrayList<Client> clients;		
	private ArrayList<ResourceFragment> fragments = new ArrayList<ResourceFragment>();
	
	private class DownloadFragment extends Thread {
		private Client client;
		private int fragment;
		private ResourceFragment fragToDownload = null;

		private DownloadFragment(int f) { 
			synchronized(clients) {
				client = clients.remove(0);		
			} 
			fragment = f;
			start();
		}
		
		public void run() {
			downloading.add();
			while(fragToDownload == null) {
				try {
					fragToDownload = client.sendResourceFragment(resourceName, resourceParts, fragment, clientDownloading);
				} catch(RemoteException e) {
					synchronized(clients) {
						if(!clients.isEmpty())	
							client = clients.remove(0);
						else	//ho finito i client da cui ricevere la risorsa
							return; 
					}
				}
			}
			synchronized(fragments) {
				fragments.add(fragment, fragToDownload);
			}		
			synchronized(clients) {
				clients.add(client);
			}
			synchronized(downloading) {
				downloading.remove();
				notify();
			}
		} 
	}
	
	public Downloader(String nm, int prts, ArrayList<Client> cls, int maxDown) { 
		resourceName = nm; 
		resourceParts = prts; 
		maxDownloads = maxDown;
		clients = cls; 
	} 
	public Resource process() {
		for(int i = 1; i < resourceParts && !clients.isEmpty(); i++) {
			while(downloading.getCurrent() <= maxDownloads) {
				synchronized(downloading) {
					try {
						wait();
					} catch(InterruptedException e) {
						//il downloader e' stato interrotto mentre aspettava di creare altri thread
					}
				}
			}
			new DownloadFragment(i); 
		}
		synchronized(fragments) {
			while(fragments.size() < resourceParts && !clients.isEmpty())
				try {
					wait();
				} catch(InterruptedException e) {
					//il downloader e' stato interrotto mentre aspettava la fine dei download
				}
		}
		if(fragments.size() < resourceParts)
			return null;
		return new Resource(resourceName, resourceParts, (ResourceFragment[])fragments.toArray());
	}
	//alla fine dello scaricamento dovrò richiamare il metodo statico di Resource per vedere se
	//tutti i frammenti che mi sono stati inviati sono ok.
}

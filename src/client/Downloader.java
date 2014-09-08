package client;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Downloader {
	private	String resourceName;
	private int resourceParts;
	private int maxDownloads;
	private int downloading = 0;
	private Integer processed = new Integer(0);
	private Client clientDownloading;
	private ArrayList<Client> clients;		
	private ArrayList<ResourceFragment> fragments;
	
	private class DownloadFragment extends Thread {
		private Client target;
		private Client calling;
		private int fragment;
		private ResourceFragment fragToDownload = null;

		private DownloadFragment(int fragment, Client target, Client calling) { 
			this.target = target;
			this.calling = calling;
			this.fragment = fragment;
			start();
		}
		
		public void run() {
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
			synchronized(downloading) {	//qui ci andrebbe il notify su Downloader.this
				downloading.remove();
				notify();
			}
		} 
	}
	
	public Downloader(String nm, int prts, ArrayList<Client> cls, Client clientDownloading, int maxDown) { 
		resourceName = nm; 
		resourceParts = prts; 
		maxDownloads = maxDown;
		clients = cls; 
		this.clientDownloading = clientDownloading;
		fragments = new ArrayList<ResourceFragment>(prts);
	} 
	
	public Resource process() {
		for(int i = 1; i <= resourceParts; i++) {
			synchronized(this) {
				while(downloading == maxDownloads || clients.isEmpty()) {
					if(downloading == 0 && clients.isEmpty())
						return null;	//se non c'è nessun thread che sta scaricando e ho finito i client, non posso scaricare la risorsa
					try {
						wait();
					} catch(InterruptedException e) {
						//Thread arrestato mentre aspettava di lanciare altri download
					}
				}
				downloading++;
				new DownloadFragment(i, clients.remove(0), clientDownloading);
			}
		}
		synchronized(processed) {
			while(processed < resourceParts)
				try {
					wait();	//aspetto che tutti i download iniziali finiscano
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
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

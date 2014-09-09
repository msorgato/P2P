package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Downloader implements Remote {
	private	String resourceName;
	private int resourceParts;
	private int maxDownloads;
	private int downloading = 0;
	private int processed = 0;
	private Client clientDownloading;
	private ArrayList<Client> clients;		
	private ArrayList<ResourceFragment> fragments;
	
	private class DownloadFragment extends Thread {
		private Client target;
		private int fragment;

		private DownloadFragment(int fragment, Client target) { 
			this.target = target;
			this.fragment = fragment;
			start();
		}
		
		public void run() {
			boolean requestSent = false;
			while(!requestSent) {
				try {
					target.requestFragment(resourceName, resourceParts, fragment, Downloader.this, clientDownloading);
				} catch(RemoteException e) {
					target = null;
				}
				if(target == null)
					synchronized(clients) {
						
					}
			}
			try {
				target.requestFragment(resourceName, resourceParts, fragment, Downloader.this, clientDownloading);
			} catch(RemoteException e) {
				target = null;
			}
		} 
	}
	
	private class Receiver extends Thread {
		ResourceFragment fragment = null;
		Client client = null;
		
		private Receiver(ResourceFragment fragment, Client client) {
			this.fragment = fragment;
			this.client = client;
			start();
		}
		
		public void run() {
			synchronized(fragments) {
				fragments.add(fragment.getPart() - 1, fragment);
			}
			synchronized(clients) {
				try {
					client.ping();
				} catch(RemoteException e) {
					client = null;
				}
				if(client != null) {
					clients.add(client);
					notifyAll();
				}
			}
			synchronized(Downloader.this) {
				downloading--;
				processed++;
				notify();
			}
		}
	}
	
	protected Downloader(String nm, int prts, ArrayList<Client> cls, Client clientDownloading, int maxDown) throws RemoteException { 
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
				new DownloadFragment(i, clients.remove(0));
			}
		}
		synchronized(fragments) {
			while(processed < resourceParts)
				try {
					wait();	//aspetto che tutti i download iniziali finiscano
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
		}
		//QUI sono finiti tutti i download. chiamo Resource.check() per vedere se è tutto ok.
		//metto il check in un ciclo, che esce solo quando check ritorna -1 oppure se non ci sono più client da cui scaricare.
		int fragmentFailure = Resource.check((ResourceFragment[])fragments.toArray(), resourceName, resourceParts);
		while(fragmentFailure != -1 && !(clients.isEmpty())) {
			synchronized(fragments) {
				new DownloadFragment(fragmentFailure, clients.remove(0));
				try {
					wait();		//aspetto che lo scaricamento del frammento erroneo abbia fine
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				fragmentFailure = Resource.check((ResourceFragment[])fragments.toArray(), resourceName, resourceParts);
			}
		}
		if(fragmentFailure != -1)
			return null;
		return new Resource(resourceName, resourceParts, (ResourceFragment[])fragments.toArray());
	}
	
	public void receive(ResourceFragment fragment, Client client) throws RemoteException {
		new Receiver(fragment, client);
	}
}

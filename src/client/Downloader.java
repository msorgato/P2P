package client;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Downloader {
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
		private ResourceFragment fragToDownload = null;

		private DownloadFragment(int fragment, Client target) { 
			this.target = target;
			this.fragment = fragment;
			start();
		}
		
		public void run() {
			try {
				fragToDownload = target.sendResourceFragment(resourceName, resourceParts, fragment, clientDownloading);
			} catch(RemoteException e) {
				target = null;
				fragToDownload = null;
			}
			synchronized(Downloader.this) {
				if(!(target == null)) {
					clients.add(target);
				}
				downloading--;
				notify();
			}
			synchronized(fragments) {
				if(!(fragToDownload == null))
					fragments.add(fragment - 1, fragToDownload);
				processed++;
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
						return null;	//se non c'� nessun thread che sta scaricando e ho finito i client, non posso scaricare la risorsa
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
		
		//QUI sono finiti tutti i download. chiamo Resource.check() per vedere se � tutto ok.
		//metto il check in un ciclo, che esce solo quando check ritorna -1 oppure se non ci sono pi� client da cui scaricare.
		
		
		
		
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
	//alla fine dello scaricamento dovr� richiamare il metodo statico di Resource per vedere se
	//tutti i frammenti che mi sono stati inviati sono ok.
}

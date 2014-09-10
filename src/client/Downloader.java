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
	
	private ClientGUI gui;
	
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
				gui.addLog("Scarico " + resourceName + " parte " + fragment + " da "  + target.getName());
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
				Downloader.this.notify();
				System.out.println("Notifico il Downloader");
			}
			synchronized(fragments) {
				if(fragToDownload != null) {
					fragments.add((fragment - 1), fragToDownload);
					gui.addLog("Scaricata parte " + fragment + " di " + resourceName);
				}
				processed++;
				fragments.notify();
			}
		} 
	}
	
	protected Downloader(String nm, int prts, ArrayList<Client> cls, Client clientDownloading, int maxDown, ClientGUI gui) { 
		resourceName = nm; 
		resourceParts = prts; 
		maxDownloads = maxDown;
		clients = cls; 
		this.clientDownloading = clientDownloading;
		this.gui = gui;
		fragments = new ArrayList<ResourceFragment>(prts);
	} 
	
	public Resource process() {
		for(int i = 1; i <= resourceParts; i++) {
			synchronized(this) {
				while(downloading == maxDownloads || clients.isEmpty()) {
					if(downloading == 0 && clients.isEmpty())
						return null;	//se non c'è nessun thread che sta scaricando e ho finito i client, non posso scaricare la risorsa
					try {
						this.wait();
						System.out.println("Downloader svegliato");
					} catch(InterruptedException e) {
						//Thread arrestato mentre aspettava di lanciare altri download
						return null;
					}
				}
				downloading++;
				new DownloadFragment(i, clients.remove(0));
			}
		}
		synchronized(fragments) {
			while(processed < resourceParts)
				try {
					fragments.wait();	//aspetto che tutti i download iniziali finiscano
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
		}
		//QUI sono finiti tutti i download. chiamo Resource.check() per vedere se è tutto ok.
		//metto il check in un ciclo, che esce solo quando check ritorna -1 oppure se non ci sono più client da cui scaricare.
		
		int fragmentFailure = Resource.check(fragments, resourceName, resourceParts);
		while(fragmentFailure != -1 && !(clients.isEmpty())) {
			synchronized(fragments) {
				new DownloadFragment(fragmentFailure, clients.remove(0));
				try {
					wait();		//aspetto che lo scaricamento del frammento erroneo abbia fine
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				fragmentFailure = Resource.check(fragments, resourceName, resourceParts);
			}
		}
		if(fragmentFailure != -1)
			return null;
		ResourceFragment[] frags = new ResourceFragment[fragments.size()];
		frags = fragments.toArray(frags);
		return new Resource(resourceName, resourceParts, frags);
	}
}

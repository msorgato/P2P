package server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;

public class ServerGUI {
	private JFrame frame;		//frame principale del Server
	private JTextArea clients; 	//lista clients connessi
	private JTextArea servers; 	//lista server connessi
	private JTextArea logs;		//log di sistema
	
	private ArrayList<String> clientsConnected = new ArrayList<String>();
	private ArrayList<String> serversConnected = new ArrayList<String>();
	
	public ServerGUI() {
		//layout della GUI
		frame = new JFrame("Server");
		frame.setLayout(new BorderLayout());
		
		//inizializzazione del pannello principale
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1,2,5,5));	
		mainPanel.setPreferredSize(new Dimension(450, 200));
		
		//inizializzazione delle varie aree di testo
		clients = new JTextArea();
		JScrollPane clientsArea = new JScrollPane(clients);
		clientsArea.setBorder(BorderFactory.createTitledBorder("Client connessi - risorse"));
		mainPanel.add(clientsArea);	
		clients.setEditable(false);		//le aree di testo non sono modificabili
		
		servers = new JTextArea();
		JScrollPane serversArea = new JScrollPane(servers);
		serversArea.setBorder(BorderFactory.createTitledBorder("Server connessi"));
		mainPanel.add(serversArea);
		servers.setEditable(false);		//le aree di testo non sono modificabili
		
		logs = new JTextArea();
		logs.setFont(new Font("Arial", Font.PLAIN, 12));
		JScrollPane logArea = new JScrollPane(logs);
		DefaultCaret caret = (DefaultCaret) logs.getCaret(); //la visualizzazione di nuovi log causa scrolling automatico
		caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
		logArea.setBorder(BorderFactory.createTitledBorder("Log"));
		logArea.setPreferredSize(new Dimension(430, 180));
		logs.setEditable(false);		//le aree di testo non sono modificabili
		logs.setText("In attesa di operazioni...");
		
		//aggiungo le aree al frame
		frame.add(mainPanel,BorderLayout.CENTER);
		frame.add(logArea,BorderLayout.SOUTH);
		
		//gestisco le impostazioni della finestra
		frame.setSize(450, 450);
		frame.setMinimumSize(new Dimension(450, 450));
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	public void addLog(String log) {
		logs.append("\n" + log);
	}
	
	public void addClient(String name) {
		synchronized(clientsConnected) {
			clientsConnected.add(name);
			refreshClients();
		}
	}
	
	public void removeClient(String name) {
		synchronized(clientsConnected) {
			boolean found = false;
			for(int i = 0; i < clientsConnected.size() && !found; i++) {
				if(clientsConnected.get(i).equals(name)) {
					clientsConnected.remove(i);
					found = true;
					refreshClients();
				}
			}	
		}
	}
	
	private void refreshClients() {
		clients.setText("");
		synchronized(clientsConnected) {
			if(clientsConnected.size() != 0)
				clients.append(clientsConnected.get(0));
			for(int i = 0; i < clientsConnected.size(); i++) {
				clients.append(clientsConnected.get(i));
			}
		}
	}
	
	public void addServer(String name) {
		synchronized(serversConnected) {
			serversConnected.add(name);
			refreshServers();
		}
	}
	
	public void removeServer(String name) {
		synchronized(serversConnected) {
			boolean found = false;
			for(int i = 0; i < serversConnected.size() && !found; i++) {
				if(serversConnected.get(i).equals(name)) {
					serversConnected.remove(i);
					found = true;
					refreshServers();
				}
			}
		}
	}
	
	private void refreshServers() {
		servers.setText("");
		synchronized(serversConnected) {
			if(serversConnected.size() != 0)
				servers.append(serversConnected.get(0));
			for(int i = 0; i < serversConnected.size(); i++) {
				clients.append(serversConnected.get(i));
			}
		}
	}	
}

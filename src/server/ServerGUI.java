package server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ServerGUI {
	private JFrame frame;		//frame principale del Server
	private JTextArea clients; 	//lista clients connessi
	private JTextArea servers; 	//lista server connessi
	private JTextArea log;		//log di sistema
	
	public ServerGUI() throws Exception {
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
		
		servers = new JTextArea();
		JScrollPane serversArea = new JScrollPane(servers);
		serversArea.setBorder(BorderFactory.createTitledBorder("Server connessi"));
		mainPanel.add(serversArea);
		
		
		
	}
}

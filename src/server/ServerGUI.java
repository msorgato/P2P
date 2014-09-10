package server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

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
		clients.setEditable(false);		//le aree di testo non sono modificabili
		
		servers = new JTextArea();
		JScrollPane serversArea = new JScrollPane(servers);
		serversArea.setBorder(BorderFactory.createTitledBorder("Server connessi"));
		mainPanel.add(serversArea);
		servers.setEditable(false);		//le aree di testo non sono modificabili
		
		//preparo il log
		logs = new JTextArea();
		logs.setFont(new Font("Arial", Font.PLAIN, 10));
		JScrollPane logArea = new JScrollPane(logs);
		DefaultCaret caret = (DefaultCaret) logs.getCaret(); //scrolling continuo verso il basso
		caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
		logArea.setBorder(BorderFactory.createTitledBorder("Log"));
		//logs.setForeground(Color.RED);
		logArea.setPreferredSize(new Dimension(430, 180));
		logs.setEditable(false);		//le aree di testo non sono modificabili
		
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
}

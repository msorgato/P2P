package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

public class ClientGUI {
	private JFrame frame;					//frame principale
	private JTextArea resourcesArea;		//lista di risorse del client
	private JTextArea downloadQueue;		//coda di download
	private JTextArea logs;					//logs di sistema
	private JTextField searchArea;			//barra testuale di ricerca risorse
	private JButton disconnectButton;		//pulsante di disconnessione
	private JButton searchButton;			//pulsante di ricerca
	
	private Client client;
	private ArrayList<String> resources = new ArrayList<String>();
	private ArrayList<String> queue = new ArrayList<String>();
	
	public ClientGUI(Client client) {
		this.client = client;
		
		//layout della GUI
		try {
			frame = new JFrame("Client " + client.getName());
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		frame.setLayout(new BorderLayout());

		//inizializzazione del pannello sensibile ad input
		JPanel sensiblePanel = new JPanel();
		sensiblePanel.setLayout(new FlowLayout());
				
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new FlowLayout(5,5,5));
		searchPanel.setBorder(BorderFactory.createTitledBorder("Cerca risorsa e Scarica"));
		searchArea = new JTextField(10);
		searchArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				if(verifyResource(searchArea.getText()))
					searchButton.setEnabled(true);
				else
					searchButton.setEnabled(false);
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if(verifyResource(searchArea.getText()))
					searchButton.setEnabled(true);
				else
					searchButton.setEnabled(false);
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if(verifyResource(searchArea.getText()))
					searchButton.setEnabled(true);
				else
					searchButton.setEnabled(false);
			}
			
		});
			 
		searchButton = new JButton("Cerca");
		searchButton.setPreferredSize(new Dimension(searchButton.getPreferredSize().width,  
											 searchArea.getPreferredSize().height)); 
		searchPanel.add(searchArea); 	//aggiungo la casella di testo
		searchPanel.add(searchButton);	//aggiungo il pulsante cerca
		searchButton.setEnabled(false);
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String content = searchArea.getText();
				String[] splitted = content.split(" ");
				try {
					client.download(splitted[0], Integer.parseInt(splitted[1]));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			
		});
				
		//creo il pulsante Disconnetti
		disconnectButton = new JButton("Disconnetti");
		disconnectButton.setPreferredSize(new Dimension(disconnectButton.getPreferredSize().width,  
													searchPanel.getPreferredSize().height - 9)); 
		disconnectButton.setEnabled(false);
		disconnectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					client.disconnectFromServer();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		//aggiungo barra di ricerca e pulsante disconnetti al pannello sensiblePanel
		sensiblePanel.add(searchPanel);
		sensiblePanel.add(disconnectButton);
		
		//creo pannello risorse complete e coda download
		JPanel resourcePanel = new JPanel();
		resourcePanel.setLayout(new GridLayout(1,2,5,5));
		
		//ora le varie aree di testo
		resourcesArea = new JTextArea();
		JScrollPane resourcesPanel = new JScrollPane(resourcesArea);
		resourcesPanel.setBorder(BorderFactory.createTitledBorder("File completi"));
		resourcePanel.add(resourcesPanel);	
		resourcesArea.setEditable(false);		//le aree di testo non sono modificabili
		
		downloadQueue = new JTextArea();
		JScrollPane downloadQueuePanel = new JScrollPane(downloadQueue);
		downloadQueuePanel.setBorder(BorderFactory.createTitledBorder("Coda download"));
		resourcePanel.add(downloadQueuePanel);
		downloadQueue.setEditable(false);		//le aree di testo non sono modificabili
		
		logs = new JTextArea();
		logs.setFont(new Font("Arial", Font.PLAIN, 12));
		JScrollPane logPanel = new JScrollPane(logs);
		DefaultCaret caret = (DefaultCaret) logs.getCaret(); //la visualizzazione di nuovi log causa scrolling automatico
		caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
		logPanel.setBorder(BorderFactory.createTitledBorder("logs"));
		logPanel.setPreferredSize(new Dimension(500, 200));
		logs.setEditable(false);				//le aree di testo non sono modificabili
		logs.setText("In attesa di operazioni...");
				
		//aggiungo i vari pannelli al JFrame principale
		frame.add(sensiblePanel,BorderLayout.NORTH);
		frame.add(resourcePanel,BorderLayout.CENTER);
		frame.add(logPanel,BorderLayout.SOUTH);				
				
		//gestisco le impostazioni della finestra
		frame.setSize(500, 400);
		frame.setMinimumSize(new Dimension(500,400));
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	public synchronized void addLog(String log) {
		logs.append("\n" + log);
	}
	
	public void addResource(String name) {
		synchronized(resources) {
			resources.add(name);
			refreshResources();
		}
	}
	
	public void removeResource(String name) {
		synchronized(resources) {
			boolean found = false;
			for(int i = 0; i < resources.size() && !found; i++) {
				if(resources.get(i).equals(name)) {
					resources.remove(i);
					found = true;
					refreshResources();
				}
			}	
		}
	}
	
	private void refreshResources() {
		resourcesArea.setText("");
		synchronized(resources) {
			if(resources.size() != 0)
				resourcesArea.append(resources.get(0));
			for(int i = 1; i < resources.size(); i++) {
				resourcesArea.append("\n" + resources.get(i));
			}
		}
	}
	
	public void enableLogout() {
		disconnectButton.setEnabled(true);
	}
	
	public void disableLogout() {
		disconnectButton.setEnabled(false);
	}
	
	private static boolean verifyResource(String content) {
		String[] splitted;
		try {
			splitted = content.split(" ");
		} catch(PatternSyntaxException e) {
			return false;
		}
		if(splitted.length != 2)
			return false;
		try {
			int i = Integer.parseInt(splitted[1]);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}
}

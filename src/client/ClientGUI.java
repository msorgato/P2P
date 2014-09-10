package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;

public class ClientGUI {
	//componenti grafici
	private JFrame frame;					//frame principale
	private JTextArea resourcesArea;		//lista di risorse del client
	private JTextArea downloadQueue;		//coda di download
	private JTextArea logs;					//logs di sistema
	private JTextField searchArea;			//barra testuale di ricerca risorse
	private JButton disconnectButton;		//pulsante di disconnessione
	private JButton searchButton;			//pulsante di ricerca
	
	public ClientGUI() {
		//layout della GUI
		frame = new JFrame("Client");
		frame.setLayout(new BorderLayout());

		//inizializzazione del pannello sensibile ad input
		JPanel sensiblePanel = new JPanel();
		sensiblePanel.setLayout(new FlowLayout());
				
		JPanel findP = new JPanel();
		findP.setLayout(new FlowLayout(5,5,5));
		findP.setBorder(BorderFactory.createTitledBorder("Cerca risorsa e Scarica"));
		searchArea = new JTextField(10);
			 
		searchButton = new JButton("Cerca e Scarica");
		searchButton.setPreferredSize(new Dimension(searchButton.getPreferredSize().width,  
											 searchArea.getPreferredSize().height)); 
		findP.add(searchArea); 	//aggiungo la casella di testo
		findP.add(searchButton);	//aggiungo il pulsante cerca
		searchButton.setEnabled(false);
				
		//creo il pulsante Disconnetti
		disconnectButton = new JButton("    avvio client...    ");
		disconnectButton.setPreferredSize(new Dimension(disconnectButton.getPreferredSize().width,  
													findP.getPreferredSize().height-9)); 
		disconnectButton.setEnabled(false);
		
		//aggiungo barra di ricerca e pulsante disconnetti al pannello sensiblePanel
		sensiblePanel.add(findP);
		sensiblePanel.add(disconnectButton);
		
		//creo pannello resourcesAreatatus -> resourcesArea completi e coda download
		JPanel resourcesAreatatusP = new JPanel();
		resourcesAreatatusP.setLayout(new GridLayout(1,2,5,5));
		
		//ora le varie aree di testo
		resourcesArea = new JTextArea();
		JScrollPane resourcesAreaList = new JScrollPane(resourcesArea);
		resourcesAreaList.setBorder(BorderFactory.createTitledBorder("File completi - logs download"));
		resourcesAreatatusP.add(resourcesAreaList);		
		
		downloadQueue = new JTextArea();
		JScrollPane downloadQueueList = new JScrollPane(downloadQueue);
		downloadQueueList.setBorder(BorderFactory.createTitledBorder("Coda download"));
		resourcesAreatatusP.add(downloadQueueList);
		
		//preparo il logs sottostante
		logs = new JTextArea();
		logs.setFont(new Font("Arial", Font.PLAIN, 10));
		JScrollPane logList = new JScrollPane(logs);
		DefaultCaret caret = (DefaultCaret) logs.getCaret(); //scrolling continuo verso il basso
		caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
		logList.setBorder(BorderFactory.createTitledBorder("logs"));
		//logs.setForeground(Color.RED);
		logList.setPreferredSize(new Dimension(470, 180));
				
		//aggiungo i vari pannelli al JFrame principale
		frame.add(sensiblePanel,BorderLayout.NORTH);
		frame.add(resourcesAreatatusP,BorderLayout.CENTER);
		frame.add(logList,BorderLayout.SOUTH);		
		
		//non permetto di editare il testo
		resourcesArea.setEditable(false);
		downloadQueue.setEditable(false);
		logs.setEditable(false);
				
		//impostazioni generali della finestra
		frame.setSize(470, 400);
		frame.setMinimumSize(new Dimension(470,400));
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	
}

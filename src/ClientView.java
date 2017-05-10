/* File name:   ClientView.java
 * Author:      Mark Kaganovsky
 * Course:      CST8221 - JAP, Lab Section: 302
 * Assignment:  2 part 2
 * Date:        April 21 2016
 * Professor:   Svillen Ranev
 * Purpose:     The ClientView and inner class ClientConnectionRunnable.
 *              The ClientView creates a ClientConnectionRunnable and adds it to a thread when the connect button is pressed.
 * Class List:  ClientView
 *              ClientConnectionRunnable
 */

import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;






/**
 * This class serves as the client's GUI and handles all connections.
 * 
 * @author  Mark Kaganovsky
 * @version 1.3
 * @see     JFrame
 * @since   1.8.0_20
 */
public class ClientView extends javax.swing.JFrame {
	private ClientConnectionRunnable clientConnectionRunnable;
	
	/** Creates the GUI. */
	public ClientView() {
		initComponents();
		
		// Set the text cursor position.
		hostTextField.requestFocus();
		hostTextField.setCaretPosition(0);
		
		// Add event handlers.
		connectButton.addActionListener((event) -> {
			connect();
		});
		
		sendButton.addActionListener((event) -> {
			sendCommand(commandTextField.getText());
		});
	}
	
	
	
	
	/** Connect to a client using the port and host specified by the user. */
	private void connect(){
		clientConnectionRunnable = new ClientConnectionRunnable(hostTextField.getText(), (String)portComboBox.getSelectedItem());
		new Thread(clientConnectionRunnable).start();
	}
	
	
	
	
	/**
	 * Adds the string to the command queue.
	 * 
	 * @param command The string to add to the command queue.
	 */
	private void sendCommand(String command){
		clientConnectionRunnable.addToQueue(command);
	}
	
	
	
	
	/**
	 * Appends a string to the terminal.
	 * 
	 * @param message The string to append to the terminal.
	 */
	private void appendToTerminal(String message){
		EventQueue.invokeLater(() -> { terminalTextArea.append(message); });
	}
	
	
	
	
	/** Set this GUIs controls to reflect an active connection. */
	private void setControlsConnected(){
		EventQueue.invokeLater(() -> {
			connectButton.setEnabled(false);
			connectButton.setBackground(Color.BLUE);
			sendButton.setEnabled(true);
		});
	}
	
	
	
	
	/** Set this GUIs controls to reflect an inactive connection. */
	private void setControlsDisconnected(){
		EventQueue.invokeLater(() -> {
			connectButton.setEnabled(true);
			connectButton.setBackground(Color.RED);
			sendButton.setEnabled(false);
		});
	}
	
	
	
	
	/**
	 * This class represents a clients connection to the server.
	 * 
	 * It handles connecting to the server, sending and recieving messages, and closing the connection.
	 * 
	 * @author  Mark Kaganovsky
	 * @version 1.0
	 * @see     Runnable
	 * @since   1.8.0_20
	 */
	private class ClientConnectionRunnable implements Runnable {
		/** The host to connect to. */
		private String host;
		
		/** The port in string format. */
		private String portStr;
		
		/** Socket representing the server. */
		private Socket server;
		
		/** Object input stream. Only {@link String}s are received over it. */
		private ObjectInputStream input;
		
		/** Object output stream. Only {@link String}s are sent over it. */
		private ObjectOutputStream output;
		
		/** Queue which contains the commands from the client. */
		private BlockingQueue<String> commandQueue;
		
		/**
		 * Default constructor.
		 * 
		 * @param h The host to connect to.
		 * @param p The port to connect on.
		 */
		public ClientConnectionRunnable(String h, String p) {
			portStr = p;
			host = h;
			commandQueue = new LinkedBlockingQueue<>();
		}
		
		/** Add a command to the command queue. */
		public void addToQueue(String command){
			commandQueue.add(command);
		}
		
		/**
		 * Handles the connection to the client.
		 * 
		 * Connects to the client, creates the IO streams, then calls the method to handle the active connection.
		 * This method alters the GUIs state.
		 */
		@Override
		public void run() {
			// Prevent the user from calling connect multiple times before a connection can be made.
			setControlsConnected();
			
			// Try to convert the port entered by the user to an int.
			int port;
			
			try{
				port = Integer.parseInt(portStr);
			}
			catch(NumberFormatException e){
				appendToTerminal("CLIENT>ERROR: Invalid port number.\n");
				setControlsDisconnected();
				return;
			}
			
			boolean continueWithConnection = false;
			
			// Check if host exists
			try {
				continueWithConnection = false;
				InetAddress.getByName(host);
				continueWithConnection = true;
			}
			catch (UnknownHostException e) {
				appendToTerminal("CLIENT>ERROR: Unknown Host.\n");
			}
			catch(Exception e){
				appendToTerminal("CLIENT>ERROR: Some other error occured - " + e.getMessage() + "\n");
			}
			finally{
				if(!continueWithConnection){
					setControlsDisconnected();
					return;
				}
			}
			
			// Host exists, try to connect to server with timeout.
			try {
				continueWithConnection = false;
				server = new Socket();
				
				/* Some servers will accept your connection but may not follow the same object serialization protocol,
				 * causing a readObject() call to block forever, therefore a socket timeout needs to be used.
				 */
				server.setSoTimeout(5000);
				
				server.connect(new InetSocketAddress(host, port));
				continueWithConnection = true;
			}
			catch(IllegalArgumentException e){
				appendToTerminal("CLIENT>ERROR: Port number is out of range.\n");
			}
			catch(SecurityException e){
				appendToTerminal("CLIENT>ERROR: A security manager has prevented a connection.\n");
			}
			catch(IOException e){
				appendToTerminal("CLIENT>ERROR: Connection refused: server is not available. Check port or restart server.\n");
			}
			catch (Exception e) {
				appendToTerminal("CLIENT>ERROR: Some other error occured - " + e.getMessage() + "\n");
			}
			finally{
				if(!continueWithConnection){
					setControlsDisconnected();
					return;
				}
			}
			
			// Get object IO streams.
			try{
				continueWithConnection = false;
				output = new ObjectOutputStream(server.getOutputStream());
				output.flush();
				input = new ObjectInputStream(server.getInputStream());
				continueWithConnection = true;
			}
			catch(SocketTimeoutException e){
				appendToTerminal("CLIENT>ERROR: Time out. A connection to the server was made but it does not follow the same protocol.\n");
			}
			catch(IOException e){
				appendToTerminal("CLIENT>ERROR: Could not create I/O streams - " + e.getMessage() + "\n");
			}
			catch(Exception e){
				appendToTerminal("CLIENT>ERROR: Some other error occured - " + e.getMessage() + "\n");
			}
			finally{
				if(!continueWithConnection){
					closeConnection();
					setControlsDisconnected();
					return;
				}
			}
			
			// Successfully connected.
			appendToTerminal("Connected to " + server.toString() + "\n");
			
			handleActiveConnection();
		}
		
		/**
		 * Once connected, this method handles the connection.
		 * 
		 * It goes into an infinite loop which:
		 *     1. Waits until the command queue has at least 1 entry.
		 *     2. Sends the command.
		 *     3. Reads the response appends it to the terminal text area.
		 *     4. Goes back to 1.
		 * 
		 * This method alters the GUIs state.
		 */
		private void handleActiveConnection(){
			// Main loop.
			while(true){
				// Wait until there is something to send.
				while(commandQueue.isEmpty()){
					// Limit cpu usage by sleeping for a little bit.
					try{
						Thread.sleep(25);
					}
					catch(InterruptedException e){
						// Do nothing.
					}
				}
				
				// Send the command.
				try {
					output.writeObject(commandQueue.remove());
				}
				catch (IOException e) {
					appendToTerminal("ERROR: Could not send command, closing connection...\n");
					closeConnection();
					setControlsDisconnected();
					return;
				}
				
				// Read the response.
				try {
					String response = (String)input.readObject();
					
					if(response.equals(Server.SR_CLS)){
						EventQueue.invokeLater(() -> { terminalTextArea.setText(null); });
					}
					else if(response.equals(Server.SR_END)){
						appendToTerminal("SERVER>Connection closed.\n");
						closeConnection();
						setControlsDisconnected();
						break;
					}
					else{
						appendToTerminal("SERVER>" + response + "\n");
					}
				}
				catch(ClassNotFoundException e){
					appendToTerminal("ERROR: Unknown response recieved.\n");
					closeConnection();
					setControlsDisconnected();
					return;
				}
				catch (IOException e) {
					appendToTerminal("ERROR: Could not read response - " + e.getMessage() + "\n");
					closeConnection();
					setControlsDisconnected();
					return;
				}
			}
		}
		
		
		/** Closes the connection to the server. */
		private void closeConnection(){
			// Try to close the connection.
			try {
				if(output != null){
					output.close();					
				}
				
				if(input != null){
					input.close();					
				}
				
				server.close();
				appendToTerminal("CLIENT>Connection closed.\n");
			}
			catch (IOException e) {
				appendToTerminal("CLIENT>ERROR: An error occured while closing the connection.\n");
			}
		}
	}
	
	
	
	
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        hostPortPanel = new javax.swing.JPanel();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portComboBox = new javax.swing.JComboBox();
        connectButton = new javax.swing.JButton();
        commandTerminalPanel = new javax.swing.JPanel();
        commandPanel = new javax.swing.JPanel();
        commandTextField = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
        terminalPanel = new javax.swing.JPanel();
        terminalScrollPane = new javax.swing.JScrollPane();
        terminalTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Kaganovsky's Client");
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(600, 550));
        setPreferredSize(new java.awt.Dimension(600, 550));

        hostPortPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.red, 10), "SET CONNECTION", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        hostPortPanel.setLayout(new java.awt.GridBagLayout());

        hostLabel.setDisplayedMnemonic('h');
        hostLabel.setLabelFor(hostTextField);
        hostLabel.setText("Host:");
        hostLabel.setMaximumSize(null);
        hostLabel.setMinimumSize(null);
        hostLabel.setPreferredSize(new java.awt.Dimension(40, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        hostPortPanel.add(hostLabel, gridBagConstraints);

        hostTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        hostTextField.setText("localhost");
        hostTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        hostTextField.setMargin(new java.awt.Insets(2, 6, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        hostPortPanel.add(hostTextField, gridBagConstraints);

        portLabel.setDisplayedMnemonic('P');
        portLabel.setLabelFor(portComboBox);
        portLabel.setText("Port:");
        portLabel.setMaximumSize(null);
        portLabel.setMinimumSize(null);
        portLabel.setPreferredSize(new java.awt.Dimension(40, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        hostPortPanel.add(portLabel, gridBagConstraints);

        portComboBox.setEditable(true);
        portComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "8080", "65000", "65535" }));
        portComboBox.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        hostPortPanel.add(portComboBox, gridBagConstraints);

        connectButton.setBackground(java.awt.Color.red);
        connectButton.setMnemonic('c');
        connectButton.setText("Connect");
        connectButton.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        hostPortPanel.add(connectButton, gridBagConstraints);

        getContentPane().add(hostPortPanel, java.awt.BorderLayout.PAGE_START);

        commandTerminalPanel.setLayout(new java.awt.BorderLayout());

        commandPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 10), "CLIENT REQUEST"), javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        commandPanel.setLayout(new java.awt.GridBagLayout());

        commandTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        commandTextField.setText("Type a server request line");
        commandTextField.setPreferredSize(new java.awt.Dimension(480, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        commandPanel.add(commandTextField, gridBagConstraints);

        sendButton.setMnemonic('s');
        sendButton.setText("Send");
        sendButton.setEnabled(false);
        sendButton.setMaximumSize(new java.awt.Dimension(70, 20));
        sendButton.setMinimumSize(new java.awt.Dimension(70, 20));
        sendButton.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        commandPanel.add(sendButton, gridBagConstraints);

        commandTerminalPanel.add(commandPanel, java.awt.BorderLayout.PAGE_START);

        terminalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.blue, 10), "TERMINAL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        terminalScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        terminalTextArea.setEditable(false);
        terminalTextArea.setColumns(20);
        terminalTextArea.setRows(5);
        terminalTextArea.setPreferredSize(null);
        terminalScrollPane.setViewportView(terminalTextArea);

        javax.swing.GroupLayout terminalPanelLayout = new javax.swing.GroupLayout(terminalPanel);
        terminalPanel.setLayout(terminalPanelLayout);
        terminalPanelLayout.setHorizontalGroup(
            terminalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(terminalScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
        );
        terminalPanelLayout.setVerticalGroup(
            terminalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(terminalScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
        );

        commandTerminalPanel.add(terminalPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(commandTerminalPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel commandPanel;
    private javax.swing.JPanel commandTerminalPanel;
    private javax.swing.JTextField commandTextField;
    private javax.swing.JButton connectButton;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JPanel hostPortPanel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JComboBox portComboBox;
    private javax.swing.JLabel portLabel;
    private javax.swing.JButton sendButton;
    private javax.swing.JPanel terminalPanel;
    private javax.swing.JScrollPane terminalScrollPane;
    private javax.swing.JTextArea terminalTextArea;
    // End of variables declaration//GEN-END:variables
}

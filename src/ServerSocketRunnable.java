/* File name:   ServerSocketRunnable.java
 * Author:      Mark Kaganovsky
 * Course:      CST8221 - JAP, Lab Section: 302
 * Assignment:  2 part 2
 * Date:        April 21 2016
 * Professor:   Svillen Ranev
 * Purpose:     This class represents the server's client connection handler.
 *              In other words, once the Server makes a connection, it adds
 *              one of these to a thread to handle the connection.
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * The server runnable to be put into a thread and handle the client when a connection is made.
 * 
 * @author  Mark Kaganovsky
 * @version 1.0
 * @see     Runnable
 * @since   1.8.0_20
 */
public class ServerSocketRunnable implements Runnable {
	/** The socket which this runnable manages. */
	private Socket socket;
	
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	
	
	/**
	 * Default constructor.
	 * @param s An open connection to a client obtained by accepting a client.
	 */
	public ServerSocketRunnable(Socket s) {
		socket = s;
	}
	
	
	
	
	/** Creates the object IO streams and enters the main server loop. */
	@Override
	public void run() {
		// Get object streams.
		try{
			output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
			input = new ObjectInputStream(socket.getInputStream());
		}
		catch(Exception e){
			System.out.println("ERROR: could not create streams " + e.getMessage());
			try {
				socket.close();
			}
			catch (IOException e1) {
				System.out.println("ERROR: Could not close connection - " + e.getMessage());
			}
			return;
		}
		
		// Receive commands until EOFException thrown
		try{
			// The delay after a command is processed in milliseconds. Increase this value to test the client's command queuing.
			long serverDelayPerCommand = 100;
			
			// Create the command verifier.
			Pattern validCommandPattern = Pattern.compile("-(end|echo|time|date|help|cls)(-.+)?");
			
			// Create the time and date formatters.
			DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
			DateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
			
			// Main loop
			while(true){
				String command  = (String)input.readObject();
				String response = "ERROR: Unrecognized command.";
				
				// Make sure that the string is in a valid format.
				if(validCommandPattern.matcher(command).matches()){
					if(command.startsWith("-echo-")){
						response = "ECHO:" + command.substring(6);
					}
					else if(command.startsWith("-echo")){
						response = "ECHO:";
					}
					else if(command.startsWith("-time")){
						response = "TIME: " + timeFormat.format(new Date());
					}
					else if(command.startsWith("-date")){
						response = "DATE: " + dateFormat.format(new Date());
					}
					else if(command.startsWith("-help")){
						response = "Available Services:\nend\necho\ntime\ndate\nhelp\ncls\n";
					}
					else if(command.startsWith("-cls")){
						response = Server.SR_CLS;
					}
					// If the response is an end command, break from the loop.
					else if(command.startsWith("-end")){
						break;
					}
				}
				
				// Write the response
				output.writeObject(response);
				
				// Sleep.
				try{
					Thread.sleep(serverDelayPerCommand);
				}
				catch(InterruptedException e){
					// Do nothing.
				}
			}
			
			output.writeObject(Server.SR_END);
		}
		catch(EOFException e){
			// Connection closed on client side.
		}
		catch(ClassNotFoundException e){
			System.out.println("ERROR: Unknown object type recieved.");
		}
		catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
		}
		
		// Close the connection
		System.out.println("Server Socket: Closing client connection...");
		try {
			output.close();
			input.close();
			socket.close();
		}
		catch (Exception e) {
			System.out.println("ERROR: Could not close connection. " + e.getMessage());
		}
	}
}

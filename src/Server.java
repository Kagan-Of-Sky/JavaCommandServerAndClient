/* File name:   Server.java
 * Author:      Mark Kaganovsky
 * Course:      CST8221 - JAP, Lab Section: 302
 * Assignment:  2 part 2
 * Date:        April 21 2016
 * Professor:   Svillen Ranev
 * Purpose:     This class represents the listen port of the server.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * This class is the server used with the {@link ClientView}.
 * It only listens for new clients, clients are handled by {@link ServerSocketRunnable}. 
 * 
 * @author  Mark Kaganovsky
 * @version 1.0
 * @see     ServerSocketRunnable
 * @since   1.8.0_20
 */
public class Server {
	/** {@value} - The server response for a clear screen command. */
	public static final String SR_CLS = "cls";
	
	/** {@value} - The server response for for closing a connection. */
	public static final String SR_END  = "end";
	
	
	
	
	/**
	 * Starts the listening port of the server. Every new connection is handled in its
	 * own thread by the {@link ServerSocketRunnable} class.
	 * 
	 * @param args Takes an optional port number as an argument.
	 *             If one is not supplied then 65535 is used.
	 */
	public static void main(String[] args) {
		int port = 65535;
		
		// Check if a port argument has been provided from the command line.
		if(args.length == 1){
			String portString = args[0];
			
			System.out.println("Using provided port: " + portString);
			
			try {
				port = Integer.parseInt(portString);
			}
			catch (NumberFormatException e) {
				System.out.println("ERROR: The provided port '" + portString + "' is not a number.");
				return;
			}
		}
		else{
			System.out.println("Using default port: " + port);
		}
		
		// Create a thread pool.
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		// Create and run the server.
		try(ServerSocket serverSocket = new ServerSocket(port)) {
			// Server listen thread.
			while(true){
				Socket client = serverSocket.accept();
				
				System.out.println("Connecting to a client " + client);
				
				executorService.execute(new ServerSocketRunnable(client));
			}
		}
		catch (IllegalArgumentException e){
			System.out.println("ERROR: Out of range port number.");
		}
		catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		catch(SecurityException e){
			System.out.println("ERROR: " + e.getMessage());
		}
		
		// Shut down the rest of the connections.
		System.out.println("Shutting down connections...");
		executorService.shutdown();
	}
}

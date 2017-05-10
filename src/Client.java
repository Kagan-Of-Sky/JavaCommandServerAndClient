/* File name:   Client.java
 * Author:      Mark Kaganovsky
 * Course:      CST8221 - JAP, Lab Section: 302
 * Assignment:  2 part 2
 * Date:        April 21 2016
 * Professor:   Svillen Ranev
 * Purpose:     Launcher for the ClientView.
 */




/**
 * Create the ClientView.
 * 
 * @author  Mark Kaganovsky
 * @version 1.0
 * @see     ClientView
 * @since   1.8.0_20
 */
public class Client {
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(() -> {
			ClientView client = new ClientView();
			client.setVisible(true);
		});
	}
}

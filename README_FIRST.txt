Bonuses Attemped:
	Bonus 1
	Bonus 2

Bonus 1 Implementation:
	A cached thread pool is used so that new threads are created as needed.

Bonus 2 Implementation:
	The ClientView has an inner class named ClientConnectionRunnable which is instansiated and added to a thread whenever the
	connect button is pressed. It handles everything from the initial connection, to the active connection, to closing the connection.
	This ClientConnectionRunnable calls the appropriate ClientView methods to safely modify the GUI.
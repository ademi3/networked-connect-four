
package server;

import shared.Message;
import shared.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {

	int count = 1; // tracks the number of clients that have connected to the server
	// list of all clients that are connected to the server
	public final List<ClientThread> clients = Collections.synchronizedList(new ArrayList<>());
	// creating a set of the usernames that have already been chosen
	private static final Set<String> chosenUsernames = Collections.synchronizedSet(new HashSet<>());
	// list of clients that are specifically waiting to be paired to a game
	public final List<ClientThread> clientsWaitingForGame = Collections.synchronizedList(new ArrayList<>());


	TheServer server = new TheServer(); // "server" is a TheServer thread which listens for client connections

	// constructor that starts the server thread
	public Server() {
		this.server.start();
	}
	// main method to actually start the server
	public static void main(String[] args) {
		System.out.println("Starting the server...");
		// instantiating and starting the server
		Server server = new Server();

	}

	public class TheServer extends Thread {

		public void run() {

			try (ServerSocket serverSocket = new ServerSocket(5555)) {
				System.out.println("Server is waiting for a client!");

				while (true) {
					Socket clientSocket = serverSocket.accept(); // waiting/blocking until a client tries to connect to the server
					// creating a new ClientThread for a client that just connected and passing in a socket for communication and its unique ID
					ClientThread acceptedClient = new ClientThread(clientSocket, count++);
					clients.add(acceptedClient);
					acceptedClient.start(); // beginning communication with the client in a new thread

					System.out.println("Client #" + acceptedClient.count + " established connection to the server.");
				}

			} catch (Exception error) {
				System.err.println("Server did not launch." + error.getMessage());
			}
		}
	}


	public class ClientThread extends Thread {
		private final Socket socket;
		private final int count;
		ObjectOutputStream out;
		private ObjectInputStream in;
		private GameInstance gameInstance;
		private char playerSymbol;

		String username = "";

		// constructor to initialize a new ClientThread object when a client connects to the server
		ClientThread(Socket s, int count){
			this.socket = s;
			this.count = count;
		}


		public void run() {

			try {
				out = new ObjectOutputStream(this.socket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(this.socket.getInputStream());
				this.socket.setTcpNoDelay(true);

				sendMessage(new Message(MessageType.REQUEST_USERNAME)); // the streams are set, so now asking for client's username

				while (true) {
					try {
						Message message = (Message) in.readObject(); // reading an object sent from the client over the network and casting it to a Message type
						handleMessage(message);
					}
					catch (Exception e) { // handling logic after a client disconnects from a game or an error happens while data is read
						System.err.println("OOOPS...Something wrong with the socket from client #" + this.count + "...disconnecting this client");
						e.printStackTrace();
						break; // exiting the loop on the first error (to not clog console)
					}
				}
			}
			catch (Exception e){
				System.out.println("Issue opening streams for client #" + count + ".");
			}
			finally {
				try {
					if (gameInstance != null) { // checking if there is a game being played
						gameInstance.processDisconnect(this); // processDisconnect() is called to inform the opponent that the other player left
					}
					clients.remove(this); // removing the current ClientThread from the "clients" list, since it is no longer an active client

					if (username.isEmpty() == false) {
						synchronized (chosenUsernames) {
							chosenUsernames.remove(username); // removing the username of the player that disconnected from the set
							System.out.println("Removed the username " + "' " + username + "' since the player disconnected");
						}
					}
					socket.close(); // closing the socket connection as it is no longer needed
				}
				catch (IOException error) {
					System.err.println("Problem closing socket for client #" + count);
				}
			}
		} // end of run() method

		// this method is responsible for processing incoming messages from the client
		// it determines the type of message and directs the program to handle it as needed
		private void handleMessage(Message message) {

			MessageType messageType = message.getType();

			if (messageType.equals(MessageType.SENT_USERNAME)) {
				String providedUsername = message.getMessageData();

				synchronized (chosenUsernames) {
					System.out.println("Client #" + count + " submitted: " + providedUsername);
					if (chosenUsernames.contains(providedUsername) == false) {
						chosenUsernames.add(providedUsername); // adding it to the set
						this.username = providedUsername;
						System.out.println("Client #" + count + " username: " + username);

						// printing the updated set of usernames
						System.out.println("Currently chosen usernames: " + chosenUsernames);

						synchronized (clientsWaitingForGame) {
							clientsWaitingForGame.add(this);
							// if there are at least two clients waiting for a game, then the server is going to start a new game between the two players
							// that have been waiting the longest
							if (clientsWaitingForGame.size() >= 2) {
								Server.ClientThread player1 = clientsWaitingForGame.remove(0); // removing the oldest client from the waiting list
								Server.ClientThread player2 = clientsWaitingForGame.remove(0); // removing the second oldest from the waiting list
								new GameInstance(player1, player2); // creating a Connect Four game between those two players
							}
						}
					}
					else { // informing the client that the username they entered has already been taken
						sendMessage(new Message(MessageType.USERNAME_TAKEN, "Username not available. Enter another one."));
						System.out.println("Client #" + count + " entered a duplicate username.");
					}
				}
			}

			else if (messageType.equals(MessageType.PIECE_MOVE)) {
				// client PIECE_MOVE messages include only the col, while the server response includes the (row, col) coordinate as well
				gameInstance.interpretMove(this, message.getCol());
			}
			else if (messageType.equals(MessageType.SEND_CHAT)) {
				if (gameInstance != null) {
					gameInstance.updateChat(this, message.getMessageData());
				}
				else {
					System.out.println("Received chat message before being in a game. Thus, ignoring.");
				}
			}
			else if (messageType.equals(MessageType.REMATCH_PROPOSAL)) {
				gameInstance.processRematchProposal(this);
			}
			else if (messageType.equals(MessageType.REMATCH_RESPONSE)) {
				gameInstance.processRematchResponse(this, message.hasAcceptedRematch());
			}
			else if (messageType.equals(MessageType.USER_LEAVE)) {
				System.out.println("Client #" + count + " left the game.");
				try {
					socket.close(); // closing the network connection with the specific client associated with the current ClientThread instance
									// this causes the execution to go to the finally block because an exception would be triggered in readObject
				}
				catch (IOException error) {
					System.err.println("Problems closing socket: " + error.getMessage());
				}
			}
		}

		// setter that assigns the GameInstance and color symbol ('R' or 'Y') to the client
		// this is called when the server assigns two players to a game

		public void setGameInstance(GameInstance game, char symbol) {
			this.gameInstance = game;
			this.playerSymbol = symbol;
		}

		public char getPlayerSymbol() {
			return playerSymbol;
		}

		public void sendMessage(Message message) {
			try {
				out.writeObject(message); // serializing the message object and writing it to the output stream
			}
			catch (IOException error) {
				System.err.println("Attempt to send message to client #" + count + " failed.");
			}
		}
	} //end of client thread
}

	
	

	

import javafx.application.Platform;
import shared.Message;
import shared.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Client extends Thread {
	
	Socket connectionSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	private GuiClient guiClient;

	// constructor that stores a reference to the GUI class inside the Client
	// this will allow the Client to talk to the GUI
	public Client(GuiClient guiClient) {
		this.guiClient = guiClient;
	}

	public void run() {
		
		try {
			this.connectionSocket = new Socket("127.0.0.1", 5555); // creating a new socket and connecting it to the server
			this.out = new ObjectOutputStream(this.connectionSocket.getOutputStream()); // setting up the output stream used to send objects from the client to the server
			this.out.flush();
			this.in = new ObjectInputStream(this.connectionSocket.getInputStream()); // setting up the input stream used to receive objects from the server to the client
			this.connectionSocket.setTcpNoDelay(true);

			System.out.println("Connection to server established at " + connectionSocket.getRemoteSocketAddress());

			// test code
			 //Message testMessage = new Message(MessageType.SEND_CHAT, "Hi from client!");
			 //sendMessage(testMessage);
			// ---

			while (true) {
				try {
					Object readInObject = in.readObject(); // reading an object that was sent from the server and storing it in the readInObject variable
					if ((readInObject != null) && (readInObject.getClass().equals(Message.class))) { // checking if the object read in from the server is of type shared.Message
						Message message = (Message) readInObject; // if it is, then it is cast from a generic Object to a shared.Message object type
						Platform.runLater(() -> guiClient.processServerMessage(message)); // ensuring that processServerMessage() runs on the JavaFX UI thread, not the Client background thread
					} else {
						System.err.println("An unwanted object type was sent in: " + readInObject);
					}
				} catch (Exception error) {
					error.printStackTrace();
					break;
				}
			}
		}
		catch (IOException error) {
			error.printStackTrace();
		}
    }

	// this method sends a shared.Message object to the server via the ObjectOutputStream
	public void sendMessage(Message message) {
		System.out.println("Sending message to server: " + message.getType() + ", col: " + message.getCol());
		try {
			out.writeObject(message); // serializing the shared.Message object and writing it to the output stream
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

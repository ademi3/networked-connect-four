This project contains two applications: a multithreaded server and a JavaFX-based client that communicate over TCP to create a networked Connect Four game.

The server is a Java socket application that listens for incoming client connections on port 5555. Each connected client is assigned its own thread, allowing multiple users to connect simultaneously. When two
players are available, the server automatically pairs them into a game session managed by a GameInstance. Game logic such as move validation, win/draw detection, rematch handling, and player disconnects is
processed server-side.

The client is a JavaFX GUI application that connects to the server and allows players to visually interact with the game. After providing a unique username, players can make moves on the board interface, send
chat messages, and respond to rematch requests. The client communicates with the server using serialized message objects.

How to run the project:


Step #1 (Start the Server):  

Open a terminal.

From the project root, do:

cd server  
mvn clean compile  
mvn exec:java -Dexec.mainClass="server.Server"  

You should see:

Starting the server...  
Server is waiting for a client!  

Leave this terminal running.  


Step #2 (Start the First Client):  

Open a new terminal window.

From the project root, do:

cd client  
mvn javafx:run  

Now, one client window has been started.  


Step #3 (Start a Second Client):  

Open another terminal window.

From the project root, do:

cd client  
mvn javafx:run  

Now, two client windows have been started.  


Step #4 (Joining a Game):  

Enter two unique usernames (one for each client window).

The server automatically pairs them into a game.


Now you're all set to start!  
  

Screenshots:  

Joining a game/entering username: 

<img width="551" height="508" alt="Screenshot 2026-02-22 at 10 53 11 AM" src="https://github.com/user-attachments/assets/4501a2c5-19c9-48b5-b674-9d9411f0a77c" />
<img width="551" height="508" alt="Screenshot 2026-02-22 at 10 53 11 AM" src="https://github.com/user-attachments/assets/4501a2c5-19c9-48b5-b674-9d9411f0a77c" />

<img width="552" height="504" alt="Screenshot 2026-02-22 at 10 55 06 AM" src="https://github.com/user-attachments/assets/8709e7fc-401e-4461-b254-4ddeb4eeb1db" />
<img width="552" height="504" alt="Screenshot 2026-02-22 at 10 55 06 AM" src="https://github.com/user-attachments/assets/8709e7fc-401e-4461-b254-4ddeb4eeb1db" />

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

Images of two users entering their username/joining a game:

<img width="551" height="508" alt="Screenshot 2026-02-22 at 10 53 11 AM" src="https://github.com/user-attachments/assets/4501a2c5-19c9-48b5-b674-9d9411f0a77c" />

<img width="552" height="504" alt="Screenshot 2026-02-22 at 10 55 06 AM" src="https://github.com/user-attachments/assets/8709e7fc-401e-4461-b254-4ddeb4eeb1db" />

Image of the two users playing in the same game:

<img width="1404" height="779" alt="Screenshot 2026-02-22 at 11 44 39 AM" src="https://github.com/user-attachments/assets/4738758d-9ae0-48d1-85d4-02cc99c1e8d2" />

Image of the final result:

<img width="1391" height="776" alt="Screenshot 2026-02-22 at 12 03 37 PM" src="https://github.com/user-attachments/assets/e3d5d648-5102-427e-ae7b-a0b1343723e0" />







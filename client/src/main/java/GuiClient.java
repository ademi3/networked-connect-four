
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import java.awt.*;
import java.util.Scanner;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import shared.Message;
import shared.MessageType;

import java.util.List;

public class GuiClient extends Application{

	private Stage primaryStage;
	private Scene startScene;
	private Scene connectFourGame;
	private TextField usernameField;
	private Circle[][] boardSlots = new Circle[6][7]; // creating the 6 x 7 2D array structure of the board
	private boolean isCurrPlayerTurn; // current player refers to the player that is associated with the current game state
	private char mySymbol;
	private Label playerTurnLabel;
	private Label gameResultLabel; // indicates whether the current player won/lost/drew


	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;
		primaryStage.setTitle("CONNECT FOUR");
		showStartScreen();
	}
	private void showStartScreen() {
		Scene startScene = organizeStartScreen();
		primaryStage.setScene(startScene);
		primaryStage.show();

	}

	private Scene organizeStartScreen() {

		Label introNote = new Label("Welcome to Connect Four!");
		introNote.setFont(Font.font("Impact", 34));
		introNote.setTranslateY(-100);
		introNote.setTextFill(Color.DARKRED);

		Label promptUserName = new Label("Enter unique username: ");
		promptUserName.setFont(Font.font("Impact", 19));
		promptUserName.setTranslateY(-10);

		usernameField = new TextField();
		usernameField.setMaxWidth(220);
		usernameField.setPrefWidth(165);
		usernameField.setPrefHeight(30);

		Button searchOpponentBtn = new Button("Search for Opponent");
		searchOpponentBtn.setOnAction(e -> connectToServer()); // calling handleOpponentSearch()
		searchOpponentBtn.setTranslateY(40);
		searchOpponentBtn.setFont(Font.font("Impact", 18));
		searchOpponentBtn.setPrefWidth(180);
		searchOpponentBtn.setPrefHeight(55);
		searchOpponentBtn.setStyle("-fx-background-color: khaki;");

		VBox vBoxForStartScreen = new VBox(20, introNote, promptUserName, usernameField, searchOpponentBtn);
		vBoxForStartScreen.setAlignment(Pos.CENTER);
		vBoxForStartScreen.setStyle("-fx-background-color: cornflowerblue;");

		return new Scene(vBoxForStartScreen, 550, 480);
	}


	private Scene organizeGameScene() {

		playerTurnLabel = new Label(); // this label will indicate which player's turn it is on the GUI
		playerTurnLabel.setTextFill(Color.DARKORANGE);
		playerTurnLabel.setFont(Font.font("Impact", 23));
		playerTurnLabel.setAlignment(Pos.BOTTOM_RIGHT);
		playerTurnLabel.setTranslateX(294);
		playerTurnLabel.setTranslateY(60);

		GridPane connectFourBoard = new GridPane();
		connectFourBoard.setAlignment(Pos.BOTTOM_CENTER);
		connectFourBoard.setHgap(15);
		connectFourBoard.setVgap(15);
		connectFourBoard.setTranslateY(50);

		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 7; col++) {
				int copyCol = col;
				Circle slot = new Circle(40);
				slot.setFill(Color.LAVENDERBLUSH);
				connectFourBoard.add(slot, col, row); // adding a slot (circle) to the GUI grid at the specified (col, row) coordinate

				boardSlots[row][col] = slot; // storing the reference of the current slot to the 2D array game model

				slot.setOnMouseClicked(event -> handleColClick(copyCol)); // every cell in the column that is clicked reacts to the click
			}
		}
		VBox vBoxForGameScreen = new VBox(10, connectFourBoard, playerTurnLabel);
		vBoxForGameScreen.setStyle("-fx-background-color: cornflowerblue;");
		return new Scene(vBoxForGameScreen, 700, 750);
	}

	private void handleColClick(int col) {
		if (isCurrPlayerTurn == true) {
			// sending the message to the server of the column number where the player wants to place their piece
			clientThread.sendMessage(new Message(MessageType.PIECE_MOVE, col));
			isCurrPlayerTurn = false;

		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	private Client clientThread;
	private void connectToServer() {
		String rawUsername = usernameField.getText(); // getting the text from the username TextField
		String username = rawUsername.trim(); // removing any leading or trailing whitespace
		// checking if the username is not blank and that the clientThread is not already running
		if ((!username.isEmpty()) &&  (clientThread == null)) {
			clientThread = new Client(this); // creating a new instance of Client that will connect to the server
			clientThread.start(); // starting the clientThread (calling run() on it)
		}
		else { // allowing the user to enter a new username after their previous one was invalid
			clientThread.sendMessage(new Message(MessageType.SENT_USERNAME, username));
		}
	}

	// method is called whenever a message is obtained from the server
	public void processServerMessage(shared.Message message) {
		shared.MessageType messageType = message.getType();

		switch(messageType) {

			case GAME_STARTED:
				Platform.runLater(() -> {
						Scene gameScene = organizeGameScene(); // creating the game scene which sets up the Connect Four game on the GUI
						primaryStage.setScene(gameScene); // showing the scene
						mySymbol = message.getPlayerSymbol();
						// the player with the 'R' symbol always goes first
						if (mySymbol == 'R') {
							isCurrPlayerTurn = true;
						}
						else {
							isCurrPlayerTurn = false;
						}

						if (isCurrPlayerTurn == true) {
							playerTurnLabel.setText("YOUR TURN");
						}
						else {
							playerTurnLabel.setText("WAIT TURN");
						}
				});
				break;

			case REQUEST_USERNAME:
				String rawUsername = usernameField.getText();
				String username = rawUsername.trim();
				if (username.isEmpty() == false) {
					clientThread.sendMessage(new Message(MessageType.SENT_USERNAME, username));
				}
				else {
					System.out.println("Username cannot be blank.");
				}
				break;

			case USERNAME_TAKEN:
				Platform.runLater(() -> { // running this code on the JavaFX thread
					Alert duplicateUsername = new Alert(Alert.AlertType.ERROR);
					duplicateUsername.setContentText(message.getMessageData()); // getting the warning message from the server-side code
					duplicateUsername.showAndWait();
					usernameField.clear();
				});
				break;

			case PIECE_MOVE:
				int row = message.getRow();
				int col = message.getCol();
				char playerSymbol = message.getPlayerSymbol();

				System.out.println("Received PIECE_MOVE: (" + row + ", " + col + ") by " + playerSymbol);

				Platform.runLater(() -> {
					Color pieceColor;
					if (playerSymbol == 'R') { // if the player that made the move is of symbol 'R', then the color of the piece is red
						pieceColor = Color.RED;
					}
					else {
						pieceColor = Color.YELLOW; // if the player that made the move is of symbol 'Y', then the color of the piece is yellow
					}

					boardSlots[row][col].setFill(pieceColor); // filling in the slot at (row, col) with the corresponding color (i.e. placing a piece)

					isCurrPlayerTurn = (playerSymbol != mySymbol);

					if (isCurrPlayerTurn == true) {
						playerTurnLabel.setText("YOUR TURN");
					}
					else {
						playerTurnLabel.setText("WAIT TURN");
					}
				});
				break;

			case WON:
				List<int[]> winningCoordinates = message.getWinningCoordinates(); // obtaining the coordinates of each piece in the winnning sequence
				highlightWinningPieces(winningCoordinates); // highlighting that sequence
				playerTurnLabel.setText("YOU WON!");
				playerTurnLabel.setTextFill(Color.GREEN);
				playerTurnLabel.setFont(Font.font("Impact", 27));
				break;

			case LOST:
				playerTurnLabel.setText("YOU LOST");
				playerTurnLabel.setTextFill(Color.MEDIUMVIOLETRED);
				playerTurnLabel.setFont(Font.font("Impact", 27));
				break;

			case DREW:
				playerTurnLabel.setText("YOU DREW");
				playerTurnLabel.setTextFill(Color.SILVER);
				playerTurnLabel.setFont(Font.font("Impact", 27));
				break;
		}

	}


	// method to fill in the winning combination on the GUI a different color to make it more noticeable
	private void highlightWinningPieces(List<int[]> winningCoordinates) {

		// looping through each coordinate-pair in the array
		for (int[] currCord : winningCoordinates) {
			int row = currCord[0];
			int col = currCord[1];

			Circle piece = boardSlots[row][col]; // accessing the piece at this coordinate pair

			piece.setFill(Color.LIMEGREEN); // highlighting the piece green
		}
	}
}



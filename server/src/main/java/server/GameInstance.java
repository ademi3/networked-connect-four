package server;
import server.Server.ClientThread;
import shared.Message;
import shared.MessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameInstance {

    private ClientThread player1;
    private ClientThread player2;
    private ClientThread currPlayer;
    private char[][] gameBoard = new char[6][7];
    private boolean isGameDone = false;

    public GameInstance(ClientThread player1, ClientThread player2) {
        this.player1 = player1;
        this.player2 = player2;

        // linking each player to this game instance
        player1.setGameInstance(this, 'R'); // player1 is red
        player2.setGameInstance(this, 'Y'); // player2 is yellow

        this.currPlayer = player1;

        resetBoard();
        player1.sendMessage(new Message(MessageType.GAME_STARTED, 'R')); // sending a GAME_STARTED message to each player with their respective symbol
        player2.sendMessage(new Message(MessageType.GAME_STARTED, 'Y'));
    }


    private void resetBoard() {
        for (int i = 0; i < gameBoard.length; i++) {
            Arrays.fill(gameBoard[i], '@');
        }
    }

    // this method locates the lowest empty slot (row) in the specified column of the board where a player can drop their piece
    private int getOpenRowInCol(int col) {
        for (int row = gameBoard.length - 1; row >= 0; row--) {
            char slot = gameBoard[row][col];
            if (slot == '@') {
                return row;
            }
        }
        return -99; // if an empty slot is not found, then -99 is returned to indicate the column is full
    }

    // this method checks if there are no empty slots left on the board
    private boolean isBoardFull() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (gameBoard[row][col] == '@') { // checking if a '@' symbol is found
                    return false; // if it is, then there is an empty slot(s), so the board is not full
                }
            }
        }
        return true; // if the loop ends without finding a '@' symbol, then the board is full, so true is returned
    }


    // this method checks if the move that is sent is valid or invalid; it also checks if it ends the game
    public void interpretMove(ClientThread moveMaker, int col) {
        if (isGameDone == true) { // handling the case where a player tries to make a move when the game is already over
            moveMaker.sendMessage(new Message(MessageType.INVALID_MOVE, "The game is over."));
            return;
        }
        if (moveMaker != currPlayer) { // handling the case where a player tries to make a move when it is not their turn
            moveMaker.sendMessage(new Message(MessageType.INVALID_MOVE, "This is not your turn."));
            return;
        }

        int row = getOpenRowInCol(col);
        if (row == -99) { // if this condition is true, then the column is full, since -99 is an indicator for a full column
            moveMaker.sendMessage(new Message(MessageType.INVALID_MOVE, "This column has no empty slots."));
            return;
        }
        gameBoard[row][col] = moveMaker.getPlayerSymbol(); // placing the current player's color symbol at the provided position on the board
        char playerSymbol = moveMaker.getPlayerSymbol();
        // sending a message to both players that describes the move that was made
        notifyPlayers(new Message(MessageType.PIECE_MOVE, playerSymbol, row, col));

        List<int[]> winningCoordinates = isWin(playerSymbol, row, col);
        if (winningCoordinates != null) {  // branch executes if the move results in a win
            moveMaker.sendMessage(new Message(MessageType.WON, winningCoordinates)); // sending victory message to the player that made the move
            playerOpponent(moveMaker).sendMessage(new Message(MessageType.LOST)); // informing the other player that they lost
            isGameDone = true;
        }
        else if (isBoardFull() == true) { // branch executes if there are no more empty slots left
            notifyPlayers(new Message(MessageType.DREW)); // notifying both players that the game ended in a draw
            isGameDone = true;
        }

        else { // branch executes if the game is not done and the move is valid
            currPlayer = playerOpponent(moveMaker); // now it's the other player's turn
        }
    }

    // this method checks if there is a horizontal win at the most recently placed piece
    private List<int[]> checkHorizontalWin(char playerSymbol, int row, int col) {
        List<int[]> winningCoordinates = new ArrayList<>();
        winningCoordinates.add(new int[] {row, col}); // adding the current piece to the list

        int right = col + 1;
        // continuing to check the slots to the right as long as the current piece has the same color as the player's symbol and is within bounds
        while ((right < 7) && (gameBoard[row][right] == playerSymbol)) {
            winningCoordinates.add(new int[] {row, right});
            right++;
        }
        int left = col - 1;
        // continuing to check the slots to the left as long as the current piece has the same color as the player's symbol and is within bounds
        while ((left >= 0) && (gameBoard[row][left] == playerSymbol)) {
            winningCoordinates.add(new int[] {row, left});
            left--;
        }
        // if there is a sequence of at least 4 matching pieces in either direction, then the respective player has won
        if (winningCoordinates.size() >= 4) {
            return winningCoordinates;
        }
        else { // if not, then the game continues
            return null;
        }
    }

    // this method checks if there is a vertical win at the most recently placed piece
    private List<int[]> checkVerticalWin(char playerSymbol, int row, int col) {
        List<int[]> winningCoordinates = new ArrayList<>();
        winningCoordinates.add(new int[] {row, col}); // adding the current piece to the list

        // checking the slots downward of the current piece
        int currRow = row + 1;
        // continuing to check the downward slots as long as the current piece has the same color as the player's symbol and is within bounds
        while ((currRow < 6) && (gameBoard[currRow][col] == playerSymbol)) {
            winningCoordinates.add(new int[] {currRow, col});
            currRow++;
        }

        // checking the slots upward of the current piece
        currRow = row - 1;
        // continuing to check the upward slots as long as the current piece has the same color as the player's symbol and is within bounds
        while ((currRow >= 0) && (gameBoard[currRow][col] == playerSymbol)) {
            winningCoordinates.add(new int[] {currRow, col});
            currRow--;
        }
        // if there is a sequence of at least 4 matching pieces in either direction, then the respective player has won
        if (winningCoordinates.size() >= 4) {
            return winningCoordinates;
        }
        else { // if not, then the game continues
            return null;
        }
    }

    // this method checks if there is a diagonal left win at the most recently placed piece
    private List<int[]> checkDiagonalLeftWin(char playerSymbol, int row, int col) {
        List<int[]> winningCoordinates = new ArrayList<>();
        winningCoordinates.add(new int[] {row, col}); // adding the current piece to the list

        // checking the slots that are downward and to the left
        int currRow = row + 1;
        int currCol = col - 1;

        while ((currRow < 6 && currCol >= 0) && (gameBoard[currRow][currCol] == playerSymbol)) {
            winningCoordinates.add(new int[] {currRow, currCol});
            currRow++;
            currCol--;
        }

        // checking the slots that are upward and to the right
        currRow = row - 1;
        currCol = col + 1;

        while ((currRow >= 0 && currCol < 7) && (gameBoard[currRow][currCol] == playerSymbol)) {
            winningCoordinates.add(new int[] {currRow, currCol});
            currRow--;
            currCol++;
        }
        // if there is a sequence of at least 4 matching pieces in either direction, then the respective player has won
        if (winningCoordinates.size() >= 4) {
            return winningCoordinates;
        }
        else { // if not, then the game continues
            return null;
        }
    }

    // this method checks if there is a diagonal right win at the most recently placed piece
    private List<int[]> checkDiagonalRightWin(char playerSymbol, int row, int col) {
        List<int[]> winningCoordinates = new ArrayList<>();
        winningCoordinates.add(new int[] {row, col}); // adding the current piece to the list

        // checking the slots that are downward and to the right
        int currRow = row + 1;
        int currCol = col + 1;

        while ((currRow < 6 && currCol < 7) && (gameBoard[currRow][currCol] == playerSymbol)) {
            winningCoordinates.add(new int[] {currRow, currCol});
            currRow++;
            currCol++;
        }

        // checking the slots that are upward and to the left
        currRow = row - 1;
        currCol = col - 1;

        while ((currRow >= 0 && currCol >= 0) && (gameBoard[currRow][currCol] == playerSymbol)) {
            winningCoordinates.add(new int[] {currRow, currCol});
            currRow--;
            currCol--;
        }

        // if there is a sequence of at least 4 matching pieces in either direction, then the respective player has won
        if (winningCoordinates.size() >= 4) {
            return winningCoordinates;
        }
        else { // if not, then the game continues
            return null;
        }
    }

    // method that checks if the most recently made move is a winning move; if it is, returns the coordinates of all the pieces in that winning sequence
    private List<int[]> isWin(char playerSymbol, int row, int col) {
        List<int[]> winningCoordinates;

        winningCoordinates = checkHorizontalWin(playerSymbol, row, col);
        if (winningCoordinates != null) {
            return winningCoordinates;
        }
        winningCoordinates = checkVerticalWin(playerSymbol, row, col);
        if (winningCoordinates != null) {
            return winningCoordinates;
        }
        winningCoordinates = checkDiagonalLeftWin(playerSymbol, row, col);
        if (winningCoordinates != null) {
            return winningCoordinates;
        }
        winningCoordinates = checkDiagonalRightWin(playerSymbol, row, col);
        if (winningCoordinates != null) {
            return winningCoordinates;
        }
        return null;
    }

    // this method returns the opponent of the player that is passed into the function
    private ClientThread playerOpponent(ClientThread currentP) {
        if (currentP == player1) {  // if the player is player1, then the opponent is player2
            return player2;
        }
        else { // if the player is player2, then the opponent is player1
            return player1;
        }
    }

    // this method sends the same message to both players in the current game instance
    private void notifyPlayers(Message message) {
        player1.sendMessage(message); // the message is sent to the first player
        player2.sendMessage(message);  // the message is sent to the second player
    }

    // this method informs the remaining player that their opponent left the game
    public void processDisconnect(ClientThread playerDisconnecting) {
        // sending a message of type USER_LEAVE to the player that is still in the game via their sendMessage() method
        playerOpponent(playerDisconnecting).sendMessage(new Message(MessageType.USER_LEAVE, "Your opponent left the game."));
    }

    // this method handles sending a chat message from the passed-in player to their opponent
    public void updateChat(ClientThread chatSender, String chatMessage) {
        // sending a chat message from the chatSender to the opponent by creating a SEND_CHAT-type message and delivering it
        // using the opponent's sendMessage() method
        playerOpponent(chatSender).sendMessage(new Message(MessageType.SEND_CHAT, chatMessage));
    }

    // this method informs the opponent that the other player (rematchInitiator) wants to play another game
    public void processRematchProposal(ClientThread rematchInitiator) {
        playerOpponent(rematchInitiator).sendMessage(new Message(MessageType.REMATCH_PROPOSAL));
    }

    // this method interprets the player's rematch decision
    // either a new game is started if the player accepts, or a message is sent out to both players informing of the rejection
    public void processRematchResponse(ClientThread rematchResponder, boolean hasAcceptedRematch) {

        if (hasAcceptedRematch == true) { // handling case where the player accepts the rematch
            notifyPlayers(new Message(MessageType.REMATCH_RESPONSE, true));
            GameInstance newGame = new GameInstance(player1, player2); // a new game is created between those players

            // linking the new GameInstance to both players
            player1.setGameInstance(newGame, 'R');
            player2.setGameInstance(newGame, 'Y');
        }
        else { // handling case where the player rejects the rematch
            notifyPlayers(new Message(MessageType.REMATCH_RESPONSE, false));
        }
    }
}

package shared;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    private MessageType messageType;
    private String messageData;
    private char playerSymbol;
    private int row;
    private int col;
    private boolean acceptedRematch;
    List<int[]> winningCoordinates;

    public MessageType getType() {
        return messageType;
    }

    public String getMessageData() {
        return messageData;
    }

    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }

    public char getPlayerSymbol() {
        return playerSymbol;
    }

    public boolean hasAcceptedRematch() {
        return acceptedRematch;
    }

    public List<int[]> getWinningCoordinates() {
        return winningCoordinates;
    }

    public Message(MessageType messageType, String messageData) {
        this.messageType = messageType;
        this.messageData = messageData;
    }

    // constructor specifically for the PIECE_MOVE command
    public Message(MessageType messageType, char playerSymbol, int row, int col) {
        this.messageType = messageType;
        this.playerSymbol = playerSymbol;
        this.row = row;
        this.col = col;
    }

    // constructor specifically for the REMATCH_RESPONSE command
    public Message(MessageType messageType, boolean acceptedRematch) {
        this.messageType = messageType;
        this.acceptedRematch = acceptedRematch;
    }

    public Message(MessageType messageType, char playerSymbol) {
        this.messageType = messageType;
        this.playerSymbol = playerSymbol;
    }

    // constructor specifically for the REQUEST_USERNAME command
    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public Message(MessageType messageType, int col) {
        this.messageType = messageType;
        this.col = col;
    }

    public Message(MessageType messageType, List<int[]> winningCoordinates) {
        this.messageType = messageType;
        this.winningCoordinates = winningCoordinates;
    }



}


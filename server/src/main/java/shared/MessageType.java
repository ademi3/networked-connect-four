package shared;

// enum that defines the constants that represent the different types of messages that can be sent between players and the server during a game
public enum MessageType {
    GAME_STARTED,
    WON,
    LOST,
    DREW,
    PIECE_MOVE,
    INVALID_MOVE,
    SEND_CHAT,
    REMATCH_PROPOSAL,
    REMATCH_RESPONSE,
    USER_LEAVE,
    REQUEST_USERNAME,
    SENT_USERNAME,
    USERNAME_TAKEN;
}

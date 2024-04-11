package server.websocket;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;
import dataAccess.MySQLDataAccess;
import exception.ResponseException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.Server;
import webSocketMessages.userCommands.UserGameCommand;
import webSocketMessages.serverMessages.ServerMessage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    {
        try {
            DataAccess dataAccess = new MySQLDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case JOIN_PLAYER -> joinPlayer(action.getUsername(), session, action.getGameID());
            case JOIN_OBSERVER -> joinObserver(action.getUsername(), session, action.getGameID());
//            case EXIT -> exit(action.visitorName());
        }
    }

    private void joinPlayer(String visitorName, Session session, int gameID) throws IOException, DataAccessException {
        connections.add(visitorName, session);
        GameData gameToReturn = null;
        String playerColor = null;
        String query = "SELECT * FROM game WHERE gameID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setInt(1, gameID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // Game already exists
                    int gameIDToReturn = resultSet.getInt("gameID");
                    String whiteUser = resultSet.getString("whiteUsername");
                    String blackUser = resultSet.getString("blackUsername");
                    String gameName = resultSet.getString("gameName");
                    String chessGameString = resultSet.getString("game");
                    Gson gson = new Gson();
                    ChessGame chessGame = gson.fromJson(chessGameString, ChessGame.class);
                    gameToReturn = new GameData(gameIDToReturn, whiteUser, blackUser, gameName, chessGame);
                    if (visitorName.equals(whiteUser)){
                        playerColor = "white";
                    } else {
                        playerColor = "black";
                    }
                } else {
                    // Game doesn't exist
                    GameData error = new GameData(400, "", "", "", new ChessGame());
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        ChessGame game = gameToReturn.getGame();
        ChessBoard board = game.getBoard();
        String gameMessage = null;
        if (playerColor == "white"){
            gameMessage = makeWhiteBoard(board);
        } else {
            gameMessage = makeBlackBoard(board);
        }
        var gameServerMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameMessage);
        connections.broadcastToSender(visitorName, gameServerMessage);
        var message = String.format("%s just joined the game as a player.", visitorName);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, serverMessage);
    }

    private void joinObserver(String visitorName, Session session, int gameID) throws IOException, DataAccessException {
        connections.add(visitorName, session);
        GameData gameToReturn = null;
        String playerColor = "white";
        String query = "SELECT * FROM game WHERE gameID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setInt(1, gameID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // Game already exists
                    int gameIDToReturn = resultSet.getInt("gameID");
                    String whiteUser = resultSet.getString("whiteUsername");
                    String blackUser = resultSet.getString("blackUsername");
                    String gameName = resultSet.getString("gameName");
                    String chessGameString = resultSet.getString("game");
                    Gson gson = new Gson();
                    ChessGame chessGame = gson.fromJson(chessGameString, ChessGame.class);
                    gameToReturn = new GameData(gameIDToReturn, whiteUser, blackUser, gameName, chessGame);
                } else {
                    // Game doesn't exist
                    GameData error = new GameData(400, "", "", "", new ChessGame());
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        ChessGame game = gameToReturn.getGame();
        ChessBoard board = game.getBoard();
        String gameMessage = makeWhiteBoard(board);
        var gameServerMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameMessage);
        connections.broadcastToSender(visitorName, gameServerMessage);
        var message = String.format("%s just joined the game as an observer.", visitorName);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, serverMessage);
    }

    public String makeWhiteBoard(ChessBoard board){
        StringBuilder sb = new StringBuilder();
        sb.append("    1   2   3   4   5   6   7   8\n");
        sb.append("  --------------------------------\n");
        for (int i = 8; i >= 1; i--) {
            sb.append(i);
            sb.append(" | ");
            for (int j = 1; j <= 8; j++) {
                // Append the current cell's value to the string
                ChessPosition pos = new ChessPosition(i, j);
                if (board.getPiece(pos) == null){
                    sb.append(" ");
                    sb.append(" | ");
                    continue;
                }
                ChessPiece piece = board.getPiece(pos);
                if (piece.pieceType == ChessPiece.PieceType.BISHOP && piece.teamColor == ChessGame.TeamColor.WHITE){
                    sb.append("B");
                } else if (piece.pieceType == ChessPiece.PieceType.KING && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("K");
                } else if (piece.pieceType == ChessPiece.PieceType.QUEEN && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("Q");
                } else if (piece.pieceType == ChessPiece.PieceType.ROOK && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("R");
                } else if (piece.pieceType == ChessPiece.PieceType.KNIGHT && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("N");
                } else if (piece.pieceType == ChessPiece.PieceType.PAWN && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("P");
                } else if (piece.pieceType == ChessPiece.PieceType.BISHOP && piece.teamColor == ChessGame.TeamColor.BLACK){
                    sb.append("b");
                } else if (piece.pieceType == ChessPiece.PieceType.KING && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("k");
                } else if (piece.pieceType == ChessPiece.PieceType.QUEEN && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("q");
                } else if (piece.pieceType == ChessPiece.PieceType.ROOK && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("r");
                } else if (piece.pieceType == ChessPiece.PieceType.KNIGHT && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("n");
                } else if (piece.pieceType == ChessPiece.PieceType.PAWN && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("p");
                } else if (piece.pieceType == null && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("X");
                }
                sb.append(" | ");
            }
            // Add a newline character after each row
            sb.append(i);
            sb.append("\n");
        }
        sb.append("  --------------------------------\n");
        sb.append("    1   2   3   4   5   6   7   8\n");
        return sb.toString();
    }

    public String makeBlackBoard(ChessBoard board){
        StringBuilder sb = new StringBuilder();
        sb.append("    8   7   6   5   4   3   2   1\n");
        sb.append("  --------------------------------\n");
        for (int i = 1; i <= 8; i++) {
            sb.append(i);
            sb.append(" | ");
            for (int j = 8; j >= 1; j--) {
                // Append the current cell's value to the string
                ChessPosition pos = new ChessPosition(i, j);
                if (board.getPiece(pos) == null){
                    sb.append(" ");
                    sb.append(" | ");
                    continue;
                }
                ChessPiece piece = board.getPiece(pos);
                if (piece.pieceType == ChessPiece.PieceType.BISHOP && piece.teamColor == ChessGame.TeamColor.WHITE){
                    sb.append("B");
                } else if (piece.pieceType == ChessPiece.PieceType.KING && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("K");
                } else if (piece.pieceType == ChessPiece.PieceType.QUEEN && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("Q");
                } else if (piece.pieceType == ChessPiece.PieceType.ROOK && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("R");
                } else if (piece.pieceType == ChessPiece.PieceType.KNIGHT && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("N");
                } else if (piece.pieceType == ChessPiece.PieceType.PAWN && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("P");
                } else if (piece.pieceType == ChessPiece.PieceType.BISHOP && piece.teamColor == ChessGame.TeamColor.BLACK){
                    sb.append("b");
                } else if (piece.pieceType == ChessPiece.PieceType.KING && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("k");
                } else if (piece.pieceType == ChessPiece.PieceType.QUEEN && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("q");
                } else if (piece.pieceType == ChessPiece.PieceType.ROOK && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("r");
                } else if (piece.pieceType == ChessPiece.PieceType.KNIGHT && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("n");
                } else if (piece.pieceType == ChessPiece.PieceType.PAWN && piece.teamColor == ChessGame.TeamColor.BLACK) {
                    sb.append("p");
                } else if (piece.pieceType == null && piece.teamColor == ChessGame.TeamColor.WHITE) {
                    sb.append("X");
                }
                sb.append(" | ");
            }
            // Add a newline character after each row
            sb.append(i);
            sb.append("\n");
        }
        sb.append("  --------------------------------\n");
        sb.append("    8   7   6   5   4   3   2   1\n");
        return sb.toString();
    }

//    private void exit(String visitorName) throws IOException {
//        connections.remove(visitorName);
//        var message = String.format("%s left the shop", visitorName);
//        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType, message);
//        connections.broadcast(visitorName, serverMessage);
//    }
//
//    public void makeNoise(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var serverMessage = new ServerMessage(ServerMessage.Type.NOISE, message);
//            connections.broadcast("", serverMessage);
//        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
}
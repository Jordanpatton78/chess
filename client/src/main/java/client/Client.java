package client;

import chess.*;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerFacade;
import client.State;
import webSocketMessages.serverMessages.ServerMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class Client {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    private String authToken;

    private String currUser;

    private GameData currGame;

    public String playerColor;

    private int gameID;

    private String serverUrl;

    public NotificationHandler notificationHandler;

    public WebSocketFacade webSocketFacade;

    public Client(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = new NotificationHandler() {
            @Override
            public void notify(ServerMessage serverMessage){
                try{
                    String message = serverMessage.getMessage();
                    if (determineMessageType(message) == ServerMessage.ServerMessageType.LOAD_GAME){
                        if (playerColor.equals(determineOrientation(message))){
                            System.out.println(message);
                        } else{
                            message = flipChessboard(message);
                            System.out.println(message);
                        }
                    }else {
                        System.out.println(message);
                    }
                } catch (ResponseException e){

                }
            }
        };

        {
            try{
                this.webSocketFacade = new WebSocketFacade(serverUrl, notificationHandler);
            } catch (ResponseException e ){

            }
        }
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - Register <USERNAME> <PASSWORD> <EMAIL>
                    - Login <USERNAME> <PASSWORD>
                    - Help
                    - Quit
                    """;
        } else if (state == State.JOINED || state == State.COMPLETED || state == State.OBSERVING){
            return """
                    - Move <Start Location> <End Location>
                    - Highlight (Legal Moves) <Piece>
                    - Redraw
                    - Leave
                    - Resign
                    - Help
                    """;
        }
        return """
                - Logout
                - Create <NAME>
                - List
                - Join <GameID> <PlayerColor>
                - Observe <GameID>
                - Help
                - Quit
                """;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "help" -> help();
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                case "highlight" -> highlightMoves(params);
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> move(params);
                case "resign" -> resign();
                case "yes" -> resignConfirm();
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException{
        if (params.length >= 3) {
            UserData user = new UserData(params[0], params[1], params[2]);
            AuthData auth = server.register(user);
            this.authToken = auth.getAuthToken();
            this.currUser = user.getUsername();
            state = State.SIGNEDIN;
            return String.format("You signed in as %s.", user.getUsername());
        } else {
            throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
        }
    }

    public String login(String... params) throws ResponseException{
        if (params.length >= 2) {
            UserData user = new UserData(params[0], params[1], null);
            AuthData auth = server.login(user);
            this.authToken = auth.getAuthToken();
            this.currUser = user.getUsername();
            state = State.SIGNEDIN;
            return String.format("You logged in as %s.", user.getUsername());
        } else {
            throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD>");
        }
    }

    public String logout() throws ResponseException{
        AuthData auth = new AuthData(this.authToken, this.currUser);
        server.logout(auth);
        this.currUser = null;
        this.authToken = null;
        state = State.SIGNEDOUT;
        return String.format("You logged out.");
    }

    public String createGame(String... params) throws ResponseException{
        if (params.length >= 1) {
            AuthData auth = new AuthData(this.authToken, this.currUser);
            int gameNumber = server.listGames(auth).size();
            String gameName = params[0];
            ChessBoard board = new ChessBoard();
            ChessGame game = new ChessGame();
            GameData gameData = new GameData(gameNumber+1, null, null, gameName, game);
            gameData = server.createGame(auth, gameData);
            return String.format("You created the game: %s", gameData.getGameName());
        } else {
            throw new ResponseException(400, "Expected: <NAME>");
        }
    }

    public String listGames() throws ResponseException {
        StringBuilder gameInfo = new StringBuilder();
        AuthData auth = new AuthData(this.authToken, this.currUser);
        ArrayList<GameData> games = server.listGames(auth);
        for (GameData game : games) {
            gameInfo.append("Game ID: ").append(game.getGameID()).append("\n");
            gameInfo.append("Game Name: ").append(game.getGameName()).append("\n");
            gameInfo.append("White Username: ").append(game.getWhiteUsername()).append("\n");
            gameInfo.append("Black Username: ").append(game.getBlackUsername()).append("\n\n");
        }
        return gameInfo.toString();
    }

    public String joinGame(String... params) throws ResponseException{
        if (params.length >= 2) {
            AuthData auth = new AuthData(this.authToken, this.currUser);
            int gameID = Integer.parseInt(params[0]);
            String playerColor = params[1];
            this.playerColor = playerColor;
            GameData gameData = new GameData(gameID, null, null, null, null);
            gameData = server.joinGame(auth, gameData, playerColor);
            ChessGame chessGame = gameData.getGame();
            ChessBoard board = chessGame.getBoard();
            if (board == null){
                board = new ChessBoard();
                board.resetBoard();
            }
            StringBuilder games = new StringBuilder();
            this.currGame = gameData;
            state = State.JOINED;
            this.webSocketFacade.joinGame(this.authToken, this.currUser, gameID);
            return games.toString();
        } else {
            throw new ResponseException(400, "Expected: <GameID> <playerColor>");
        }
    }

    public String observeGame(String... params) throws ResponseException{
        if (params.length >= 1) {
            AuthData auth = new AuthData(this.authToken, this.currUser);
            int gameID = Integer.parseInt(params[0]);
            GameData gameData = new GameData(gameID, null, null, null, null);
            gameData = server.joinObserver(auth, gameData, null);
            ChessGame chessGame = gameData.getGame();
            ChessBoard board = chessGame.getBoard();
            if (board == null){
                board = new ChessBoard();
                board.resetBoard();
            }
            StringBuilder games = new StringBuilder();
            this.currGame = gameData;
            state = State.OBSERVING;
            this.webSocketFacade.observeGame(this.authToken, this.currUser, gameID);
            this.playerColor = "white";
            return games.toString();
        } else {
            throw new ResponseException(400, "Expected: <GameID>");
        }
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

    public static String flipChessboard(String board) {
        // Split the board into rows
        String[] rows = board.split("\n");

        // Initialize a StringBuilder to store the flipped board
        StringBuilder flippedBoard = new StringBuilder();

        for (int i = rows.length-1; i >=0; i --){
            rows[i] = flipString(rows[i]);
            if (i == 0 || i == 1 || i == rows.length-2 || i == rows.length-1){
                rows[i] = "    " + rows[i];
            }
            flippedBoard.append(rows[i]);
            flippedBoard.append("\n");
        }
        return flippedBoard.toString();
    }

    public static String flipString(String originalString) {
        // Split the original string by spaces
        String[] numbers = originalString.split(" ");

        // Initialize a StringBuilder to store the flipped string
        StringBuilder flippedStringBuilder = new StringBuilder();

        // Iterate over the numbers in reverse order and append them to the StringBuilder
        for (int i = numbers.length - 1; i >= 0; i--) {
            flippedStringBuilder.append(numbers[i]);
            if (i > 0) {
                flippedStringBuilder.append(" "); // Append a space between numbers, except for the last one
            }
        }

        // Convert the StringBuilder to a string and return
        return flippedStringBuilder.toString();
    }

    public String determineOrientation(String chessboard) throws ResponseException{
        // Split the chessboard string into lines
        String chessboardCopy = new String(chessboard);
        String[] lines = chessboardCopy.trim().split("\n");

        // Check if the first line starts with "8 |"
        if (lines[0].startsWith("8")) {
            return "black";
        } else {
            return "white";
        }
    }

    public static ServerMessage.ServerMessageType determineMessageType(String message) {
        // Split the chessboard string into lines
        String messageCopy = new String(message);
        String[] lines = messageCopy.trim().split("\n");

        // Check if the first line starts with "8 |"
        if (lines[0].startsWith("8")||lines[0].startsWith("1")) {
            return ServerMessage.ServerMessageType.LOAD_GAME;
        } else {
            return ServerMessage.ServerMessageType.NOTIFICATION;
        }
    }

    public String highlightMoves(String ... params) throws ResponseException{
        if (state != State.COMPLETED){
            this.currGame = server.getGame(new AuthData(this.authToken, this.currUser), this.currGame);
            if (params.length >= 1) {
                String pieceString = params[0];
                String[] pieceStr = pieceString.split(",");
                int row = Integer.parseInt(pieceStr[0]);
                int col = Integer.parseInt(pieceStr[1]);
                ChessGame game = currGame.getGame();
                if (game.getBoard() == null){
                    ChessBoard board = new ChessBoard();
                    board.resetBoard();
                    game.setBoard(board);
                }
                ChessBoard board = game.getBoard();
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                StringBuilder moves = new StringBuilder();
                if (piece == null){
                    moves.append("Null piece.");
                    return moves.toString();
                }
                HashSet<ChessMove> validMoves = piece.pieceMoves(board, pos);
                for (ChessMove move : validMoves){
                    ChessPosition end = move.getEndPosition();
                    int endRow = end.getRow();
                    int endCol = end.getColumn();
                    board.squares[endRow-1][endCol-1] = new ChessPiece(ChessGame.TeamColor.WHITE, null);
                }
                if (this.playerColor.equals("white")){
                    moves.append(makeWhiteBoard(board));
                } else if (this.playerColor.equals("black")){
                    moves.append(makeBlackBoard(board));
                }
                for (int i=0; i<board.squares.length;i++){
                    for (int j=0; j<board.squares[i].length;j++){
                        ChessPosition chessPosition = new ChessPosition(i+1,j+1);
                        ChessPiece chessPiece = board.getPiece(chessPosition);
                        if (chessPiece != null){
                            if (chessPiece.teamColor == ChessGame.TeamColor.WHITE && chessPiece.pieceType == null){
                                board.removePiece(chessPosition);
                            }
                        }
                    }
                }
                return moves.toString();
            } else {
                throw new ResponseException(400, "Expected: <Piece>");
            }
        } else {
            return "The game is over.";
        }

    }

    public String redraw()throws ResponseException {
        if (state != State.COMPLETED){
            this.currGame = server.getGame(new AuthData(this.authToken, this.currUser), this.currGame);
            ChessGame game = currGame.getGame();
            ChessBoard board = game.getBoard();
            StringBuilder sb = new StringBuilder();
            if (this.playerColor.equals("white")){
                sb.append(makeWhiteBoard(board));
            } else {
                sb.append(makeBlackBoard(board));
            }
            return sb.toString();
        } else {
            return "The game is over.";
        }
    }

    public String leave() throws ResponseException{
        state = State.SIGNEDIN;
        GameData game = null;
        if (this.playerColor.equals("white")){
            game = new GameData(currGame.getGameID(), null, currGame.getBlackUsername(), currGame.getGameName(), currGame.getGame());
        } else {
            game = new GameData(currGame.getGameID(), currGame.getWhiteUsername(), null, currGame.getGameName(), currGame.getGame());
        }
        server.leaveGame(new AuthData(this.authToken, this.currUser), game);
        webSocketFacade.leave(this.authToken, this.currUser, gameID);
        this.currGame = null;
        return "You left the game.";
    }
    public String resign() throws ResponseException{
        if (state != State.OBSERVING){
            return "Are you sure you would like to resign? Yes/ no";
        }
        return "You can't resign as an observer.";
    }

    public String resignConfirm() throws ResponseException{
        if (state != State.OBSERVING){
            state = State.COMPLETED;
            webSocketFacade.resign(this.authToken, this.currUser, gameID);
            return "You resigned.";
        }
        return "You can't resign as an observer.";
    }

    public String move(String... params) throws ResponseException, InvalidMoveException {
        if (state != State.COMPLETED && state != State.OBSERVING){
            this.currGame = server.getGame(new AuthData(this.authToken, this.currUser), this.currGame);
            if (params.length >= 1) {
                String startStr = params[0];
                String endStr = params[1];
                int startRow = Integer.parseInt(startStr.split(",")[0]);
                int startCol = Integer.parseInt(startStr.split(",")[1]);
                int endRow = Integer.parseInt(endStr.split(",")[0]);
                int endCol = Integer.parseInt(endStr.split(",")[1]);
                ChessPosition startPos = new ChessPosition(startRow, startCol);
                ChessPosition endPos = new ChessPosition(endRow, endCol);
                ChessMove move = new ChessMove(startPos, endPos, null);
                ChessGame game = currGame.getGame();
                ChessBoard board = game.getBoard();
                ChessPiece piece = board.getPiece(startPos);
                game.makeMove(move);
                GameData newGame = new GameData(currGame.getGameID(), currGame.getWhiteUsername(), currGame.getBlackUsername(), currGame.getGameName(), game);
                this.currGame = server.move(new AuthData(this.authToken, this.currUser), newGame);
                this.webSocketFacade.makeMove(this.authToken, this.currUser, currGame.getGameID());
                isInCheckmate();
                isInCheck();
            } else {
                throw new ResponseException(400, "Expected: <Start Location> <End Location>");
            }
            return "";
        } else {
            if (state == State.COMPLETED){
                return "The game is over.";
            } else {
                return "You can't make moves as an observer.";
            }
        }

    }


    public void isInCheck() throws ResponseException{
        if (state != State.COMPLETED){
            ChessGame chessGame = currGame.getGame();
            if (chessGame.isInCheck(chessGame.getTeamTurn())){
                String loser = null;
                if (playerColor.equals("white")){
                    loser = currGame.getBlackUsername();
                } else {
                    loser = currGame.getWhiteUsername();
                }
                this.webSocketFacade.check(this.authToken, loser, currGame.getGameID());
            }
        }
    }
    public void isInCheckmate() throws ResponseException{
        ChessGame chessGame = currGame.getGame();
        if (chessGame.isInCheckmate(chessGame.getTeamTurn())){
            String loser = null;
            if (playerColor.equals("white")){
                loser = currGame.getBlackUsername();
            } else {
                loser = currGame.getWhiteUsername();
            }
            this.webSocketFacade.checkmate(this.authToken, loser, currGame.getGameID());
            state = State.COMPLETED;
        }
    }

}
package client;

import chess.ChessBoard;
import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerFacade;
import client.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Client {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    private String authToken;

    private String currUser;

    public Client(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - Register <USERNAME> <PASSWORD> <EMAIL>
                    - Login <USERNAME> <PASSWORD>
                    - Quit
                    """;
        }
        return """
                - Logout
                - Create <NAME>
                - List
                - Join <GameID>
                - Observe <GameID>
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
//                case "join" -> joinGame(params);
//                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException{
        if (params.length >= 3) {
            state = State.SIGNEDIN;
            UserData user = new UserData(params[0], params[1], params[2]);
            AuthData auth = server.register(user);
            this.authToken = auth.getAuthToken();
            this.currUser = user.getUsername();
            return String.format("You signed in as %s.", user.getUsername());
        } else {
            throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
        }
    }

    public String login(String... params) throws ResponseException{
        if (params.length >= 2) {
            state = State.SIGNEDIN;
            UserData user = new UserData(params[0], params[1], null);
            AuthData auth = server.login(user);
            this.authToken = auth.getAuthToken();
            this.currUser = user.getUsername();
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



    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }
}

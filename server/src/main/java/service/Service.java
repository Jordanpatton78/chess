package service;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.Random;

public class Service {

    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData addUser(UserData user) throws DataAccessException {
        return dataAccess.addUser(user);
    }

    public UserData getUser(UserData user) throws DataAccessException {
        return dataAccess.getUser(user);
    }

    public AuthData addAuth(UserData user) throws DataAccessException {
        UUID authToken = UUID.randomUUID();
        String strAuthToken = authToken.toString();
        String username = user.getUsername();
        AuthData authData = new AuthData(strAuthToken, username);
        return dataAccess.addAuth(user, authData);
    }

    public AuthData getAuth(AuthData auth) throws DataAccessException{
        return dataAccess.getAuth(auth);
    }

    public AuthData deleteAuth(AuthData auth) throws DataAccessException{
        return dataAccess.deleteAuth(auth);
    }

    public ArrayList<Object> listGames() throws DataAccessException{
        return dataAccess.listGames();
    }

    public GameData createGame(GameData game) throws DataAccessException{
        int gameId = game.getGameID();
        GameData newGame = null;
        ChessGame chessGame = new ChessGame();
        if (gameId == 0){
            Random rand = new Random();
            int randomNumber = rand.nextInt(101);
            newGame = new GameData(randomNumber, game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), chessGame);
        }else{
            newGame = game;
        }
        return dataAccess.createGame(newGame);
    }

    public GameData getGame(GameData game) throws DataAccessException{
        return dataAccess.getGame(game);
    }

    public GameData updateGame(String username, GameData game, String playerColor) throws DataAccessException{
        int gameID = game.getGameID();
        String whiteUsername = game.getWhiteUsername();
        String blackUsername = game.getBlackUsername();
        if (playerColor == null){
            //
        }else if (whiteUsername == null && playerColor.equals("WHITE")){
            whiteUsername = username;
        }else if (blackUsername == null && playerColor.equals("BLACK")){
            blackUsername = username;
        }
        String gameName = game.getGameName();
        ChessGame chessGame = game.getGame();
        GameData newGame = new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
        return dataAccess.updateGame(newGame);
    }

    public GameData leaveGame(GameData game) throws DataAccessException{
        return dataAccess.leaveGame(game);
    }

    public void deleteAll() throws DataAccessException {
        dataAccess.deleteAll();
    }

}
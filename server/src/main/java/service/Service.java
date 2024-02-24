package service;

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
        GameData new_game = null;
        if (gameId == 0){
            Random rand = new Random();
            int randomNumber = rand.nextInt(101);
            new_game = new GameData(randomNumber, game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), game.getGame());
        }else{
            new_game = game;
        }
        return dataAccess.createGame(new_game);
    }

    public GameData getGame(GameData game) throws DataAccessException{
        return dataAccess.getGame(game);
    }

    public GameData updateGame(GameData game) throws DataAccessException{

        return dataAccess.updateGame(game);
    }

    public void deleteAll() throws DataAccessException {
        dataAccess.deleteAll();
    }

}
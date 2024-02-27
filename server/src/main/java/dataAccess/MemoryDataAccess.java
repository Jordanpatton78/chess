package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess{

    static HashMap<String, UserData> userMap = new HashMap<>();
    static HashMap<String, AuthData> authMap = new HashMap<>();

    static HashMap<Integer, GameData> gameMap = new HashMap<>();
    @Override
    public UserData addUser(UserData user) throws DataAccessException {
        String username = user.getUsername();
        String password = user.getPassword();
        String usernameAndPassword = username + password;
        userMap.put(usernameAndPassword, user);
        return userMap.get(usernameAndPassword);
    }

    @Override
    public UserData getUser(UserData user) throws DataAccessException {
        String username = user.getUsername();
        String password = user.getPassword();
        String usernameAndPassword = username + password;
        return userMap.get(usernameAndPassword);
    }

    @Override
    public AuthData addAuth(UserData user, AuthData authData) throws DataAccessException {
        String authToken = authData.getAuthToken();
        authMap.put(authToken, authData);
        return authData;
    }

    @Override
    public AuthData getAuth(AuthData authData) throws DataAccessException{
        String authToken = authData.getAuthToken();
        return authMap.get(authToken);
    }

    @Override
    public AuthData deleteAuth(AuthData auth) throws DataAccessException{
        String authToken = auth.getAuthToken();
        if (authMap.containsKey(authToken)){
            authMap.remove(authToken);
            return auth;
        }
        return null;
    }

    @Override
    public ArrayList<Object> listGames() throws DataAccessException{
        return new ArrayList<>(gameMap.values());
    }

    @Override
    public GameData createGame( GameData game) throws DataAccessException{
        int gameId = game.getGameID();
        gameMap.put(gameId, game);
        return game;
    }

    @Override
    public GameData getGame(GameData game) throws DataAccessException{
        int gameId = game.getGameID();
        GameData gameData = gameMap.get(gameId);
        return gameData;
    }

    @Override
    public GameData updateGame(GameData game) throws DataAccessException{
        int gameId = game.getGameID();
        gameMap.put(gameId, game);
        return game;
    }

    @Override
    public void deleteAll() throws DataAccessException {
        userMap.clear();
        authMap.clear();
        gameMap.clear();
    }
}

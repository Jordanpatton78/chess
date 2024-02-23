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

    static HashMap<String, GameData> gameMap = new HashMap<>();
    @Override
    public UserData addUser(UserData user) throws DataAccessException {
        String username = user.getUsername();
        String password = user.getPassword();
        String username_and_password = username + password;
        userMap.put(username_and_password, user);
        return userMap.get(username_and_password);
    }

    @Override
    public UserData getUser(UserData user) throws DataAccessException {
        String username = user.getUsername();
        String password = user.getPassword();
        String username_and_password = username + password;
        return userMap.get(username_and_password);
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
    public void createGame(AuthData auth) throws DataAccessException{
        ;
    }

    @Override
    public void deleteAll() throws DataAccessException {
        userMap.clear();
        authMap.clear();
        gameMap.clear();
    }
}

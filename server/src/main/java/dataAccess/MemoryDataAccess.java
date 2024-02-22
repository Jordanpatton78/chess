package dataAccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess{

    static HashMap<String, UserData> userMap = new HashMap<>();
    static HashMap<String, AuthData> authMap = new HashMap<>();
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
        String username = user.getUsername();
        authMap.put(username, authData);
        return authData;
    }

    @Override
    public void deleteAll() throws DataAccessException {

    }
}

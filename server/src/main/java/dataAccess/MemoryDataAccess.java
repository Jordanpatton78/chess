package dataAccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{

    static HashMap<String, UserData> userMap = new HashMap<>();
    @Override
    public UserData addUser(UserData user) throws DataAccessException {
        String username = user.getUsername();
        userMap.put(username, user);
        return userMap.get(username);
    }

    @Override
    public UserData getUser(UserData user) throws DataAccessException {
        String username = user.getUsername();
        return userMap.get(username);
    }

    @Override
    public AuthData createAuth(UserData user, AuthData authData) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAll() throws DataAccessException {

    }
}

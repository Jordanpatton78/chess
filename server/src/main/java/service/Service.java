package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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

    public ArrayList<Object> listGames() throws DataAccessException{
        return dataAccess.listGames();
    }

    public void createGame(AuthData auth) throws DataAccessException{
        dataAccess.createGame(auth);
    }

    public void deleteAll() throws DataAccessException {
        dataAccess.deleteAll();
    }

}
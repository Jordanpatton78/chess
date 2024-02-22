package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.Collection;

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

    public AuthData createAuth(UserData user, AuthData authData) throws DataAccessException {
        return dataAccess.createAuth(user, authData);
    }

    public void deleteAll() throws DataAccessException {
        dataAccess.deleteAll();
    }

}
package dataAccess;

import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;

import javax.xml.crypto.Data;
import java.util.Collection;

public class DataAccess {
    public UserData addUser(UserData user) throws DataAccessException{
        return user;
    }

    public UserData getUser(UserData user) throws DataAccessException{
        return user;
    }

    public AuthData createAuth(UserData user, AuthData authData) throws DataAccessException {
        return authData;
    }


    public void deleteAll() throws DataAccessException{
        ;
    }
}
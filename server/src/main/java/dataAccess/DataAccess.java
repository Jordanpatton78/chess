package dataAccess;

import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;

import javax.xml.crypto.Data;
import java.util.Collection;
import java.util.HashMap;

public interface DataAccess {
    public UserData addUser(UserData user) throws DataAccessException;

    public UserData getUser(UserData user) throws DataAccessException;

    public AuthData createAuth(UserData user, AuthData authData) throws DataAccessException;

    public void deleteAll() throws DataAccessException;
}
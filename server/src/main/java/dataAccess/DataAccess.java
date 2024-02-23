package dataAccess;

import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public interface DataAccess {
    public UserData addUser(UserData user) throws DataAccessException;

    public UserData getUser(UserData user) throws DataAccessException;

    public AuthData addAuth(UserData user, AuthData authData) throws DataAccessException;

    public AuthData getAuth(AuthData authData) throws DataAccessException;

    public ArrayList<Object> listGames() throws DataAccessException;

    public void createGame(AuthData authData) throws DataAccessException;

    public void deleteAll() throws DataAccessException;
}
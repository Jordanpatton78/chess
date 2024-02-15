package dataAccess;

import dataAccess.DataAccessException;
import model.User;

import java.util.Collection;

public interface DataAccess {
    User addUser(User user) throws DataAccessException;

    void deleteAll() throws DataAccessException;
}
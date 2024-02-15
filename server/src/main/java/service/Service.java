package service;

import dataAccess.DataAccess;
import model.User;
import dataAccess.DataAccessException;

import java.util.Collection;

public class Service {

    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public User addUser(User user) throws DataAccessException {
        return dataAccess.addUser(user);
    }

    public void deleteAll() throws DataAccessException {
        dataAccess.deleteAll();
    }

}
package dataAccess;

import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySQLDataAccessTest {

    DataAccess dataAccess;
    {
        try {
            dataAccess = new MySQLDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void addUserSuccess() throws DataAccessException {
        UserData user = new UserData("jordan", "patton", "my_email");
        UserData new_user = dataAccess.addUser(user);
        assert new_user.getUsername() == user.getUsername();
    }

    @Test
    void addUserFailure() throws DataAccessException{
        UserData user = new UserData("username", "password", "email");
        UserData user1 = dataAccess.addUser(user); // Adding the same user twice should throw an exception
        UserData user2 = dataAccess.addUser(user); // Attempting to add the same user again
        assert user2.getUsername() == "403";
    }

    @Test
    void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("jordan", "patton", "my_email");
        user = dataAccess.addUser(user);
        UserData new_user = dataAccess.getUser(user);
        assert new_user.getUsername().equals(user.getUsername());
    }

    @Test
    void getUserFailure() throws DataAccessException{
        UserData user = new UserData("username_not_in_db", "password", "email");
        UserData user1 = dataAccess.getUser(user);
        assert user1.getUsername() == "401";
    }

}
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
    void addUserFailure() throws DataAccessException {
        UserData user = new UserData("username", "password", "email");
        UserData new_user = dataAccess.addUser(user);
        UserData another_user = dataAccess.addUser(user);
        assert new_user.getUsername() != another_user.getUsername();
    }
}
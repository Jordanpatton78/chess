package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

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

    @Test
    void addAuthSuccess() throws DataAccessException {
        UserData user = new UserData("jordan", "password", "email");
        AuthData auth = new AuthData("", "jordan");
        AuthData authData = dataAccess.addAuth(user, auth);
        assert authData.getUsername().equals(user.getUsername());
    }

    @Test
    void addAuthFailure() throws DataAccessException{
        UserData user = new UserData("username_not_in_db", "password", "email");
        AuthData auth = new AuthData("", "username_not_in_db");
        AuthData authData = dataAccess.addAuth(user, auth);
        assert authData.getAuthToken().equals("400");
    }

    @Test
    void getAuthSuccess() throws DataAccessException {
        UserData user = new UserData("jordan", "password", "email");
        AuthData auth = new AuthData("", "jordan");
        AuthData authData = dataAccess.addAuth(user, auth);
        AuthData gotAuthData = dataAccess.getAuth(authData);
        assert authData.getAuthToken().equals(gotAuthData.getAuthToken());
    }

    @Test
    void getAuthFailure() throws DataAccessException{
        AuthData auth = new AuthData("auth_not_in_db", "");
        AuthData authData = dataAccess.getAuth(auth);
        assert authData.getAuthToken().equals("400");
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        UserData user = new UserData("jordan", "password", "email");
        AuthData auth = new AuthData("10000000", "username");
        AuthData authData = dataAccess.addAuth(user, auth);
        AuthData deletedAuth = dataAccess.deleteAuth(authData);
        assert authData.getAuthToken().equals(deletedAuth.getAuthToken());
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, "white", "black", "gameName", new ChessGame());
        GameData new_game = dataAccess.createGame(game);
        assert new_game.getGameID() == game.getGameID();
    }

    @Test
    void createGameFailure() throws DataAccessException{
        GameData game = new GameData(1, "white", "black", "gameName", new ChessGame());
        GameData inDatabase = dataAccess.createGame(game);
        assert inDatabase.getGameID() == 403;
    }

    @Test
    void getGameSuccess() throws DataAccessException {
        GameData game = new GameData(2, "white", "black", "gameName", new ChessGame());
        game = dataAccess.createGame(game);
        GameData new_game = dataAccess.getGame(game);
        assert new_game.getGameID() == game.getGameID();
    }

    @Test
    void getGameFailure() throws DataAccessException{
        GameData game = new GameData(100000, "white", "black", "gameName", new ChessGame());
        GameData gotGame = dataAccess.getGame(game);
        assert gotGame.getGameID() == 400;
    }
}
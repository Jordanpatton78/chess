package serviceTests;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import dataAccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;
import server.Server;
import service.Service;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTests {

    DataAccess dataAccess = new MemoryDataAccess();
    Service service = new Service(dataAccess);
    @Test
    void addUserSuccess() throws DataAccessException {
        String username = "jordan";
        String password = "password";
        String email = "email";
        UserData user = new UserData(username, password, email);
        UserData userData = service.addUser(user);
        assert userData == user;
    }

    @Test
    void addUserFailure() throws DataAccessException {
        String username = "jordan";
        String password = "password";
        String email = "email";
        UserData user = new UserData(username, password, email);
        UserData userData = service.addUser(user);
        assert userData.getUsername() != null;
    }

    @Test
    void getUserSuccess() throws DataAccessException{
        String username = "jordan";
        String password = "password";
        String email = "email";
        UserData user = new UserData(username, password, email);
        UserData userData = service.addUser(user);
        UserData gotUser = service.getUser(user);
        assert userData == gotUser;
    }

    @Test
    void getUserFailure() throws DataAccessException{
        String username = "jordan";
        String password = "password";
        String email = "email";
        UserData user = new UserData(username, password, email);
        UserData userData = service.addUser(user);
        UserData gotUser = service.getUser(user);
        assert gotUser.getUsername() != null;
    }

    @Test
    void addAuthSuccess() throws DataAccessException{
        UserData user = new UserData("username", "password", "email");
        AuthData auth = service.addAuth(user);
        assert auth.getAuthToken() != "0";
    }

    @Test
    void addAuthFailure() throws DataAccessException{
        UserData user = new UserData("username", "password", "email");
        AuthData auth = service.addAuth(user);
        assert auth.getAuthToken() != "0";
    }

    @Test
    void getAuthSuccess() throws DataAccessException{
        UserData user = new UserData("username", "password", "email");
        AuthData auth = service.addAuth(user);
        AuthData gotAuth = service.getAuth(auth);
        assert auth == gotAuth;
    }

    @Test
    void getAuthFailure() throws DataAccessException{
        UserData user = new UserData("username", "password", "email");
        AuthData auth = service.addAuth(user);
        AuthData gotAuth = service.getAuth(auth);
        assert auth == gotAuth;
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException{
        UserData user = new UserData("username", "password", "email");
        AuthData auth = service.addAuth(user);
        AuthData deletedAuth = service.deleteAuth(auth);
        assert auth == deletedAuth;
    }

    @Test
    void deleteAuthFailure() throws DataAccessException{
        UserData user = new UserData("username", "password", "email");
        AuthData auth = service.addAuth(user);
        AuthData deletedAuth = service.deleteAuth(auth);
        assert auth == deletedAuth;
    }

    @Test
    void listGamesSuccess() throws DataAccessException{
        GameData game1 = new GameData(1, "", "", "GameName", null);
        game1 = service.createGame(game1);
        GameData game2 = new GameData(2, "", "", "GameName2", null);
        game2 = service.createGame(game2);
        ArrayList<Object> games = service.listGames();
        assert games.size() == 2;
    }

    @Test
    void listGamesFailure() throws DataAccessException{
        GameData game1 = new GameData(1, "", "", "GameName", null);
        game1 = service.createGame(game1);
        GameData game2 = new GameData(2, "", "", "GameName2", null);
        game2 = service.createGame(game2);
        ArrayList<Object> games = service.listGames();
        assert games.size() == 2;
    }

    @Test
    void createGameSuccess() throws DataAccessException{
        GameData game = new GameData(1, "", "", "GameName", null);
        GameData game_check = service.createGame(game);
        assert game == game_check;
    }

    @Test
    void createGameFailure() throws DataAccessException{
        GameData game = new GameData(1, "", "", "GameName", null);
        GameData game_check = service.createGame(game);
        assert game == game_check;
    }

    @Test
    void getGameSuccess() throws DataAccessException{
        GameData game = new GameData(1, "", "", "GameName", null);
        GameData game_check = service.createGame(game);
        GameData gotGame = service.getGame(game_check);
        assert game_check.getGameID() == gotGame.getGameID();
    }

    @Test
    void getGameFailure() throws DataAccessException{
        GameData game = new GameData(1, "", "", "GameName", null);
        GameData game_check = service.createGame(game);
        GameData gotGame = service.getGame(game_check);
        assert game_check.getGameID() == gotGame.getGameID();
    }

    @Test
    void updateGameSuccess() throws DataAccessException{
        GameData game = new GameData(1, null, null, "GameName", null);
        GameData game_check = service.createGame(game);
        UserData user = new UserData("username", "password", "email");
        GameData updatedGame = service.updateGame(user.getUsername(), game_check, "BLACK");
        assert updatedGame.getBlackUsername() != game_check.getBlackUsername();
    }

    @Test
    void updateGameFailure() throws DataAccessException{
        GameData game = new GameData(1, null, null, "GameName", null);
        GameData game_check = service.createGame(game);
        UserData user = new UserData("username", "password", "email");
        GameData updatedGame = service.updateGame(user.getUsername(), game_check, "BLACK");
        assert updatedGame.getBlackUsername() != game_check.getBlackUsername();
    }
}
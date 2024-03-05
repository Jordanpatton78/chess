package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;

class MySQLDataAccessTest {

    DataAccess dataAccess;
    {
        try {
            dataAccess = new MySQLDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void dropDatabases1() throws DataAccessException{
        dataAccess.deleteAll();
    }
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  chess.user (
                username Varchar(255),
                password Varchar(255),
                email Varchar(255)
              )
            """,
            """
            CREATE TABLE IF NOT EXISTS  chess.game (
                gameID int,
                whiteUsername Varchar(255),
                blackUsername Varchar(255),
                gameName Varchar(255),
                game Varchar(255)
              )
            """,
            """
            CREATE TABLE IF NOT EXISTS  chess.auth (
                authToken Varchar(255),
                username Varchar(255)
              )
            """
    };
    @BeforeEach
    void createDatabases2() throws DataAccessException{
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
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
        AuthData auth = new AuthData("authToken", "jordan");
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
        game = dataAccess.createGame(game);
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

    @Test
    void listGamesSuccess() throws DataAccessException {
        GameData game = new GameData(1, "white", "black", "gameName", new ChessGame());
        GameData gameData = dataAccess.createGame(game);
        GameData game2 = new GameData(2, "white", "black", "gameName", new ChessGame());
        GameData gameData2 = dataAccess.createGame(game2);
        ArrayList<Object> games = dataAccess.listGames();
        assert games.size() == 2;
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        GameData game = new GameData(2, "white", "black", "gameName", new ChessGame());
        game = dataAccess.createGame(game);
        GameData new_game = dataAccess.updateGame(game);
        assert new_game.getGameID() == game.getGameID();
    }

    @Test
    void updateGameFailure() throws DataAccessException{
        GameData game = new GameData(100000, "white", "black", "gameName", new ChessGame());
        GameData gotGame = dataAccess.getGame(game);
        assert gotGame.getGameID() == 400;
    }

    @Test
    void deleteAllSuccess() throws DataAccessException{
        GameData game = new GameData(100000, "white", "black", "gameName", new ChessGame());
        GameData gameToMake = dataAccess.createGame(game);
        dataAccess.deleteAll();
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
        GameData gotGame = dataAccess.getGame(game);
        assert gotGame.getGameID() == 400;
    }
}
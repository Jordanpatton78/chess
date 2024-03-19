package clientTests;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;
import dataAccess.MySQLDataAccess;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.ServerFacade;
import server.Server;

import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
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
            CREATE TABLE IF NOT EXISTS  user (
                username Varchar(255),
                password Varchar(255),
                email Varchar(255)
              )
            """,
            """
            CREATE TABLE IF NOT EXISTS  game (
                gameID int,
                whiteUsername Varchar(255),
                blackUsername Varchar(255),
                gameName Varchar(255),
                game Varchar(255)
              )
            """,
            """
            CREATE TABLE IF NOT EXISTS  auth (
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

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        String strPort = Integer.toString(port);
        String url = "http://localhost:" + strPort;
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(url);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerTestPositive() throws ResponseException {
        UserData user = new UserData("username", "password", "email");
        AuthData registeredUser = facade.register(user);
        assert registeredUser.getUsername().equals(user.getUsername());
    }

    @Test
    public void registerTestNegative() throws ResponseException {
        UserData user = new UserData("username", "password", "email");
        assertThrows(ResponseException.class, () -> {
            facade.register(user);
            facade.register(user);
        });
    }

    @Test
    public void loginTestPositive() throws ResponseException {
        UserData user = new UserData("username", "password", "email");
        AuthData registeredUser = facade.register(user);
        AuthData loggedInUser = facade.login(user);
        assert loggedInUser.getUsername().equals(user.getUsername());
    }

    @Test
    public void loginTestNegative() throws ResponseException {
        UserData user = new UserData("username", "password", "email");
        UserData fakeUser = new UserData("New User", "password", "email");
        assertThrows(ResponseException.class, () -> {
            facade.register(user);
            facade.login(fakeUser);
        });
    }

    @Test
    public void logoutTestPositive() throws ResponseException, URISyntaxException {
        UserData user = new UserData("username", "password", "email");
        AuthData registeredUser = facade.register(user);
        AuthData loggedOutUser = facade.logout(registeredUser);
        assert loggedOutUser.getAuthToken().equals(registeredUser.getAuthToken());
    }

    @Test
    public void logoutTestNegative() throws ResponseException {
        UserData user = new UserData("username", "password", "email");
        AuthData fakeAuth = new AuthData("Fake Auth Token", "");
        assertThrows(ResponseException.class, () -> {
            facade.register(user);
            facade.logout(fakeAuth);
        });
    }

    @Test
    public void createGamePositive() throws ResponseException, URISyntaxException {
        UserData user = new UserData("username", "password", "email");
        GameData game = new GameData(10, "", "", "gameName", null);
        AuthData registeredUser = facade.register(user);
        GameData createdGame = facade.createGame(registeredUser, game);
        assert createdGame.getGameID() == game.getGameID();
    }

    @Test
    public void createGameNegative() throws ResponseException {
        UserData user = new UserData("username", "password", "email");
        GameData game = new GameData(10, "", "", "gameName", null);
        AuthData fakeAuth = new AuthData("Fake token", "");
        assertThrows(ResponseException.class, () -> {
            facade.register(user);
            GameData createdGame = facade.createGame(fakeAuth, game);
        });
    }
}

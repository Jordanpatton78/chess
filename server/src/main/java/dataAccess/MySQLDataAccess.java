package dataAccess;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLDataAccess implements DataAccess{

    static HashMap<String, UserData> userMap = new HashMap<>();
    static HashMap<String, AuthData> authMap = new HashMap<>();

    static HashMap<Integer, GameData> gameMap = new HashMap<>();
    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }
    @Override
    public UserData addUser(UserData user) throws DataAccessException {
        // First check to make sure the username doesn't already exist
        String usernameToCheck = user.getUsername();
        String query = "SELECT * FROM chess.user WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setString(1, usernameToCheck);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // Username already exists
                    UserData error = new UserData("403", "password", "email");
                    return error;
                } else {
                    // Username doesn't exist
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        // Then if the username doesn't exist we'll add the data to the db
        var statement = "INSERT INTO chess.user (username, password, email) VALUES (?, ?, ?)";
        var id = executeUpdate(statement, user.username(), user.password(), user.email());
        return new UserData(user.username(), user.password(), user.email());
    }

    @Override
    public UserData getUser(UserData user) throws DataAccessException {
        String username = user.getUsername();
        String password = user.getPassword();

        String query = "SELECT * FROM chess.user WHERE username = ? and password = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // Username already exists
                    String fetchedUsername = resultSet.getString("username");
                    String passwordFromDB = resultSet.getString("password");
                    String email = resultSet.getString("email");
                    UserData userToReturn = new UserData(fetchedUsername, passwordFromDB, email);
                    return userToReturn;
                } else {
                    // Username doesn't exist
                    UserData error = new UserData("401", "password", "email");
                    return error;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData addAuth(UserData user, AuthData authData) throws DataAccessException {
        String authToken = authData.getAuthToken();
        authMap.put(authToken, authData);
        return authData;
    }

    @Override
    public AuthData getAuth(AuthData authData) throws DataAccessException{
        String authToken = authData.getAuthToken();
        return authMap.get(authToken);
    }

    @Override
    public AuthData deleteAuth(AuthData auth) throws DataAccessException{
        String authToken = auth.getAuthToken();
        if (authMap.containsKey(authToken)){
            authMap.remove(authToken);
            return auth;
        }
        return null;
    }

    @Override
    public ArrayList<Object> listGames() throws DataAccessException{
        return new ArrayList<>(gameMap.values());
    }

    @Override
    public GameData createGame( GameData game) throws DataAccessException{
        int gameId = game.getGameID();
        gameMap.put(gameId, game);
        return game;
    }

    @Override
    public GameData getGame(GameData game) throws DataAccessException{
        int gameId = game.getGameID();
        GameData gameData = gameMap.get(gameId);
        return gameData;
    }

    @Override
    public GameData updateGame(GameData game) throws DataAccessException{
        int gameId = game.getGameID();
        gameMap.put(gameId, game);
        return game;
    }

    @Override
    public void deleteAll() throws DataAccessException {
        userMap.clear();
        authMap.clear();
        gameMap.clear();
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String s) ps.setString(i + 1, s);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
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
    private void configureDatabase() throws DataAccessException {
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
}

package dataAccess;

import chess.ChessGame;
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
        // Then if the username doesn't exist we'll add the data to the db
        String authToken = authData.getAuthToken();
        String username = user.getUsername();
        if (authToken.isEmpty()){
            return new AuthData("400", "username");
        }
        var statement = "INSERT INTO chess.auth (authToken, username) VALUES (?, ?)";
        var id = executeUpdate(statement, authToken, username);
        return new AuthData(authToken, username);
    }

    @Override
    public AuthData getAuth(AuthData authData) throws DataAccessException{
        String username = authData.getUsername();
        String authToken = authData.getAuthToken();

        String query = "SELECT * FROM chess.auth WHERE authToken = ? and username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setString(1, authToken);
            preparedStatement.setString(2, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // Username already exists
                    String authTokenToReturn = resultSet.getString("authToken");
                    String usernameToReturn = resultSet.getString("username");
                    AuthData authToReturn = new AuthData(authTokenToReturn, usernameToReturn);
                    return authToReturn;
                } else {
                    // Username doesn't exist
                    AuthData error = new AuthData("400", "");
                    return error;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData deleteAuth(AuthData auth) throws DataAccessException{
        String authToken = auth.getAuthToken();
        var statement = "DELETE FROM chess.auth WHERE authToken = ?";
        var id = executeUpdate(statement, authToken);
        return new AuthData(authToken, "");
    }

    @Override
    public ArrayList<Object> listGames() throws DataAccessException{
        return new ArrayList<>(gameMap.values());
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException{
        int gameIDtoCheck = game.getGameID();
        String query = "SELECT * FROM chess.game WHERE gameID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setInt(1, gameIDtoCheck);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // GameID already exists
                    GameData error = new GameData(403, "", "", "", new ChessGame());
                    return error;
                } else {
                    // GameID doesn't exist
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        var statement = "INSERT INTO chess.game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        Gson gson = new Gson();
        var id = executeUpdate(statement, game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), gson.toJson(game.getGame()));
        return new GameData(game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), game.getGame());
    }

    @Override
    public GameData getGame(GameData game) throws DataAccessException{
        int gameID = game.getGameID();

        String query = "SELECT * FROM chess.game WHERE gameID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setInt(1, gameID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // Game already exists
                    int gameIDToReturn = resultSet.getInt("gameID");
                    String whiteUser = resultSet.getString("whiteUsername");
                    String blackUser = resultSet.getString("blackUsername");
                    String gameName = resultSet.getString("gameName");
                    String chessGameString = resultSet.getString("game");
                    Gson gson = new Gson();
                    ChessGame chessGame = gson.fromJson(chessGameString, ChessGame.class);
                    GameData gameToReturn = new GameData(gameIDToReturn, whiteUser, blackUser, gameName, chessGame);
                    return gameToReturn;
                } else {
                    // Game doesn't exist
                    GameData error = new GameData(400, "", "", "", new ChessGame());
                    return error;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
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
                    else if (param instanceof Integer num ) ps.setInt(i+1, num);
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
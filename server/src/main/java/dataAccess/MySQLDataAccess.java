package dataAccess;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLDataAccess implements DataAccess{

    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }
    @Override
    public UserData addUser(UserData user) throws DataAccessException {
        // First check to make sure the username doesn't already exist
        String usernameToCheck = user.getUsername();
        String query = "SELECT * FROM user WHERE username = ?";
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
        if (user.getPassword() == null){
            UserData error = new UserData("400", "password", "email");
            return error;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(user.password());
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        var id = executeUpdate(statement, user.username(), hashedPassword, user.email());
        return new UserData(user.username(), hashedPassword, user.email());
    }

    @Override
    public UserData getUser(UserData user) throws DataAccessException {
        String username = user.getUsername();
        String password = user.getPassword();

        String query = "SELECT * FROM user WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // Username exists, retrieve hashed password from the database
                    String fetchedUsername = resultSet.getString("username");
                    String hashedPasswordFromDB = resultSet.getString("password");
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                    // Check if the provided password matches the hashed password from the database
                    if (encoder.matches(password, hashedPasswordFromDB)) {
                        // Passwords match, create and return user data
                        String email = resultSet.getString("email");
                        UserData userToReturn = new UserData(fetchedUsername, password, email);
                        return userToReturn;
                    } else {
                        // Passwords don't match
                        UserData error = new UserData("401", "password", "email");
                        return error;
                    }
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
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        var id = executeUpdate(statement, authToken, username);
        return new AuthData(authToken, username);
    }

    @Override
    public AuthData getAuth(AuthData authData) throws DataAccessException{
        String username = authData.getUsername();
        String authToken = authData.getAuthToken();

        String query = "SELECT * FROM auth WHERE authToken = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setString(1, authToken);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // AuthToken already exists
                    String authTokenToReturn = resultSet.getString("authToken");
                    String usernameToReturn = resultSet.getString("username");
                    AuthData authToReturn = new AuthData(authTokenToReturn, usernameToReturn);
                    return authToReturn;
                } else {
                    // AuthToken doesn't exist
                    AuthData error = new AuthData("401", "");
                    return error;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData deleteAuth(AuthData auth) throws DataAccessException{
        // Get Auth to verify
        String username = auth.getUsername();
        String authToken = auth.getAuthToken();

        String query = "SELECT * FROM auth WHERE authToken = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setString(1, authToken);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // AuthToken already exists
                    var statement = "DELETE FROM auth WHERE authToken = ?";
                    var id = executeUpdate(statement, authToken);
                    return new AuthData(authToken, "Deleted");
                } else {
                    // AuthToken doesn't exist
                    AuthData error = new AuthData("401", "");
                    return error;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

    }

    @Override
    public ArrayList<Object> listGames() throws DataAccessException{
        ArrayList<Object> games = new ArrayList<>();
        String query = "SELECT * FROM game";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                while (resultSet.next()) {
                    // Game already exists
                    int gameIDToReturn = resultSet.getInt("gameID");
                    String whiteUser = resultSet.getString("whiteUsername");
                    String blackUser = resultSet.getString("blackUsername");
                    String gameName = resultSet.getString("gameName");
                    String chessGameString = resultSet.getString("game");
                    Gson gson = new Gson();
                    ChessGame chessGame = gson.fromJson(chessGameString, ChessGame.class);
                    GameData gameToReturn = new GameData(gameIDToReturn, whiteUser, blackUser, gameName, chessGame);
                    games.add(gameToReturn);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return games;
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException{
        int gameIDtoCheck = game.getGameID();
        String query = "SELECT * FROM game WHERE gameID = ?";
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
        var statement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        Gson gson = new Gson();
        ChessGame chessGame = new ChessGame();
        ChessBoard board = new ChessBoard();
        chessGame.setBoard(board);
        var id = executeUpdate(statement, game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), gson.toJson(chessGame));
        return new GameData(game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), chessGame);
    }

    @Override
    public GameData getGame(GameData game) throws DataAccessException{
        int gameID = game.getGameID();

        String query = "SELECT * FROM game WHERE gameID = ?";
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
    public GameData updateGame(GameData game) throws DataAccessException {
        int gameIDtoCheck = game.getGameID();
        String query = "SELECT * FROM game WHERE gameID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter for the prepared statement
            preparedStatement.setInt(1, gameIDtoCheck);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any results
                if (resultSet.next()) {
                    // GameID already exists
                    int gameIDToReturn = resultSet.getInt("gameID");
                    // Update the existing data with new whiteUsername and blackUsername
                    String updateStatement = "UPDATE game SET whiteUsername = ?, blackUsername = ? WHERE gameID = ?";
                    try (PreparedStatement updatePreparedStatement = connection.prepareStatement(updateStatement)) {
                        updatePreparedStatement.setString(1, game.getWhiteUsername());
                        updatePreparedStatement.setString(2, game.getBlackUsername());
                        updatePreparedStatement.setInt(3, gameIDToReturn);
                        updatePreparedStatement.executeUpdate();
                    }
                    // Return the updated game data
                    Gson gson = new Gson();
                    return new GameData(gameIDToReturn, game.getWhiteUsername(), game.getBlackUsername(), resultSet.getString("gameName"), gson.fromJson(resultSet.getString("game"), ChessGame.class));
                } else {
                    // GameID doesn't exist
                    GameData error = new GameData(400, "", "", "", new ChessGame());
                    return error;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public GameData leaveGame(GameData game) throws DataAccessException {
        // Then if the username doesn't exist we'll add the data to the db
        int gameID = game.getGameID();
        String whiteUser = game.getWhiteUsername();
        String blackUser = game.getBlackUsername();
        String gameName = game.getGameName();
        ChessGame chessGame = game.getGame();
        var statement1 = "DELETE FROM game WHERE gameID = ?";
        var id = executeUpdate(statement1, gameID);
        Gson gson = new Gson();
        var statement2 = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        var id2 = executeUpdate(statement2, game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), gson.toJson(chessGame));
        return new GameData(game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), chessGame);
    }

    @Override
    public GameData move(GameData game) throws DataAccessException {
        // Then if the username doesn't exist we'll add the data to the db
        int gameID = game.getGameID();
        String whiteUser = game.getWhiteUsername();
        String blackUser = game.getBlackUsername();
        String gameName = game.getGameName();
        ChessGame chessGame = game.getGame();
        var statement1 = "DELETE FROM game WHERE gameID = ?";
        var id = executeUpdate(statement1, gameID);
        Gson gson = new Gson();
        var statement2 = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        var id2 = executeUpdate(statement2, game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), gson.toJson(chessGame));
        return new GameData(game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), chessGame);
    }


    @Override
    public void deleteAll() throws DataAccessException {
        var statement = "DELETE FROM user";
        var id = executeUpdate(statement);
        var statement2 = "DELETE FROM game";
        var id2 = executeUpdate(statement2);
        var statement3 = "DELETE FROM auth";
        var id3 = executeUpdate(statement3);
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
                game Varchar(10000)
              )
            """,
            """
            CREATE TABLE IF NOT EXISTS  auth (
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

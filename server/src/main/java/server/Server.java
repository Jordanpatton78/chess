package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import dataAccess.MemoryDataAccess;
import model.AuthData;
import model.ErrorData;
import model.GameData;
import model.UserData;
import service.Service;
import spark.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Server {

    private final Service service;
    DataAccess dataAccess = new MemoryDataAccess();

    public Server() {
        service = new Service(dataAccess);
    }
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);
        Spark.delete("/db", this::clear);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public Object register(Request req, Response res) throws DataAccessException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        UserData user_check = service.getUser(user);
        UserData new_user = null;
        if (user_check == null){
            new_user = service.addUser(user);
        }
        else{
            res.status(403);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: already taken.");
            return new Gson().toJson(error);
        }
        if (new_user.getPassword()==null){
            res.status(400);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: bad request");
            return new Gson().toJson(error);
        }
        AuthData authToken = service.addAuth(new_user);
        Object result = new Gson().toJson(authToken);
        return result;
    }

    private Object clear(Request req, Response res) throws DataAccessException{
        service.deleteAll();
        res.status(200);
        return "";
    }

    private Object login(Request req, Response res) throws DataAccessException{
        var user = new Gson().fromJson(req.body(), UserData.class);
        UserData user_check = service.getUser(user);
        if (user_check == null){
            res.status(401);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: Unauthorized");
            return new Gson().toJson(error);
        }
        AuthData authToken = service.addAuth(user);
        Object result = new Gson().toJson(authToken);
        return result;
    }

    private Object logout(Request req, Response res) throws DataAccessException {

        String authToken = req.headers("authorization");
        AuthData auth = new AuthData(authToken, "");
        // Delete the auth data
        AuthData auth_check = service.deleteAuth(auth);
        if (auth_check == null){
            res.status(401);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: Unauthorized");
            return new Gson().toJson(error);
        }
        // Convert the auth data to JSON and return it
        Object result = new Gson().toJson(auth);
        return result;
    }

    private Object listGames(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        var game = new Gson().fromJson(req.body(), GameData.class);
        AuthData auth = new AuthData(authToken, "");
        AuthData auth_check = service.getAuth(auth);
        if (auth_check == null) {
            res.status(401);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: Unauthorized");
            return new Gson().toJson(error);
        }
        ArrayList<Object> games = service.listGames();

        // Create a Map to store the games
        HashMap<String, Object> response = new HashMap<>();
        response.put("games", games);

        // Convert the Map to JSON using Gson
        return new Gson().toJson(response);
    }

    private Object createGame(Request req, Response res) throws DataAccessException{
        String authToken = req.headers("authorization");
        var game = new Gson().fromJson(req.body(), GameData.class);
        AuthData auth = new AuthData(authToken, "");
        AuthData auth_check = service.getAuth(auth);
        if (auth_check == null){
            res.status(401);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: Unauthorized");
            return new Gson().toJson(error);
        }
        GameData new_game = service.createGame(game);
        Object result = new Gson().toJson(new_game);
        return result;
    }

    private Object joinGame(Request req, Response res) throws DataAccessException{
        String authToken = req.headers("authorization");
        var game = new Gson().fromJson(req.body(), GameData.class);
        AuthData auth = new AuthData(authToken, "");
        AuthData auth_check = service.getAuth(auth);
        if (auth_check == null){
            res.status(401);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: Unauthorized");
            return new Gson().toJson(error);
        }
        // Parse the JSON string
        JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
        // Access the "playerColor" attribute
        String playerColor = "";
        if (jsonObject.has("playerColor")){
            playerColor = jsonObject.get("playerColor").getAsString();
        } else{
            playerColor = null;
        }
        String username = auth_check.getUsername();
        GameData gameData = service.getGame(game);
        if (gameData == null){
            res.status(400);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: bad request");
            return new Gson().toJson(error);
        }
        if (playerColor != null){
            if (playerColor.equals("BLACK") && gameData.getBlackUsername() != null){
                res.status(403);
                res.type("application/json");
                ErrorData error = new ErrorData("Error: already taken");
                return new Gson().toJson(error);
            } else if (playerColor.equals("WHITE") && gameData.getWhiteUsername() != null) {
                res.status(403);
                res.type("application/json");
                ErrorData error = new ErrorData("Error: already taken");
                return new Gson().toJson(error);
            }
        }
        GameData updatedGame = service.updateGame(username, gameData, playerColor);
        Object result = new Gson().toJson(updatedGame);
        return result;
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

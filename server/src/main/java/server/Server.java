package server;

import com.google.gson.Gson;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import dataAccess.MemoryDataAccess;
import model.AuthData;
import model.ErrorData;
import model.UserData;
import service.Service;
import spark.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;
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
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);

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
        AuthData authToken = service.addAuth(new_user);
        Object result = new Gson().toJson(authToken);
        return result;
    }

    private Object clear(Request req, Response res) throws DataAccessException{
        service.deleteAll();
        res.status(204);
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

    private Object listGames(Request req, Response res) throws DataAccessException{
        var auth = new Gson().fromJson(req.body(), AuthData.class);
        AuthData auth_check = service.getAuth(auth);
        if (auth_check == null){
            res.status(401);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: Unauthorized");
            return new Gson().toJson(error);
        }
        ArrayList<Object> games = service.listGames();
        return new Gson().toJson(games);
    }

    private Object createGame(Request req, Response res) throws DataAccessException{
        var auth = new Gson().fromJson(req.body(), AuthData.class);
        AuthData auth_check = service.getAuth(auth);
        if (auth_check == null){
            res.status(401);
            res.type("application/json");
            ErrorData error = new ErrorData("Error: Unauthorized");
            return new Gson().toJson(error);
        }
        service.createGame();
        return "";
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

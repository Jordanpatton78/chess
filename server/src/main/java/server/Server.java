package server;

import com.google.gson.Gson;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.Service;
import spark.*;

import javax.xml.crypto.Data;

public class Server {

    private final Service service;
    DataAccess dataAccess = new DataAccess();

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

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object register(Request req, Response res) throws DataAccessException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        user = service.addUser(user);
        return new Gson().toJson(user);
    }

    private Object clear(Request req, Response res) throws DataAccessException{
        service.deleteAll();
        res.status(204);
        return "";
    }

    private Object login(Request req, Response res) throws DataAccessException{
        var user = new Gson().fromJson(req.body(), UserData.class);
        user = service.getUser(user);
        var auth = new Gson().fromJson(req.body(), AuthData.class);
        var authToken = service.createAuth(user, auth);
        return new Gson().toJson(authToken);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

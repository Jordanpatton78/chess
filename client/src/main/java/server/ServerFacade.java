package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData login(UserData user) throws ResponseException {
        var path = "/session";
        return this.makeRequest("POST", path, user, null, null, AuthData.class);
    }

    public AuthData register(UserData user) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, user, null, null, AuthData.class);
    }

    public AuthData logout(AuthData auth) throws ResponseException {
        var path = "/session";
        String authToken = auth.getAuthToken();
        return this.makeRequest("DELETE", path, auth, authToken, null, AuthData.class);
    }

    public GameData createGame(AuthData auth, GameData game) throws ResponseException {
        var path = "/game";
        String authToken = auth.getAuthToken();
        return this.makeRequest("POST", path, game, authToken, null, GameData.class);
    }

    public ArrayList<GameData> listGames(AuthData auth) throws ResponseException {
        var path = "/game";
        String authToken = auth.getAuthToken();
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if (authToken != null){
                http.setRequestProperty("authorization", authToken);
            }
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.connect();

            // Read response
            ArrayList<GameData> gamesList = null;
            int responseCode = http.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader reader = new InputStreamReader(http.getInputStream());

                // Convert JSON response to HashMap
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<String, ArrayList<GameData>>>() {}.getType();
                HashMap<String, ArrayList<GameData>> resultMap = gson.fromJson(reader, type);

                // Extract the "games" array
                gamesList = resultMap.get("games");
                return gamesList;
            } else {
                throw new ResponseException(responseCode, "Error occurred: " + http.getResponseMessage());
            }
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public GameData joinGame(AuthData auth, GameData game, String playerColor) throws ResponseException {
        var path = "/game";
        int gameID = game.getGameID();
        Gson gson = new Gson();
        String json = "{\"playerColor\":" + playerColor + ",\"gameID\":" + gameID + "}";
        String authToken = auth.getAuthToken();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        return this.makeRequest("PUT", path, jsonObject, authToken, playerColor, GameData.class);
    }

    public GameData joinObserver(AuthData auth, GameData game, String playerColor) throws ResponseException {
        var path = "/game";
        int gameID = game.getGameID();
        Gson gson = new Gson();
        String json = "{\"playerColor\":" + playerColor + ",\"gameID\":" + gameID + "}";
        String authToken = auth.getAuthToken();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        return this.makeRequest("PUT", path, jsonObject, authToken, playerColor, GameData.class);
    }

    private <T> T makeRequest(String method, String path, Object request, String auth, String playerColor, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if (auth != null){
                http.setRequestProperty("authorization", auth);
            }
            if (playerColor != null){
                http.setRequestProperty("playerColor", playerColor);
            }
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
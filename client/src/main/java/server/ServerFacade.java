package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
//import model.Pet;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }


//    public void preLoginHelp() throws ResponseException {
//        var path = "/pet";
//        return this.makeRequest("POST", path, pet, Pet.class);
//    }
//
//    public void quit() throws ResponseException {
//        var path = "/pet";
//        return this.makeRequest("POST", path, pet, Pet.class);
//    }

    public AuthData login(UserData user) throws ResponseException {
        var path = "/session";
        return this.makeRequest("POST", path, user, null, AuthData.class);
    }

    public AuthData register(UserData user) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, user, null, AuthData.class);
    }
//
//    public void postLoginHelp() throws ResponseException {
//        var path = "/pet";
//        return this.makeRequest("POST", path, pet, Pet.class);
//    }
//
    public AuthData logout(AuthData auth) throws ResponseException, URISyntaxException {
        var path = "/session";
        String authToken = auth.getAuthToken();
        return this.makeRequest("DELETE", path, auth, authToken, AuthData.class);
    }
//
    public GameData createGame(AuthData auth, GameData game) throws ResponseException {
        var path = "/game";
        String authToken = auth.getAuthToken();
        return this.makeRequest("POST", path, game, authToken, GameData.class);
    }
//
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

private HashMap<String, Object> convertJsonToHashMap(String jsonResponse) {
    // Your implementation to parse JSON and create a HashMap
    // Example:
    // Gson gson = new Gson();
    // Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
    // return gson.fromJson(jsonResponse, type);

    // Dummy implementation
    HashMap<String, Object> dummyMap = new HashMap<>();
    dummyMap.put("dummyKey", "dummyValue");
    return dummyMap;
}
//
//    public void joinGame() throws ResponseException {
//        var path = "/pet";
//        return this.makeRequest("POST", path, pet, Pet.class);
//    }
//
//    public void joinObserver() throws ResponseException {
//        var path = "/pet";
//        return this.makeRequest("POST", path, pet, Pet.class);
//    }

//    public Pet addPet(Pet pet) throws ResponseException {
//        var path = "/pet";
//        return this.makeRequest("POST", path, pet, Pet.class);
//    }
//
//    public void deletePet(int id) throws ResponseException {
//        var path = String.format("/pet/%s", id);
//        this.makeRequest("DELETE", path, null, null);
//    }
//
//    public void deleteAllPets() throws ResponseException {
//        var path = "/pet";
//        this.makeRequest("DELETE", path, null, null);
//    }
//

    private <T> T makeRequest(String method, String path, Object request, String headers, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if (headers != null){
                http.setRequestProperty("authorization", headers);
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
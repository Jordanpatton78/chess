package model;

import com.google.gson.*;

public record UserData(String username, String password, String email) {

    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
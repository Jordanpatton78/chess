package model;

import com.google.gson.*;

public record ErrorData(String message) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
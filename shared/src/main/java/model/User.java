package model;

import com.google.gson.*;

public record User() {
    public String toString() {
        return new Gson().toJson(this);
    }
}
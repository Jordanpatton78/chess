package model;

import com.google.gson.*;

public record ErrorData(String message) {

//    public String getErrorMessage(){
//        return this.message;
//    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
package model;

import chess.ChessGame;
import com.google.gson.*;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    public int getGameID(){
        return this.gameID;
    }
    public String getWhiteUsername(){
        return whiteUsername;
    }

    public String getBlackUsername(){
        return blackUsername;
    }

    public String getGameName(){
        return gameName;
    }

    public ChessGame getGame(){
        return game;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
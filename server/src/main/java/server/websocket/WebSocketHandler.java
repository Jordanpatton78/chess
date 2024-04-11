package server.websocket;

import com.google.gson.Gson;
import dataAccess.DataAccess;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.userCommands.UserGameCommand;
import webSocketMessages.serverMessages.ServerMessage;

import java.io.IOException;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case JOIN_PLAYER -> joinPlayer(action.getUsername(), session);
//            case EXIT -> exit(action.visitorName());
        }
    }

    private void joinPlayer(String visitorName, Session session) throws IOException {
        connections.add(visitorName, session);
        var message = String.format("%s just joined the game as a player.", visitorName);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, serverMessage);
    }

//    private void exit(String visitorName) throws IOException {
//        connections.remove(visitorName);
//        var message = String.format("%s left the shop", visitorName);
//        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType, message);
//        connections.broadcast(visitorName, serverMessage);
//    }
//
//    public void makeNoise(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var serverMessage = new ServerMessage(ServerMessage.Type.NOISE, message);
//            connections.broadcast("", serverMessage);
//        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
}
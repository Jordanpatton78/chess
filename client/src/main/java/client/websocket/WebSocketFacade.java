package client.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import webSocketMessages.userCommands.UserGameCommand;
import webSocketMessages.serverMessages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;


    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/connect");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void joinGame(String authToken, String username, int gameID) throws ResponseException {
        try {
            var action = new UserGameCommand( UserGameCommand.CommandType.JOIN_PLAYER, authToken, username, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void observeGame(String authToken, String username, int gameID) throws ResponseException {
        try {
            var action = new UserGameCommand( UserGameCommand.CommandType.JOIN_OBSERVER, authToken, username, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(String authToken, String username, int gameID) throws ResponseException {
        try {
            var action = new UserGameCommand( UserGameCommand.CommandType.MAKE_MOVE, authToken, username, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leave(String authToken, String username, int gameID) throws ResponseException {
        try {
            var action = new UserGameCommand( UserGameCommand.CommandType.LEAVE, authToken, username, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resign(String authToken, String username, int gameID) throws ResponseException {
        try {
            var action = new UserGameCommand( UserGameCommand.CommandType.RESIGN, authToken, username, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void check(String authToken, String username, int gameID) throws ResponseException {
        try {
            var action = new UserGameCommand( UserGameCommand.CommandType.CHECK, authToken, username, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
    public void checkmate(String authToken, String username, int gameID) throws ResponseException {
        try {
            var action = new UserGameCommand( UserGameCommand.CommandType.CHECKMATE, authToken, username, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

}
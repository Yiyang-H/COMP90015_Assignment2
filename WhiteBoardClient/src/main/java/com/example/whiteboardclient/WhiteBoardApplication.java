package com.example.whiteboardclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

/**
 * Entry point of the client application, starts the GUI and the maintains a session object
 */
public class WhiteBoardApplication extends Application {
    private static Stage primaryStage;
    private static WhiteBoardSession session = null;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        changeScene("homepage.fxml");
        stage.setTitle("White Board");
        stage.setResizable(false);
        stage.setHeight(800);
        stage.setWidth(1200);
        stage.show();
    }

    public static void changeScene(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(WhiteBoardApplication.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            primaryStage.setScene(scene);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public static WhiteBoardSession getSession() {
        return session;
    }

    public static void createWhiteBoard() {

        session = new WhiteBoardSession(Client.getInstance().createRoom());
        session.start();
    }

    public static void joinWhiteBoard(int roomId) {
        Socket s = Client.getInstance().joinRoom(roomId);
        if(s != null) {
            session = new WhiteBoardSession(s);
            session.start();
        }else {
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Join request");
                alert.setHeaderText(null);
                alert.setContentText( "Host has rejected your join request.");
                alert.showAndWait();
            });
        }

    }

    public static void backToHomepage() {
        session = null;
        changeScene("homepage.fxml");
    }

    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println("Not enough arguments");
            System.exit(1);
        }
        String serverAddress = args[0];
        int serverPort = 0;
        try{
            serverPort = Integer.parseInt(args[1]);
        }catch(NumberFormatException e) {
            System.out.println("Invalid port number");
        }
        String clientName = args[2];

        new Client(serverAddress,serverPort,clientName);
        launch();

        if(session != null) {
            session.closeSession();
        }

    }

}

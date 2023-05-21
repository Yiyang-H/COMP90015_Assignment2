package com.example.whiteboardclient;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

/**
 * Controls the behaviour of the white board
 */
public class WhiteBoardController {
    @FXML MenuBar menuBar = null;
    @FXML CanvasController canvasController = null;
    @FXML ListView usersList = null;

    @FXML Button kickButton = null;
    @FXML ListView chat = null;
    @FXML TextArea input = null;

    private WhiteBoardSession session;


    @FXML
    void initialize() {
        session = WhiteBoardApplication.getSession();
        if(kickButton != null) {
            kickButton.disableProperty().bind(usersList.getSelectionModel().selectedItemProperty().isNull());
            kickButton.setOnAction((e) -> {
                System.out.println(usersList.getSelectionModel().selectedItemProperty().get());
                session.kickUser((String) usersList.getSelectionModel().selectedItemProperty().get());
            });
        }


        input.setOnKeyPressed((e)->{
            if(e.getCode() == KeyCode.ENTER) {
                session.sendChat(input.getText());
                input.setText("");
            }


        });

        session.setWhiteBoardController(this);
    }

    public ListView getUsersList() {
        return usersList;
    }

    public ListView getChat() {
        return chat;
    }

    @FXML void handleNew() {
        canvasController.handleNew();
    }

    @FXML void handleOpen() {
        canvasController.handleOpen();
    }

    @FXML void handleSave() {
        canvasController.handleSave();
    }

    @FXML void handleSaveAs() {
        canvasController.handleSaveAs();
    }

    @FXML void handleClose() {
        canvasController.handleClose();
    }


}

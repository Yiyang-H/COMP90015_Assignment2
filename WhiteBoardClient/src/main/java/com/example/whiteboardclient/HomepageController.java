package com.example.whiteboardclient;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Controls the behaviour of the homepage
 */
public class HomepageController {

    @FXML private TableView<Room> table = null;
    @FXML private Button create = null;
    @FXML private Button refresh = null;
    @FXML private Button join = null;

    @FXML
    public void initialize() {
//        table = new TableView<>();
        TableColumn<Room, String> roomName = new TableColumn<>("Room Name");
        roomName.setCellValueFactory(new PropertyValueFactory<>("name"));
        table.setPlaceholder(new Label("No room is open, create one!"));


        table.getColumns().addAll(roomName);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        updateTable(table);

//        roomName.setMinWidth(300);
        refresh.setOnAction((e) -> {
            updateTable(table);
        });

        join.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        join.setOnAction((e) -> {
            int roomId = table.getSelectionModel().selectedItemProperty().get().getRoomId();

            WhiteBoardApplication.joinWhiteBoard(roomId);
            WhiteBoardApplication.changeScene("client-white-board.fxml");
        });

        create.setOnAction((e) -> {

            WhiteBoardApplication.createWhiteBoard();
            WhiteBoardApplication.changeScene("white-board.fxml");
        });
    }

    public void updateTable(TableView table) {
        JSONArray rooms = Client.getInstance().getRooms();
        if(rooms != null) {
            table.getItems().clear();
            for(Object room : rooms) {
                JSONObject roomJSON = (JSONObject) room;
                String roomName = (String) roomJSON.get("host");
                int roomId = Integer.parseInt(roomJSON.get("roomId").toString());
                table.getItems().add(new Room(roomName, roomId));
            }
        }
    }






}

package com.example.whiteboardclient;

import Draw.Drawable;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Session connected to the server, it receives notifications/updates from the server and interact with GUI
 * of the application to display those notifications/updates.
 */
public class WhiteBoardSession extends Thread {
    private GraphicsContext gc;
    private Socket socket;
    private CanvasController canvasController;
    private WhiteBoardController whiteBoardController;
//    private ConcurrentHashMap<String, Boolean> users;
    private ListView users;
    private ListView chat;



    private Queue<Drawable> uploads;

    private DataOutputStream output;
    private DataInputStream input;


    public WhiteBoardSession(Socket socket) {
        uploads = new ConcurrentLinkedQueue<>();
        this.socket = socket;
    }

    public void setCanvasController(CanvasController canvasController) {
        this.canvasController = canvasController;
    }

    public void setWhiteBoardController(WhiteBoardController whiteBoardController) {
        this.whiteBoardController = whiteBoardController;
    }


    public void addDrawing(Drawable newDrawing) {
        JSONObject request = new JSONObject();
        request.put("operation", "Draw");
        request.put("drawing", newDrawing.toJSONObject());
        try {
            output.writeUTF(request.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newWhiteBoard() {
        JSONObject request = new JSONObject();
        request.put("operation", "New");
        try {
            output.writeUTF(request.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void kickUser(String user) {
        JSONObject request = new JSONObject();
        request.put("operation", "Kick");
        request.put("clientName", user);
        try {
            output.writeUTF(request.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openWhiteBoard(JSONArray drawings) {
        JSONObject request = new JSONObject();
        request.put("operation", "Open");
        request.put("drawings", drawings);
        try {
            output.writeUTF(request.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSession() {
        JSONObject closeRequest = new JSONObject();
        closeRequest.put("operation", "CloseSession");
        try {
            output.writeUTF(closeRequest.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void sendChat(String text) {
        JSONObject request = new JSONObject();
        request.put("operation", "Chat");
        request.put("text", text);
        try {
            output.writeUTF(request.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void run() {
        try{
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());

            while(canvasController == null || whiteBoardController == null) {
                Thread.sleep(100);
            }
            gc = canvasController.getGc();
            users = whiteBoardController.getUsersList();
            chat = whiteBoardController.getChat();

            while(socket != null) {

                // If there is anything drawn to be uploaded
                if(!uploads.isEmpty()) {
                    JSONObject drawRequest = new JSONObject();
                    drawRequest.put("operation", "Draw");
                    drawRequest.put("drawing", uploads.poll().toJSONObject());
                    output.writeUTF(drawRequest.toJSONString());
                }else if(input.available() > 0) {
                    String notification = input.readUTF();
                    System.out.println("notification from server: " + notification);

                    JSONParser parser = new JSONParser();
                    JSONObject notiObject = (JSONObject) parser.parse(notification);
                    String operation = (String) notiObject.get("operation");
                    switch(operation) {
                        case "Draw":
                            JSONObject drawingJSON = (JSONObject) notiObject.get("drawing");
                            Drawable drawing = Drawable.parseDrawable(drawingJSON);

                            Platform.runLater(()->{
                                drawing.draw(gc);
                                canvasController.drawings.add(drawing);
                            });

                            break;
                        case "Open":
                            while(canvasController == null) {
                                Thread.sleep(100);
                            }
                            JSONArray drawings = (JSONArray) notiObject.get("drawings");
                            Platform.runLater(()->{
                                canvasController.clearBoard();
                                canvasController.drawAll(drawings);
                            });
                            break;
                        case "New":
                            Platform.runLater(()->{
                                canvasController.clearBoard();
                            });
                            break;
                        case "JoinRoom":
                            // Only host may receive the join room request from other users
                            String clientName = (String) notiObject.get("clientName");
                            Platform.runLater(()->{
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Join request");
                                alert.setHeaderText(null);
                                alert.setContentText(clientName + " wants to join the room.");

                                ButtonType accept = new ButtonType("Accept", ButtonBar.ButtonData.YES);
                                ButtonType reject = new ButtonType("Reject", ButtonBar.ButtonData.CANCEL_CLOSE);
                                alert.getButtonTypes().setAll(accept, reject);

                                if(alert.showAndWait().get() == accept) {
                                    notiObject.put("status", "success");
                                    try {
                                        output.writeUTF(notiObject.toJSONString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    notiObject.put("status", "rejected");
                                    try {
                                        output.writeUTF(notiObject.toJSONString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case "UpdateUsers":
                            JSONArray updatedUsers = (JSONArray) notiObject.get("users");
                            Platform.runLater(()->{
                                users.getItems().setAll(updatedUsers);
                            });
                            break;
                        case "CloseSession":
                            Platform.runLater(()->{
                                WhiteBoardApplication.backToHomepage();
                                showAlert("Session closed", "The host has closed the drawing board.");
                            });

                            socket = null;
                            break;
                        case "Kick":
                            Platform.runLater(()->{
                                WhiteBoardApplication.backToHomepage();
                                showAlert("Kicked!", "The host kicked you.");
                            });
                            break;
                        case "Chat":
                            if(chat != null) {
                                Platform.runLater(()->{
                                    String sender = (String) notiObject.get("sender");
                                    chat.getItems().add(sender + " says: " + notiObject.get("text"));
                                });
                            }
                            break;
                        default:
                            break;
                    }

                }
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

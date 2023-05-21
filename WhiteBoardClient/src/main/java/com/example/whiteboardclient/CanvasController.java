package com.example.whiteboardclient;

import Draw.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Controls the behaviour of the canvas
 */
public class CanvasController {
    @FXML private Canvas canvas = null;
    @FXML private Canvas tempCanvas = null;
    @FXML private StackPane pane = null;
    @FXML private Pane textPane = null;
    @FXML private HBox tools = null;
    @FXML private ToggleButton penButton = null;
    @FXML private ToggleButton lineButton = null;
    @FXML private ToggleButton circleButton = null;
    @FXML private ToggleButton triangleButton = null;
    @FXML private ToggleButton rectangleButton = null;
    @FXML private ToggleButton textInputButton = null;

    @FXML private ColorPicker colorPicker = null;

    private GraphicsContext gc;
    private GraphicsContext tempGc;


    public ConcurrentLinkedQueue<Drawable> drawings;

    private File currentFile = null;

    // Tools that the user is currently using
    // 0 for pen, 1 for draw line, 2 for draw circle, 3 for triangle, 4 for rectangle
    private int tool = 0;
    private Point2D start = null;
    private HandWriting currentHW = null;
    private WhiteBoardSession whiteBoardSession = null;


    @FXML
    void initialize() {

        whiteBoardSession = WhiteBoardApplication.getSession();

        gc = canvas.getGraphicsContext2D();


        tempGc = tempCanvas.getGraphicsContext2D();
        clearBoard();

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        tempCanvas.widthProperty().bind(pane.widthProperty());
        tempCanvas.heightProperty().bind(pane.heightProperty());

        pane.setStyle("-fx-background-color:white;");
        tools.setAlignment(Pos.TOP_CENTER);

        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(penButton, lineButton, circleButton, triangleButton, rectangleButton, textInputButton);
        tg.selectToggle(penButton);

        colorPicker.setOnAction((e) -> {
            gc.setStroke(colorPicker.getValue());
            tempGc.setStroke(colorPicker.getValue());
        });

        pane.setOnMousePressed((e)-> {
            gc.beginPath();
            start = toLocal(e.getSceneX(), e.getSceneY());
            if(tool == 0) {
                currentHW = new HandWriting(gc.getStroke());
            }
            if(tool == 5) {
                TextField tf = new TextField("Enter text...");
                textPane.getChildren().add(tf);

                String style = String.format("-fx-background-color: rgba(0, 0, 0, 0);-fx-text-inner-color: #%s;", colorPicker.getValue().toString().substring(2));
                tf.setStyle(style);
                tf.setLayoutX(start.getX());
                tf.setLayoutY(start.getY());

                tf.requestFocus();
                tf.selectAll();
                tf.focusedProperty().addListener((focus) -> {
                    System.out.println(focus.toString());
                    tf.setVisible(false);
                });
                tf.setOnAction((event)->{
                    Text text = new Text(start.getX(), start.getY(), tf.getText(), gc.getStroke());
                    whiteBoardSession.addDrawing(text);
                    drawings.add(text);
                    text.draw(gc);

                    tf.setVisible(false);
                });
            }
        });
        pane.setOnMouseDragged((e) -> {
            Point2D p = toLocal(e.getSceneX() ,e.getSceneY());
            tempGc.clearRect(0, 0, pane.getWidth(), pane.getHeight());

            // Show temporary shape
            switch(tool) {
                case 0:
                    gc.lineTo(p.getX(),p.getY());
                    gc.stroke();
                    currentHW.addPoint(p);
                    break;
                case 1:
                    // draw a line
                    Line line = new Line(start.getX(), start.getY(), p.getX(), p.getY(), gc.getStroke());
                    line.draw(tempGc);
                    break;
                case 2:
                    // draw a circle
                    Circle circle = new Circle(start.getX(), start.getY(), p.getX(), p.getY(), gc.getStroke());
                    circle.draw(tempGc);
                    break;
                case 3:
                    // draw a triangle
                    Triangle triangle = new Triangle(start.getX(), start.getY(), p.getX(), p.getY(), gc.getStroke());
                    triangle.draw(tempGc);
                    break;
                case 4:
                    // draw a rectangle
                    Rectangle rectangle = new Rectangle(start.getX(), start.getY(), p.getX(), p.getY(), gc.getStroke());
                    rectangle.draw(tempGc);
                    break;
                default:
                    break;
            }

        });

        pane.setOnMouseReleased((e) -> {
            Point2D p = toLocal(e.getSceneX() ,e.getSceneY());
            tempGc.clearRect(0, 0, pane.getWidth(), pane.getHeight());

            // Draw on gc
            switch(tool) {
                case 0:
                    if(currentHW.hasPoints()) {
                        drawings.add(currentHW);
                        whiteBoardSession.addDrawing(currentHW);
                        currentHW = null;
                    }
                    break;
                case 1:
                    // draw a line
                    Line line = new Line(start.getX(), start.getY(), p.getX(), p.getY(), gc.getStroke());
                    drawings.add(line);
                    whiteBoardSession.addDrawing(line);
                    line.draw(gc);
                    break;
                case 2:
                    // draw a circle
                    Circle circle = new Circle(start.getX(), start.getY(), p.getX(), p.getY(), gc.getStroke());
                    drawings.add(circle);
                    whiteBoardSession.addDrawing(circle);
                    circle.draw(gc);
                    break;
                case 3:
                    // draw a triangle
                    Triangle triangle = new Triangle(start.getX(), start.getY(), p.getX(), p.getY(), gc.getStroke());
                    drawings.add(triangle);
                    whiteBoardSession.addDrawing(triangle);
                    triangle.draw(gc);
                    break;
                case 4:
                    // draw a rectangle
                    Rectangle rectangle = new Rectangle(start.getX(), start.getY(), p.getX(), p.getY(), gc.getStroke());
                    drawings.add(rectangle);
                    whiteBoardSession.addDrawing(rectangle);
                    rectangle.draw(gc);
                    break;
                default:
                    break;
            }
        });


        whiteBoardSession.setCanvasController(this);
    }

    private Point2D toLocal(double x, double y) {
        try {
            return pane.getLocalToSceneTransform().inverseTransform(x,y);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void switchTool(ActionEvent e) {
        Node node = (Node)e.getSource();
        String x = (String) node.getUserData();
        tool = Integer.parseInt(x);
    }



    public void clearBoard() {
        colorPicker.setValue(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.clearRect(0, 0, pane.getWidth(), pane.getHeight());
        textPane.getChildren().clear();
        tempGc.setStroke(Color.BLACK);
        tempGc.setLineWidth(1);
        tempGc.clearRect(0, 0, pane.getWidth(), pane.getHeight());
        drawings = new ConcurrentLinkedQueue<>();
        // New will reset the currentFile
        currentFile = null;
    }

    public void handleNew() {
        whiteBoardSession.newWhiteBoard();
    }

    public void handleOpen() {
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(pane.getScene().getWindow());
        if(selectedFile == null) {
            return;
        }
        currentFile = selectedFile;
        clearBoard();
        try {
            Scanner myReader = new Scanner(selectedFile);
            JSONParser parser = new JSONParser();
            JSONArray drawingArray = (JSONArray) parser.parse(myReader.nextLine());
            myReader.close();
            whiteBoardSession.openWhiteBoard(drawingArray);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public void drawAll(JSONArray allDrawings) {
        for(Object drawingObj : allDrawings) {
            Drawable drawing = Drawable.parseDrawable((JSONObject) drawingObj);
            drawing.draw(gc);
            drawings.add(drawing);
        }

        gc.setStroke(colorPicker.getValue());
    }

    public void handleSave() {

        if(currentFile == null) {
            FileChooser fc = new FileChooser();
            currentFile = fc.showSaveDialog(pane.getScene().getWindow());
        }
        if(currentFile == null) {
            return;
        }

        try {
            FileWriter fw = new FileWriter(currentFile);
            JSONArray drawingArray = new JSONArray();
            for(Drawable drawing : drawings) {
                drawingArray.add(drawing.toJSONObject());
            }
            fw.write(drawingArray.toJSONString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleSaveAs() {
        FileChooser fc = new FileChooser();
        currentFile = fc.showSaveDialog(pane.getScene().getWindow());

        if(currentFile == null) {
            return;
        }

        try {
            FileWriter fw = new FileWriter(currentFile);
            JSONArray drawingArray = new JSONArray();
            for(Drawable drawing : drawings) {
                drawingArray.add(drawing.toJSONObject());
            }
            fw.write(drawingArray.toJSONString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GraphicsContext getGc() {
        return gc;
    }

    public void handleClose() {
        whiteBoardSession.closeSession();
        WhiteBoardApplication.backToHomepage();
    }

}

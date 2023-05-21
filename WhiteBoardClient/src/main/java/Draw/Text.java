package Draw;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import org.json.simple.JSONObject;

public class Text extends Drawable{

    private double x;
    private double y;
    private String text;

    public Text(double x, double y, String text, Paint strokeColor) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.strokeColor = strokeColor;
    }

    @Override
    public void draw(GraphicsContext gc) {
//        super.draw(gc);
//        gc.strokeText(text, x, y);
//        gc.setStroke(prevStrokeColor);

        // Get the text pane
        Pane p = (Pane) gc.getCanvas().getParent().getChildrenUnmodifiable().get(2);
        TextField tf = new TextField(text);
        String style = String.format("-fx-background-color: rgba(0, 0, 0, 0);-fx-text-inner-color: #%s;", strokeColor.toString().substring(2));
        tf.setStyle(style);
        tf.setLayoutX(x);
        tf.setLayoutY(y);
        tf.setDisable(true);
        tf.setOpacity(1);
        p.getChildren().add(tf);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("Drawable", "Text");
        object.put("x", x);
        object.put("y", y);
        object.put("text", text);
        object.put("strokeColor", strokeColor.toString());
        return object;
    }
}

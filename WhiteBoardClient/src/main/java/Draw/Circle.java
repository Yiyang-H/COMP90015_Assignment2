package Draw;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import org.json.simple.JSONObject;

public class Circle extends Shape {


    public Circle(double x1, double y1, double x2, double y2, Paint strokeColor) {
        super(x1, y1, x2, y2, strokeColor);
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        double startX = Math.min(x1, x2);
        double startY = Math.min(y1, y2);
        double width = Math.abs(x1 - x2);
        double height = Math.abs(y1 - y2);
        gc.strokeOval(startX, startY, width, height);
        gc.setStroke(prevStrokeColor);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("Drawable", "Circle");
        object.put("x1", x1);
        object.put("x2", x2);
        object.put("y1", y1);
        object.put("y2", y2);
        object.put("strokeColor", strokeColor.toString());

        return object;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "x1=" + x1 +
                ", y1=" + y1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                '}';
    }
}

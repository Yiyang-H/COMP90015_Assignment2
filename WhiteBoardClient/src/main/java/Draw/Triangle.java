package Draw;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import org.json.simple.JSONObject;

public class Triangle extends Shape {
    public Triangle(double x1, double y1, double x2, double y2, Paint strokeColor) {
        super(x1, y1, x2, y2, strokeColor);
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        double topX = (x1 + x2) / 2;
        double topY = Math.min(y1, y2);
        gc.strokeLine(topX, topY, x1, y2);
        gc.strokeLine(topX, topY, x2, y2);
        gc.strokeLine(x1, y2, x2, y2);
        gc.setStroke(prevStrokeColor);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("Drawable", "Triangle");
        object.put("x1", x1);
        object.put("x2", x2);
        object.put("y1", y1);
        object.put("y2", y2);
        object.put("strokeColor", strokeColor.toString());

        return object;
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "x1=" + x1 +
                ", y1=" + y1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                '}';
    }
}

package Draw;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class HandWriting extends Drawable {

    private ArrayList<Point2D> points;

    public HandWriting(Paint strokeColor) {
        this.strokeColor = strokeColor;
        points = new ArrayList<>();
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        Point2D lastPoint = null;
        gc.beginPath();
        for(Point2D point : points) {
            if(lastPoint == null) {
                lastPoint = point;
                continue;
            }
            gc.strokeLine(lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY());
            lastPoint = point;
        }
        gc.setStroke(prevStrokeColor);
    }

    public void addPoint(Point2D point) {
        points.add(point);
    }

    public boolean hasPoints() {
        return !points.isEmpty();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("Drawable", "HandWriting");
        JSONArray ps = new JSONArray();
        for(Point2D p : points) {
            JSONObject pObj = new JSONObject();
            pObj.put("x", p.getX());
            pObj.put("y", p.getY());
            ps.add(pObj);
        }
        object.put("points", ps);
        object.put("strokeColor", strokeColor.toString());
        return object;
    }
}

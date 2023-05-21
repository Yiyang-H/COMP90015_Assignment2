package Draw;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class Drawable {
    protected Paint strokeColor;
    protected Paint prevStrokeColor;


    public void draw(GraphicsContext gc){
        prevStrokeColor = gc.getStroke();
        gc.setStroke(strokeColor);
    };

    public abstract JSONObject toJSONObject();
    public static Drawable parseDrawable(JSONObject object) {

        String type = (String) object.get("Drawable");
        Paint strokeColor = Paint.valueOf((String) object.get("strokeColor"));

        if(type.equals("Text")) {
            double x = (double) object.get("x");
            double y = (double) object.get("y");
            String text = (String) object.get("text");
            return new Text(x,y,text,strokeColor);
        }

        if(type.equals("HandWriting")) {
            JSONArray points = (JSONArray) object.get("points");
            HandWriting hw = new HandWriting(strokeColor);
            for(Object pObj : points) {
                double x = (double) ((JSONObject) pObj).get("x");
                double y = (double) ((JSONObject) pObj).get("y");
                hw.addPoint(new Point2D(x,y));
            }

            return hw;
        }

        double x1 = (double) object.get("x1");
        double x2 = (double) object.get("x2");
        double y1 = (double) object.get("y1");
        double y2 = (double) object.get("y2");


        switch (type) {
            case "Line":
                return new Line(x1,y1,x2,y2,strokeColor);
            case "Circle":
                return new Circle(x1,y1,x2,y2,strokeColor);
            case "Triangle":
                return new Triangle(x1,y1,x2,y2,strokeColor);
            case "Rectangle":
                return new Rectangle(x1,y1,x2,y2,strokeColor);
            default:
                System.out.println("Parsing drawable failed");
                break;
        }


        return null;
    }
}

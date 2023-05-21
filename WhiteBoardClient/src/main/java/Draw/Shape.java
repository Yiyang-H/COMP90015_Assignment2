package Draw;


import javafx.scene.paint.Paint;

public abstract class Shape extends Drawable {

    protected double x1;
    protected double y1;
    protected double x2;
    protected double y2;

    public Shape(double x1, double y1, double x2, double y2, Paint stokeColor) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.strokeColor = stokeColor;
    }

}

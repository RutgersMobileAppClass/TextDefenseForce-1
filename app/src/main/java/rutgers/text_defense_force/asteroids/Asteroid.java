package rutgers.text_defense_force.asteroids;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;

public class Asteroid {

    public enum AsteroidSpeed {
        SLOW(0.5), MEDIUM(1), FAST(1.5);
        private final double speed;

        AsteroidSpeed(double speed) {
            this.speed = speed;
        }

        public double getSpeed() {
            return speed;

        }
    }

    protected final PointF targetPoint;
    protected final AsteroidSpeed speed;
    protected final String word;
    protected final Bitmap bitmap;
    public float xCoordinate;
    public float yCoordinate;

    public Asteroid(float initialXCoordinate, float initialYCoordinate, PointF targetPoint,
                    AsteroidSpeed asteroidSpeed, String word, Bitmap bitmap) {

        if (targetPoint == null || bitmap == null || word == null || asteroidSpeed == null)
            throw new NullPointerException();
        if (word.equals("")) throw new IllegalArgumentException();

        this.targetPoint = targetPoint;
        this.speed = asteroidSpeed;
        this.word = word;
        this.bitmap = bitmap;
        this.xCoordinate = initialXCoordinate;
        this.yCoordinate = initialYCoordinate;
    }

    public synchronized RectF getAsteroidBounds() {
        float left = xCoordinate - ((float)bitmap.getWidth() / 2f);
        float top = yCoordinate - ((float)bitmap.getHeight() / 2f);
        float right = xCoordinate + ((float)bitmap.getWidth() / 2f);
        float bottom = yCoordinate + ((float)bitmap.getHeight() / 2f);
        return new RectF(left, top, right, bottom);
    }

    public synchronized void updateTrajectory() {

        double slopeY = targetPoint.y - yCoordinate;
        double slopeX = targetPoint.x - xCoordinate;
        double slope = slopeY / slopeX;
        double angularDirection = (float) Math.atan(slope);

        if (xCoordinate >= targetPoint.x) {
            xCoordinate = (float) ((double) xCoordinate - (speed.getSpeed() * Math.cos(angularDirection)));
            yCoordinate = (float) ((double) yCoordinate - (speed.getSpeed() * Math.sin(angularDirection)));

        } else {
            xCoordinate = (float) ((double) xCoordinate + (speed.getSpeed() * Math.cos(angularDirection)));
            yCoordinate = (float) ((double) yCoordinate + (speed.getSpeed() * Math.sin(angularDirection)));
        }

    }

    public synchronized Canvas drawAsteroid(Canvas canvas) {
        canvas.drawBitmap(bitmap, null, getAsteroidBounds(), new Paint());
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(TypedValue.COMPLEX_UNIT_SP * 25);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(word, xCoordinate - paint.measureText(word)/2f, yCoordinate + (bitmap.getHeight() / 1.2f), paint);
        return canvas;
    }
}
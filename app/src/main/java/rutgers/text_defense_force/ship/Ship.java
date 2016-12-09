package rutgers.text_defense_force.ship;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.EditText;

import rutgers.text_defense_force.GameView;
import rutgers.text_defense_force.R;
import rutgers.text_defense_force.asteroids.Asteroid;

public class Ship {

    protected final Bitmap scaledShip;
    protected final float xCoordinate;
    protected final float yCoordinate;

    protected String usersText;
    protected Bitmap paintedShip;

    protected EditText editText;

    public final Context context;

    public Ship(Context context, float xCoordinate, float yCoordinate) {

        this.context = context;

        editText = new EditText(this.context);

        if (context == null) throw new NullPointerException();
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        Bitmap unscaledShip = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.spaceship);

        scaledShip = GameView.scaleBitmapToScreen(unscaledShip, 12f, this.context);
        rotateShip(0f);
    }

    public void rotateShip(float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg,width,height,true);
        paintedShip = Bitmap.createBitmap(scaledShip, 0, 0, scaledShip.getWidth(),
                scaledShip.getHeight(), matrix, true);
    }

    public void rotateShipAtAsteroid(Asteroid asteroid) {

        double slopeY = asteroid.yCoordinate - yCoordinate;
        double slopeX = asteroid.xCoordinate - xCoordinate;
        double slope = slopeY / slopeX;

        double angularDirection = (float) Math.atan(slope);

        float angularDirectionDegrees = (float)(angularDirection * (180d / Math.PI));

        if (angularDirectionDegrees< 0f) {
            rotateShip(angularDirectionDegrees + 90f);
        } else {
            rotateShip(angularDirectionDegrees - 90f);
        }

    }

    public RectF getShipBounds() {
        float left = xCoordinate - ((float)scaledShip.getWidth() / 2f);
        float top = yCoordinate - ((float)scaledShip.getHeight() / 2f);
        float right = xCoordinate + ((float)scaledShip.getWidth() / 2f);
        float bottom = yCoordinate + ((float)scaledShip.getHeight() / 2f);
        return new RectF(left, top, right, bottom);
    }

    public void setUsersText(String usersText) {
        this.usersText = usersText;
    }

    public String getUsersText() {
        return usersText;
    }

    public synchronized Canvas drawShip(Canvas canvas) {
        canvas.drawBitmap(paintedShip, null, getShipBounds(), new Paint());
        if (usersText == null || usersText.isEmpty() || usersText.equals("")) return canvas;
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(TypedValue.COMPLEX_UNIT_SP * 25);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        //xCoordinate - paint.measureText(word)/2f, yCoordinate + (bitmap.getHeight() / 1.2f)
        canvas.drawText(usersText, xCoordinate - paint.measureText(usersText)/2f, yCoordinate, paint);
        return canvas;
    }
}
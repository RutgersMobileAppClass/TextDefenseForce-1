package rutgers.text_defense_force;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.View;

public class GameView extends View {

    protected GameController gameController;

    public GameView(Context context, GameController gameController) {
        super(context);
        this.gameController = gameController;
        setBackgroundResource(R.drawable.nebula);
    }

    public void updateGameView() {
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        gameController.getAsteroidField().drawField(canvas);
        gameController.getShip().drawShip(canvas);
    }

    public static Bitmap scaleBitmapToScreen(Bitmap unscaledBitmap, float scalingHeightRatio,
        Context context) {

        float bitmapWidth = unscaledBitmap.getWidth();
        float bitmapHeight = unscaledBitmap.getHeight();
        float bitmapAspectRatio = bitmapWidth/bitmapHeight;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenHeight = displayMetrics.heightPixels;

        float scaledBitmapHeight = screenHeight / scalingHeightRatio;
        float scaledBitmapWidth = scaledBitmapHeight * bitmapAspectRatio;

        return Bitmap.createScaledBitmap(unscaledBitmap, (int) scaledBitmapWidth,
                (int) scaledBitmapWidth, false);
    }
}
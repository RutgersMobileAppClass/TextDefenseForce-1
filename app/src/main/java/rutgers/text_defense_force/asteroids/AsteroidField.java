package rutgers.text_defense_force.asteroids;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;

import rutgers.text_defense_force.GameView;
import rutgers.text_defense_force.R;

public class AsteroidField {

    protected final ArrayList<Asteroid> asteroids = new ArrayList<>();
    protected final RectF targetBounds;
    protected final Context context;
    protected final Asteroid.AsteroidSpeed asteroidSpeed;
    protected final Bitmap scaledAsteroid1;
    protected final Bitmap scaledAsteroid2;
    protected final Bitmap scaledAsteroid3;

    public AsteroidField(Context context, RectF targetBounds,
        Asteroid.AsteroidSpeed asteroidSpeed) {

        if (context == null || targetBounds == null || asteroidSpeed == null)
            throw new NullPointerException();

        this.context = context;
        this.asteroidSpeed = asteroidSpeed;

        Bitmap unscaledAsteroid1 = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.asteroid1);
        scaledAsteroid1 = GameView.scaleBitmapToScreen(unscaledAsteroid1, 16f, context);

        Bitmap unscaledAsteroid2 = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.asteroid2);
        scaledAsteroid2 = GameView.scaleBitmapToScreen(unscaledAsteroid2, 16f, context);

        Bitmap unscaledAsteroid3 = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.asteroid3);
        scaledAsteroid3 = GameView.scaleBitmapToScreen(unscaledAsteroid3, 16f, context);

        this.targetBounds = targetBounds;

    }

    public synchronized void resetField() {
        asteroids.clear();
    }

    public synchronized ArrayList<Asteroid> updateTrajectories() {
        ArrayList<Asteroid> colliedAsteroids = new ArrayList<>();
        for (Asteroid asteroid : asteroids) {
                asteroid.updateTrajectory();
            if (targetBounds.intersect(asteroid.getAsteroidBounds()))
                colliedAsteroids.add(asteroid);
        }
        return colliedAsteroids;
    }

    public synchronized Canvas drawField(Canvas canvas) {
        for (Asteroid asteroid : asteroids) {
            canvas = asteroid.drawAsteroid(canvas);
        }
        return canvas;
    }

    public ArrayList<Asteroid> getAsteroids() {
        return asteroids;
    }

    public synchronized void spawnAnAsteroid(int asteroidImage, String asteroidWord) {
        Bitmap asteroidBitmap = null;
        switch (asteroidImage) {

            case 1:
                asteroidBitmap = scaledAsteroid1;
                break;
            case 2:
                asteroidBitmap = scaledAsteroid2;
                break;
            default:
                asteroidBitmap = scaledAsteroid3;
                break;
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenHeight = displayMetrics.heightPixels;
        float screenWidth = displayMetrics.widthPixels;

        float asteroidHeight = asteroidBitmap.getHeight();
        float asteroidWidth = asteroidBitmap.getWidth();

        int quadrant = generateRandomInteger(0,2);

        float minimumX = 0 - (asteroidWidth / 2f);
        float maximumX = screenWidth + (asteroidWidth / 2f);
        float minimumY = 0 - (asteroidHeight / 2f);
        float maximumY = (screenHeight + (asteroidHeight / 2f)) / 2f;

        float initialXCoordinate;
        float initialYCoordinate;

        switch(quadrant) {

            // Spawn from top
            case 0:

                initialXCoordinate = generateRandomInteger((int)minimumX, (int)maximumX);
                initialYCoordinate = minimumY;
                break;

            // Spawn from left
            case 1:

                initialXCoordinate = minimumX;
                initialYCoordinate = generateRandomInteger((int)minimumY, (int)maximumY);
                break;

            // Spawn from right
            default:

                initialXCoordinate = maximumX;
                initialYCoordinate = generateRandomInteger((int)minimumY, (int)maximumY);
                break;

        }

        PointF targetPoint = new PointF(targetBounds.centerX(), targetBounds.centerY());



        Asteroid asteroid = new Asteroid(initialXCoordinate, initialYCoordinate, targetPoint,
                asteroidSpeed,asteroidWord, asteroidBitmap);

        asteroids.add(asteroid);

    }

    private int generateRandomInteger(int min, int max) {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    public synchronized void removeAnAsteroid(Asteroid asteroid) {
        if (asteroid == null) throw new NullPointerException();
        if (asteroids.contains(asteroid)) asteroids.remove(asteroid);
    }

    public synchronized Asteroid doesTextMatchAsteroid(String word) {
        for (Asteroid asteroid : asteroids) {
            if (word.equals(asteroid.word)) return asteroid;
        }
        return null;
    }
}
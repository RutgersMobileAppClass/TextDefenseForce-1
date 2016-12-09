package rutgers.text_defense_force;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import rutgers.text_defense_force.asteroids.Asteroid;
import rutgers.text_defense_force.asteroids.AsteroidField;
import rutgers.text_defense_force.ship.Ship;
import rutgers.text_defense_force.word_list_service.cached_service_wrapper.CachedServiceWrapper;
import rutgers.text_defense_force.word_list_service.cached_service_wrapper.CachedServiceWrapperListener;

public class GameController implements CachedServiceWrapperListener {

    public enum GameDifficulty {
        EASY, MEDIUM, HARD;
    }

    protected final Activity parentActivity;
    protected final int frameRate;
    protected final GameDifficulty gameDifficulty;
    protected final CachedServiceWrapper cachedServiceWrapper;
    protected final AsteroidField asteroidField;
    protected final Ship ship;
    protected final GameView gameView;
    protected final EditText editText;
    protected Timer gameFrameTimer;
    protected boolean firstSpawn = true;
    protected int score = 0;

    public GameController(final Activity activity, int frameRate, GameDifficulty gameDifficulty,
                          final EditText editText) {

        // Store the parent activity, the frame rate, and the game difficulty
        parentActivity = activity;
        this.frameRate = frameRate;
        this.gameDifficulty = gameDifficulty;
        this.editText = editText;

        // Start the cache service wrapper to obtain words from the text file
        cachedServiceWrapper = new CachedServiceWrapper(parentActivity, this, 5);

        // Create the ship and set it to the middle of the screen
        DisplayMetrics metrics = parentActivity.getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;
        float centerXCoordinate = screenWidth/2;
        float centerYCoordinate = screenHeight/2;
        ship = new Ship(parentActivity, centerXCoordinate, centerYCoordinate);

        // Create the asteroid field
        asteroidField = new AsteroidField(parentActivity, ship.getShipBounds(),
                getSpeedFromDifficulty());

        // Create the game view
        gameView = new GameView(parentActivity, this);

        // Create the game frame rate timer
        int milliSecondFrameRate = 1000 / frameRate;
        gameFrameTimer = new Timer();
        gameFrameTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                // Update the asteroids trajectories
                ArrayList<Asteroid> collisions = asteroidField.updateTrajectories();

                // If there are no asteroids in play, we can return
                if (asteroidField.getAsteroids().isEmpty()) return;

                // If any of the asteroids collided with the ship
                if (!collisions.isEmpty()) {

                    // Reset the game
                    resetGame();

                // Otherwise, update the game view to reflect the new asteroid positions
                } else {
                    parentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gameView.updateGameView();
                        }
                    });
                }
            }
        }, 0, milliSecondFrameRate);

        // Reset the game
        resetGame();
    }

    protected void resetGame() {

        firstSpawn = true;

        // Reset the asteroid field
        asteroidField.resetField();

        // Set the users score to zero
        score = 0;

        // Prepare the game for a new round
        prepareForNewRound();
    }

    protected void prepareForNewRound() {
        try {
            Toast.makeText(parentActivity.getApplicationContext(), "Score: " + score,
                    Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {

        }
        clearUserText();

        if (firstSpawn) {
            generateAnAsteroid();
            generateAnAsteroid();
            generateAnAsteroid();
            generateAnAsteroid();
            generateAnAsteroid();
            firstSpawn = false;
        } else {
            generateAnAsteroid();
        }
    }

    protected void clearUserText() {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Clear the entered text
                editText.setText("");
                ship.setUsersText("");
            }
        });
    }

    protected void generateAnAsteroid() {
        String word = cachedServiceWrapper.requestWord();
        if (word == null) word = "Go!";
        asteroidField.spawnAnAsteroid((int)(Math.random() * 3) + 1, word);
    }

    public void keyboardTextObtained(String text) {
        ship.setUsersText(text);

        Asteroid asteroid = asteroidField.doesTextMatchAsteroid(text);
        if (asteroid != null) {
            ship.rotateShipAtAsteroid(asteroid);
            asteroidField.removeAnAsteroid(asteroid);
            score = score + 1;
            prepareForNewRound();
        }
    }

    public GameView getGameView() {
        return gameView;
    }

    public AsteroidField getAsteroidField() {
        return asteroidField;
    }

    public Ship getShip() {
        return ship;
    }

    public int getScore() {
        return score;
    }

    protected Asteroid.AsteroidSpeed getSpeedFromDifficulty() {
        if (gameDifficulty.equals(GameDifficulty.EASY)) return Asteroid.AsteroidSpeed.SLOW;
        if (gameDifficulty.equals(GameDifficulty.MEDIUM)) return Asteroid.AsteroidSpeed.MEDIUM;
        return Asteroid.AsteroidSpeed.FAST;
    }

    // This method is called when there is a failure in the service. This shouldn't really happen
    // but just in case, we will display a toast message
    public void serviceFailure() {
        Toast.makeText(parentActivity.getApplicationContext(), "Service failure detected! ",
                Toast.LENGTH_SHORT).show();
    }

    // This method is called from the cached service wrapper when a word was requested (via our
    // button) but there were no words in the cache, so it had to open the file and fetch a word
    public void wordObtained(String word) {

    }
}
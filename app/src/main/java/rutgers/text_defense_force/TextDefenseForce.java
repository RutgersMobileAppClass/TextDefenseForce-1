package rutgers.text_defense_force;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextDefenseForce extends AppCompatActivity {

    protected GameController gameController;
    protected EditText editText;
    protected KeyboardListener keyboardListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_defense_force);

        editText = (EditText) findViewById(R.id.editText);
        gameController = new GameController(this, 30, GameController.GameDifficulty.MEDIUM, editText);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.gameView);
        linearLayout.addView(gameController.getGameView());
        bindKeyboardToEditText();

    }

    protected void bindKeyboardToEditText() {


        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        keyboardListener = new KeyboardListener();
        editText.addTextChangedListener(keyboardListener);

    }

    protected class KeyboardListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after) {
            String text = s.toString();
            gameController.keyboardTextObtained(text);
        }
    }
}
package edu.sdsmt.group4;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    /**
     * Name of bundle sent to external activates
     */
    public static final String INTENTPARAMETERS = "game";

    /**
     * Constant string use to save and restore parameters to a bundle
     */
    private final static String PARAMETERS = "PARAM";
    /**
     * View where the game takes place
     */
    private GameView gameView;

    /**
     * The game state
     */
    protected Game game;

    private Cloud cloud;

    private Parameters params = new Parameters();

    /**
     * Constructor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        gameView = this.findViewById(R.id.gameView);
        cloud = new Cloud();

        if (savedInstanceState != null) {
            params = (Parameters) savedInstanceState.getSerializable(PARAMETERS);
            game = new Game(gameView, params.LocalPlayer);
        } else {
            params.LocalPlayer = getIntent().getStringExtra(WaitActivity.PLAYERNAME);
            game = new Game(gameView, params.LocalPlayer);
            game.addCollectibles(gameView.getContext());
            Random rnd = new Random();
            game.addPlayer(gameView.getContext(), params.LocalPlayer, (float) (0.1 + (0.9 - 0.1) * rnd.nextDouble()), (float) (0.1 + (0.9 - 0.1) * rnd.nextDouble()), rnd.nextInt(), 0);
            cloud.saveToCloud(game);
            game.updateTimestamp();
            setRounds();
        }

        gameView.setGame(game);
        setBackButtonCallback();
        cloud.gameStateChangeListener(game, gameView);
        updateUI();
    }

    private void setRounds() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ask_for_rounds));

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                game.setNumberOfRounds(Integer.parseInt(input.getText().toString()));
                cloud.saveToCloud(game);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void endGame() {
        game.cancelTimer();
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.WINNER, game.getWinner());
        intent.putExtra(ResultActivity.SCORE, game.getPlayerPoints());
        intent.putExtra(ResultActivity.USERNAME, game.getLocalPlayer());
        startActivity(intent);
        finish();
    }

    public static class Parameters implements Serializable {
        public String LocalPlayer;
    }

    /**
     * Handle the back button being pressed to return to log in activity
     */
    void setBackButtonCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event by returning to log in activity
                Intent intent = new Intent(gameView.getContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * Creates menu options
     * @param menu the menu view
     * @return true if created successfully, false otherwise
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_play, menu);
        return true;
    }

    /**
     * Handles when one of the menu options is selected
     * @param item which menu item was selected
     * @return true if handled, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu_exit) {
             onExit();
             return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when menu item "Exit Game" is pressed
     */
    public void onExit() {
        cloud.leaveGame();
        game.cancelTimer();
        Intent intent = new Intent(this, EarlyExitActivity.class);
        intent.putExtra(EarlyExitActivity.USERNAME, params.LocalPlayer);
        startActivity(intent);
        finish();
    }

    /**
     * Handle a Capture button press
     *
     * @param view the view
     */
    public void onCapture(View view) {
        cloud.saveToCloud(game);
        game.updateTimestamp();
        Intent intent = new Intent(this, CaptureActivity.class);
        Bundle bundle = new Bundle();
        onSaveInstanceState(bundle);
        intent.putExtra(INTENTPARAMETERS, bundle);
        startActivity(intent);
    }

    /**
     * Function called when we need to save the state of the activity
     *
     * @param bundle the bundle where the data is saved
     */
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putSerializable(PARAMETERS, params);
    }

    /**
     * Set the values of the player turn and round info textViews
     */
    public void updateUI() {

        TextView playersTurnInfo = this.findViewById(R.id.textViewPlayersTurn);
        TextView roundInfo = this.findViewById(R.id.textViewRounds);

        this.findViewById(R.id.constraint).setBackgroundColor(game.getPlayerColor());

        String str;

        if(game.getCurrentPlayer().getName().equals(params.LocalPlayer))
            str = getString(R.string.your_turn_label);
        else
            str = game.getPlayerTurnInfo() + getString(R.string.textViewPLayersTurn);
        playersTurnInfo.setText(str);

        str = getString(R.string.textViewRounds) + game.getRound() + "/" + game.getNumberOfRounds();
        roundInfo.setText(str);

        if(game.getMovable() == null)
            this.findViewById(R.id.capture_btn).setEnabled(false);
        else
            this.findViewById(R.id.capture_btn).setEnabled(true);
    }
}

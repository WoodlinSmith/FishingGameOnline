package edu.sdsmt.group4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import static edu.sdsmt.group4.PlayActivity.INTENTPARAMETERS;

public class CaptureActivity extends AppCompatActivity {

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

    private PlayActivity.Parameters params = new PlayActivity.Parameters();

    /**
     * Constructor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        gameView = this.findViewById(R.id.captureView);
        cloud = new Cloud();

        if (savedInstanceState != null)
            params = (PlayActivity.Parameters) savedInstanceState.getSerializable(PARAMETERS);
        else
            params = (PlayActivity.Parameters) getIntent().getBundleExtra(INTENTPARAMETERS).getSerializable(PARAMETERS);

        game = new Game(gameView, params.LocalPlayer);
        gameView.setGame(game);

        cloud.gameStateChangeListener(game, gameView);
        toolsOptions();
    }

    /**
     * Handle the back button press
     */
    public void onBack(View view) { finish(); }

    /**
     * Handle the confirm button press
     */
    public void onConfirm(View view) {
        //check if items overlap
        game.attemptCatch();
        game.nextTurn();

        cloud.saveToCloud(game);
        game.updateTimestamp();
        finish();
    }

    /**
     * Function called when we need to save the state of the activity
     *
     * @param bundle the bundle where the data is saved
     */
    @Override
    protected void onSaveInstanceState (Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putSerializable(PARAMETERS, params);
    }

    /**
     * Set up the spinner
     */
    public void toolsOptions() {
        this.findViewById(R.id.constraint).setBackgroundColor(game.getPlayerColor());

        Spinner tools = findViewById(R.id.select_type_spinner);

        List<String> choices = new ArrayList<>();
        choices.add("Round Net");
        choices.add("Rectangular Net");
        choices.add("Spear");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tools.setAdapter(adapter);

        tools.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int pos, long id) {
                CaptureTool captureTool;

                if(game.getCurrentPlayer() != null) {
                    if (pos == 0)
                        captureTool = new RoundNet(gameView.getContext(), R.drawable.fishing_net_round, game.getCurrentPlayer().getX(), game.getCurrentPlayer().getY());
                    else if (pos == 1)
                        captureTool = new RectangleNet(gameView.getContext(), R.drawable.fishing_net, game.getCurrentPlayer().getX(), game.getCurrentPlayer().getY());
                    else
                        captureTool = new Spear(gameView.getContext(), R.drawable.harpoon, game.getCurrentPlayer().getX(), game.getCurrentPlayer().getY());

                    game.setCaptureTool(captureTool);
                    gameView.invalidate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                game.setCaptureTool(new RoundNet(gameView.getContext(),R.drawable.fishing_net_round, game.getCurrentPlayer().getX(), game.getCurrentPlayer().getY()));
            }
        });
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

    public void endGameEarly() {
        game.cancelTimer();
        Intent intent = new Intent(this, EarlyExitActivity.class);
        intent.putExtra(EarlyExitActivity.USERNAME, params.LocalPlayer);
        startActivity(intent);
        finish();
    }
}
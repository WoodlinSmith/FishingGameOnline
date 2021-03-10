package edu.sdsmt.group4;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    public static final String WINNER = "winner";
    public static final String SCORE = "score";
    public static  final String USERNAME = "username";

    private String username;

    private TextView winnerText;
    private TextView playerPoints;
    /**
     * Constructor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        winnerText = this.findViewById(R.id.winner_text);
        playerPoints = this.findViewById(R.id.player_points);


        if(savedInstanceState != null) {
            winnerText.setText(savedInstanceState.getString(WINNER));
            playerPoints.setText(savedInstanceState.getString(SCORE));
            username = savedInstanceState.getString(USERNAME);
        }
        else {
            winnerText.setText(getIntent().getStringExtra(WINNER));
            playerPoints.setText(getIntent().getStringExtra(SCORE));
            username = getIntent().getStringExtra(USERNAME);
        }
    }

    /**
     * Handle a restart button press
     * @param view restart button
     */
    public void onRestart(View view){
        Intent intent = new Intent(this, WaitActivity.class);
        intent.putExtra(WaitActivity.PLAYERNAME, username);
        startActivity(intent);
        finish();
    }

    /**
     * Function called when we need to save the state of the activity
     *
     * @param bundle the bundle where the data is saved
     */
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(WINNER, winnerText.getText().toString());
        bundle.putString(SCORE, playerPoints.getText().toString());
        bundle.putString(USERNAME, username);
    }
}

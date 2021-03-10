package edu.sdsmt.group4;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class WaitActivity extends AppCompatActivity {

    public static final String PLAYERNAME = "playername";
    public static final String ROUNDSPARAM = "Rounds";

    private String playername = "";
    private int rounds = 0;

    /**
     * Get player1 name from the information passed into this activity. Then wait for second player
     * to log in.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        playername = Objects.requireNonNull(getIntent().getExtras()).getString(PLAYERNAME, "");

        Cloud cloud = new Cloud();
        cloud.joinGame();
        cloud.secondPlayerListener(this);
    }

    /**
     * Proceeds to PlayActivity with all the necessary information once the second player logs in.
     */
    public void onSecondPlayerLogin() {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra(WaitActivity.PLAYERNAME, playername);
        //NOTE this is where the number of rounds are specified
        intent.putExtra(WaitActivity.ROUNDSPARAM, 3);
        startActivity(intent);
        finish();
    }
}

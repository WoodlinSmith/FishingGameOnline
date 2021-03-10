package edu.sdsmt.group4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class EarlyExitActivity extends AppCompatActivity {

    public static  final String USERNAME = "username";

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_early_exit);

        if(savedInstanceState != null)
            username = savedInstanceState.getString(USERNAME);
        else
            username = getIntent().getStringExtra(USERNAME);
    }

    /**
     * Starts a new game when the "Play Again" button is pressed
     * @param view Play Again button
     */
    public void onPlayAgain(View view) {
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
        bundle.putString(USERNAME, username);
    }
}

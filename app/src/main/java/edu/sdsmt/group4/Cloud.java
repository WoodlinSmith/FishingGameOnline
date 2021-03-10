package edu.sdsmt.group4;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Cloud {

    private final FirebaseDatabase database = FirebaseInit.getDb();
    private final DatabaseReference gameStateRef = database.getReference("GameState");
    private final DatabaseReference userRef = gameStateRef.child("LoggedIn");
    private final FirebaseAuth authToken = FirebaseAuth.getInstance();
    private final FirebaseUser currUser = authToken.getCurrentUser();

    /**
     * Adds a lister that will update the game state whenever there is a change in the database
     *
     * @param game The game that will be updated
     * @param view The view we are loading the game into
     */
    public void gameStateChangeListener(final Game game, final GameView view) {

        gameStateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                game.loadJSON(dataSnapshot);
                view.invalidate();

                if (game.isGameOver()) {
                    gameStateRef.removeEventListener(this);
                    endGame(view);
                    clearGame();
                } else if (dataSnapshot.child("LoggedIn").getChildrenCount() < 2) {
                    gameStateRef.removeEventListener(this);
                    endGameEarly(view);
                    clearGame();
                } else if (view.getContext() instanceof PlayActivity) {
                    ((PlayActivity) view.getContext()).updateUI();
                } else if (view.getContext() instanceof CaptureActivity) {
                    ((CaptureActivity) view.getContext()).toolsOptions();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(view.getContext(), R.string.gameState_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Notify the activity that the game has ended
     *
     * @param view the view displaying the game
     */
    private void endGame(GameView view) {
        if (view.getContext() instanceof PlayActivity)
            ((PlayActivity) view.getContext()).endGame();

        if (view.getContext() instanceof CaptureActivity)
            ((CaptureActivity) view.getContext()).endGame();
    }

    /**
     * Notify the activity that the game has ended early
     *
     * @param view the view displaying the game
     */
    private void endGameEarly(GameView view) {
        if (view.getContext() instanceof PlayActivity)
            ((PlayActivity) view.getContext()).onExit();

        if (view.getContext() instanceof CaptureActivity)
            ((CaptureActivity) view.getContext()).endGameEarly();
    }

    /**
     * Clear the gameState in the database
     */
    public void clearGame() {
        gameStateRef.removeValue();
        DatabaseReference timeoutRef = FirebaseInit.getDb().getReference("Timestamps");
        timeoutRef.removeValue();
    }

    /**
     * Save the current gameState to the database.
     *
     * @param game game that will be saved
     */
    public void saveToCloud(Game game) {
        game.saveJSONObject(gameStateRef);
    }

    /**
     * Remove the player from the game
     */
    public void leaveGame() {
        userRef.child(currUser.getUid()).removeValue();
    }

    /**
     * Listen for a second player to join the game and notify the wait activity
     *
     * @param activity The wait activity
     */
    public void secondPlayerListener(final WaitActivity activity) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 1) {
                    activity.onSecondPlayerLogin();
                    userRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(activity, R.string.secondPlayer_fail, Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });
    }

    /**
     * Join the game
     */
    public void joinGame() {
        userRef.child(currUser.getUid()).setValue(true);
    }
}

/*
Project 2 Grading
Firebase login name: woodlin.smith.dev@gmail.com
Firebase login password: Fx24&yRT

Time out period: 60 seconds
how to reset database: On the login page, click the menu button. There will be an option to reset the
database, just click that and everything except the Users branch will be removed.
Reminder: Mark where the timeout period is set with GRADING: TIMEOUT


Group:

X 4pt Database reset
X 9pt New user activity
X 9pt Opening/log in activity
X 2pt Wait activity exists
X 7pt Log in with preferences
X 4pt Loss of internet
X 5pt rotation

Individual:

	Sequencing - Erica Keeble
		X 5pt register
		X 20pt game entry, waiting, main game play
		X 15pt end/early exits
		X 5pt active exit menu
		X 5pt rotation


	Upload - Woodlin Smith

		X 10pt intial setup
		X 5pt waiting
		X 15pt store game state
		X 15pt notify end/early exits
		X 5pt rotation


	Download - Caelan Klein

		X 10pt intial setup
		X 5pt waiting
		X 15pt store game state
		X 15pt grab and forward end/early exits
		X 5pt rotation


	Monitor Waiting - NA
		NA 15pt inital setup
		NA 12pt Uploading the 3 state
		NA 12pt Downloading the 3 state
		NA 6pt UI update
		NA 5pt rotation

Please list any additional rules that may be needed to properly grade your project:
If you force close the app (swipe it away) in the middle of a game, please be sure to hit the reset
menu option on the login screen when you open it away. Exiting the game uncleanly like that doesn't
allow the database and authentication to get handled, and causes very weird behavior once you log in.
Also, a snapshot of the DB state is being stored in the folder labeled db_snapshot at the root level
of our project.
 */

package edu.sdsmt.group4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String USERID_KEY = "userid";
    private static final String PASSWORD_KEY = "password";
    private int CREATE_USER_ACTIVITY = 1;
    public static final String SUCCESSFUL_USER_CREATION = "user_created_successfully";

    private FirebaseAuth authToken=FirebaseAuth.getInstance();
    private FirebaseUser currUser;
    private FirebaseDatabase database=FirebaseInit.getDb();
    private DatabaseReference loginRef=database.getReference("GameState/LoggedIn");
    private LoginVerified loginVer=new LoginVerified();

    private static final String RESET_EMAIL="hiddenemail@email.com";
    private static final String RESET_PASSWORD="hidden";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //if the user exited the app, they should not still be signed in.
        if(authToken.getCurrentUser()!=null)
        {
            authToken.signOut();
        }

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.contains(USERID_KEY)) {
            ((EditText) findViewById(R.id.editTextEmail1)).setText(sharedPref.getString(USERID_KEY, ""));
            ((CheckBox) findViewById(R.id.checkSaveInfo)).setChecked(true);
        }

        if(sharedPref.contains(PASSWORD_KEY))
            ((EditText)findViewById(R.id.editTextPassword)).setText(sharedPref.getString(PASSWORD_KEY, ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_help:
                onHelp();
                return true;

            case R.id.menu_reset:
                onResetDatabase();
                return true;

        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Called when the "Reset Database" menu item is selected. Signs in a hidden user (not in DB), so
     * they will have write permissions. This will let them clear the game.
     */
    private void onResetDatabase() {
        Task<AuthResult> result=authToken.signInWithEmailAndPassword(RESET_EMAIL,RESET_PASSWORD);
        result.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Cloud cloud = new Cloud();
                    cloud.clearGame();
                }
            }
        });
    }

    /**
     * Called when the Help menu item is pressed. The game instructions pop up with a close button.
     */
    private void onHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_HelpText);
        builder.setCancelable(true);

        builder.setPositiveButton(
                R.string.close,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Called when Login button is pressed. Gets the username and password from the view, validates
     * login credentials, and logs the user in. If invalid credentials, a toast is displayed which
     * says to try again or create a new user.
     * @param view login button
     */
    public void onLogin(View view) {

        if(authToken.getCurrentUser()!=null)
        {
            authToken.signOut();
        }
        String email = ((EditText) findViewById(R.id.editTextEmail1)).getText().toString();
        String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();

        //if login is valid, add this player to the "logged in" branch in database
        login(email, password);

    }

    /**
     * Called when the "Save Login Info" checkbox is checked
     * @param view the checkbox viewR
     */
    public void onSaveLoginInfo(View view) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        if(((CheckBox) findViewById(R.id.checkSaveInfo)).isChecked()) {
            String email = ((EditText) findViewById(R.id.editTextEmail1)).getText().toString();
            String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(USERID_KEY, email);
            editor.putString(PASSWORD_KEY, password);
            editor.apply();
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
        }
    }

    /**
     * Adds player to logged-in branch in the database
     * @param email what was entered into the username edit text view
     * @param password what was entered into the password edit text view
     * return username of the user with that email and password
     */
    private void login(final String email, String password) {
        if(email.isEmpty() || password.isEmpty())
            return;

        Task<AuthResult> result = authToken.signInWithEmailAndPassword(email,password);
        result.addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {

                    Log.d("LoginSuccess", "signInWithEmail:onComplete:" + task.isSuccessful());
                    //Mark the user as logged in
                    currUser=authToken.getCurrentUser();
                    loginRef.child(currUser.getUid()).setValue(true);
                    loginVer.setCredentialsVerified(true);
                    if(loginVer.credentialsVerified())
                    {
                        Log.d("LoginCallback", "Callback set");
                    }

                    onSuccessfulSignIn(currUser.getUid());



                } else {
                    Log.e("LoginFail", "signInWithEmail:failed", task.getException());
                    Toast.makeText(findViewById(R.id.ConstraintLayout).getContext(),
                            R.string.failed_login_toast, Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    /**
     * Called when the Create New User button is pressed. Takes you to the CreateUserActivity.
     * @param view Create new user button
     */
    public void onCreateNewUser(View view) {
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivityForResult(intent, CREATE_USER_ACTIVITY);
    }

    /**
     * Called when the user signs in, grabs their screen name.
     * @param Uid user id
     */
    private void onSuccessfulSignIn(final String Uid)
    {
        final Intent intent = new Intent(this, WaitActivity.class);

        final DatabaseReference ref = database.getReference("Users/" + Uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                intent.putExtra(WaitActivity.PLAYERNAME, dataSnapshot.child("username").getValue(String.class));
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SignInError","Failed to sign in");
            }
        });

    }

    /**
     * On return from CreateUserActivity, if the user was created successfully, show a toast.
     * Otherwise, don't show anything (because that means the user hit the return button).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CREATE_USER_ACTIVITY && resultCode== Activity.RESULT_OK)
        {
            if(data != null && !Objects.requireNonNull(data.getExtras()).getBoolean(SUCCESSFUL_USER_CREATION))
                Toast.makeText(findViewById(R.id.ConstraintLayout).getContext(),
                        R.string.successful_user_create_toast, Toast.LENGTH_LONG).show();
        }


    }
}

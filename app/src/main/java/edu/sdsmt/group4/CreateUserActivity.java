package edu.sdsmt.group4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class CreateUserActivity extends AppCompatActivity {

    FirebaseAuth userAuth= FirebaseAuth.getInstance();
    FirebaseUser currUser;
    DatabaseReference userRef= FirebaseInit.getDb().getReference("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
    }

    /**
     * Called when Create User button is pressed. Creates user. If successful, return to log in
     * activity. Otherwise, remain in activity.
     * @param view Create User button
     */
    public void onCreateUser(View view) {

        //if the create user is successful, return to login activity
        createUser();
    }

    /**
     * Gets information entered into the view and checks that the passwords match. Enters the
     * information into the database as a new user. Displays toast if passwords do not match.
     */
    private void createUser() {
        final String playerName = ((EditText) findViewById(R.id.editTextUsername)).getText().toString();
        final String email = ((EditText) findViewById(R.id.editTextEmail1)).getText().toString();
        final String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();
        final String passwordConfirm = ((EditText) findViewById(R.id.editTextConfirmPass)).getText().toString();

        //if passwords do not match, display toast and return false
        if(!password.equals(passwordConfirm)) {
            Toast.makeText(findViewById(R.id.ConstraintLayout).getContext(),
                    R.string.passwords_dont_match_toast, Toast.LENGTH_LONG).show();
            return;
        }

        if(email.isEmpty() || password.isEmpty())
            return;

        Task<AuthResult> result = userAuth.createUserWithEmailAndPassword(email, password);
        result.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task task) {
                currUser= userAuth.getCurrentUser();
                if (task.isSuccessful()||currUser != null) {
                    Log.d("Success", "createUserWithEmail:onComplete:" + task.isSuccessful());

                    //Add data items to the result
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("id", currUser.getUid());
                    result.put("password", password);
                    result.put("email", email);
                    result.put("username", playerName);


                    userRef.child(currUser.getUid()).setValue(result);

                    //Move back to the sign in screen.
                    Intent intent = new Intent();
                    intent.putExtra(LoginActivity.SUCCESSFUL_USER_CREATION, true);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });

        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(findViewById(R.id.ConstraintLayout).getContext(),
                        e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Called when the Return button is pressed. Returns to the LoginActivity
     * @param view return button
     */
    public void onReturn(View view) {
        finish();
    }
}

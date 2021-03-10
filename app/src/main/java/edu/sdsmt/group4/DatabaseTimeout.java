package edu.sdsmt.group4;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class DatabaseTimeout {

    //GRADING: TIMEOUT
    private static int DB_TIMEOUT=60000;
    private boolean timedOut=false;
    long time_cutoff;

    DatabaseTimeout()
    {
        updateCutoff();
    }

    /**
     * Updates the time cutoff to be 1 minute in the past
     */
    void updateCutoff()
    {
       time_cutoff = new Date().getTime()-DB_TIMEOUT;
    }

    /**
     * Queries the database for any timestamps older then a minute. If it finds one, there was
     * A timeout. It will then move to the EarlyExitActivity
     * @param view
     */
    void setQuery(final View view)
    {
        updateCutoff();
        final Query updatedRef=FirebaseInit.getDb().getReference("Timestamps/players")
                .orderByChild("timestamp").endAt(time_cutoff);
        updatedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    if(snapshot.hasChild("timestamp")&&!isTimedOut()) {
                        Log.d("TimedOut", "A player timed out!");

                        timedOut(true);

                        //Move to early exit.
                        if (view.getContext() instanceof PlayActivity) {
                            ((PlayActivity) view.getContext()).onExit();
                        }
                        if (view.getContext() instanceof CaptureActivity) {
                            ((CaptureActivity) view.getContext()).endGameEarly();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TimedOut","Timeout query failed");
            }
        });

    }

    public void timedOut(boolean timeoutVal)
    {
        this.timedOut=timeoutVal;
    }

    public boolean isTimedOut() {
        return timedOut;
    }
}

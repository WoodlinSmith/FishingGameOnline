package edu.sdsmt.group4;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseInit {
    private static FirebaseDatabase db;

    /**
     * Inits the database as a singleton
     * @return the database
     */
    public static FirebaseDatabase getDb()
    {
        if(db==null)
        {
            db=FirebaseDatabase.getInstance();
            db.setPersistenceEnabled(true);
        }
        return db;
    }
}

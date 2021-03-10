package edu.sdsmt.group4;

import android.content.Context;

public class Fish extends GamePiece {

    /**
     * GamePiece constructor
     * @param xPos starting x location as a normalized coordinate (0 to 1)
     * @param yPos starting y location as a normalized coordinate (0 to 1)
     *
     * Relative x and y locations in the range 0-1 are used for the center
     * of the game piece.
     */
    public Fish(Context context, int bitmapId, float xPos, float yPos){
        super(context, bitmapId, xPos, yPos);
        scale = (float) 0.25;
    }
}

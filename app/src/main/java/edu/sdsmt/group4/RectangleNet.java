package edu.sdsmt.group4;

import android.content.Context;

public class RectangleNet extends CaptureTool {

    /**
     * Constructor
     *
     * @param context view context
     * @param bitmapId resource id of the bitmap used to draw the piece
     * @param xPos starting x location as a normalized coordinate (0 to 1)
     * @param yPos starting y location as a normalized coordinate (0 to 1)
     *
     * Relative x and y locations in the range 0-1 are used for the center
     * of the game piece.
     */

    RectangleNet(Context context, int bitmapId, float xPos, float yPos) {
        super(context, bitmapId, xPos, yPos);
        canRotate = false;
        canScale = true;
        restrictMovement = false;
        applyProbability = true;
    }
}
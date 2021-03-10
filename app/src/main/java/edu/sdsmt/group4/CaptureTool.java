package edu.sdsmt.group4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public class CaptureTool extends GamePiece {

    private int toolChoice = 0;
    private Paint paint = new Paint();

    CaptureTool(Context context, int bitmapId, float xPos, float yPos){super(context, bitmapId,xPos, yPos); }

    @Override
    public void draw(Canvas canvas, int marginX, int marginY, int gameSize) {
        super.draw(canvas, marginX, marginY, gameSize);
    }

    @Override
    public boolean hit(float testX, float testY, int puzzleSize, float scaleFactor) {
        // Make relative to the location and size to the piece size
        int pX = (int) ((testX - x) * puzzleSize / scaleFactor) + bitmap.getWidth() / 2;
        int pY = (int) ((testY - y) * puzzleSize / scaleFactor) + bitmap.getHeight() / 2;

        return pX >= 0 && pX < bitmap.getWidth() &&
                pY >= 0 && pY < bitmap.getHeight();

        // We are within the rectangle of the piece.
        // Are we touching actual picture?
//(piece.getPixel(pX, pY) & 0xff000000) != 0;
    }

}
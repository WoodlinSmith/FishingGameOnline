package edu.sdsmt.group4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import java.io.Serializable;

public abstract class GamePiece implements Serializable {

    /**
     * Is the piece allowed to rotate
     */
    protected boolean canRotate = false;
    /**
     * Is the piece allowed to scale
     */
    protected boolean canScale = false;
    /**
     * Is the pieces movement restricted
     */
    protected boolean restrictMovement = false;
    /**
     * x location.
     * We use relative X locations in the range 0-1 for the center
     * of the game piece.
     */
    protected float x = 0;
    /**
     * y location
     */
    protected float y = 0;
    /**
     * Angle in which to rotate the piece when drawn
     */
    private float angle = 0;
    /**
     * Scale factor applied to the piece when drawn
     */
    protected float scale = 1;
    /**
     * Original x position of the piece when its turn begins
     */
    private float origX = 0;
    /**
     * Original y position of the piece when its turn begins
     */
    protected float origY = 0;

    /**
     * Checks if the piece should catch only one fish
     */
    protected boolean catchOnlyOne = false;

    /**
     * Checks if you should apply probability to a tool/piece
     */
    protected boolean applyProbability = false;

    /**
     * Resource id of the bitmap
     */

    private int bitmapId;

    /**
     * The image used to draw the piece.
     */
    protected transient Bitmap bitmap;

    /**
     * The image for translated piece
     */
    protected transient Bitmap movedPiece;

    /**
     * GamePiece constructor
     *
     * @param context view context
     * @param bitmapId resource id of the bitmap used to draw the piece
     * @param xPos starting x location as a normalized coordinate (0 to 1)
     * @param yPos starting y location as a normalized coordinate (0 to 1)
     *
     * Relative x and y locations in the range 0-1 are used for the center
     * of the game piece.
     */
    public GamePiece(Context context, int bitmapId, float xPos, float yPos){
        this.bitmapId = bitmapId;
        bitmap = BitmapFactory.decodeResource(context.getResources(), this.bitmapId);
        x = xPos;
        y = yPos;
        setOrigLoc();
    }

    /**
     * Get the x position of the piece
     */
    public float getX() {
        return x;
    }

    /**
     * Get the y position of the piece
     */
    public float getY() {
        return y;
    }

    /**
     * Get the x position of the piece at the beginning of the turn
     */
    public float getOrigX() { return origX; }

    /**
     * Get the y position of the piece at the beginning of the turn
     */
    public float getOrigY() { return origY; }

    /**
     * Get the scale factor
     */
    public float getScale() { return scale; }

    /**
     * Get weather movement is restricted for he piece
     */
    public boolean isMovementRestricted() {
        return restrictMovement;
    }

    /**
     * Draw the Capture Object
     * @param canvas Canvas we are drawing on
     * @param marginX Margin X value in pixels
     * @param marginY Margin Y value in pixels
     * @param gameSize Size of the game area in pixels
     */
    public void draw(Canvas canvas, int marginX, int marginY, int gameSize) {

        if (bitmap == null)
            return;

        canvas.save();

        // Convert x,y to pixels and add the margin, then draw
        canvas.translate(marginX + x * gameSize, marginY + y * gameSize);

        //scaleFactor = scale; // * SCALE_IN_VIEW_CAPTURE_OBJECT;

        // Scale it to the right size
        movedPiece = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*scale), (int)(bitmap.getHeight()*scale), true);

        //rotate the image
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        movedPiece = Bitmap.createBitmap(movedPiece, 0, 0, movedPiece.getWidth(), movedPiece.getHeight(), matrix, true);

        // This magic code makes the center of the piece at 0, 0
        canvas.translate(-movedPiece.getWidth() / 2, -movedPiece.getHeight() / 2);

        // Draw the player
        canvas.drawBitmap(movedPiece, 0, 0, null);
        canvas.restore();
    }


    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    /**
     * Test to see if we have touched a game piece
     *
     * @param testX       x location as a normalized coordinate (0 to 1)
     * @param testY       y location as a normalized coordinate (0 to 1)
     * @param puzzleSize  the size of the game area in pixels
     * @param scaleFactor the amount to scale a piece by
     * @return true if we hit the piece
     */
    public boolean hit(float testX, float testY, int puzzleSize, float scaleFactor) {
        // Make relative to the location and size to the piece size
        int pX = (int) ((testX - x) * puzzleSize / scaleFactor) + bitmap.getWidth() / 2;
        int pY = (int) ((testY - y) * puzzleSize / scaleFactor) + bitmap.getHeight() / 2;

        if (pX < 0 || pX >= bitmap.getWidth() ||
                pY < 0 || pY >= bitmap.getHeight()) {
            return false;
        }

        // We are within the rectangle of the piece.
        // Are we touching actual picture?
        return (bitmap.getPixel(pX, pY) & 0xff000000) != 0;
    }

    /**
     * Move the player piece by dx, dy
     *
     * @param dx x amount to move
     * @param dy y amount to move
     */
    public void move(float dx, float dy) {
        x += dx;
        y += dy;
    }

    public void reloadBitmap(Context context){
        bitmap = BitmapFactory.decodeResource(context.getResources(), bitmapId);
    }

    /**
     * Set the original location of a game piece
     */
    public void setOrigLoc(){
        origX = x;
        origY = y;
    }

    public void rotate(float dAngle) {
        angle += dAngle;
    }

    public void Scale(float v) {
        scale *= v;
    }

    public boolean isApplyProbability() {
        return applyProbability;
    }

    public boolean isCatchOnlyOne() {
        return catchOnlyOne;
    }
}

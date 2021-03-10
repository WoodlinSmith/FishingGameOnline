package edu.sdsmt.group4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

class Player extends GamePiece {

    /**
     * Bitmap used to identify a player
     */
    private transient Bitmap identifyingPiece;

    /**
     * The name of the player
     */
    private String name;

    /**
     * The players score
     */
    private int score;

    /**
     * The color used to identify the player
     */
    private int color;

    /**
     * The paint used to draw identifying piece of the player
     */
    private transient Paint paint;

    /**
     * The resource id of the bitmap used to identify a player
     */
    private int identifyingPieceId;


    /**
     * Player constructor
     *
     * @param context the view context
     * @param id bitmap resource id
     * @param identifyingID bitmap resource id used for identifying the player
     * @param color color used to identify the player
     * @param playerName the name of the player
     * @param xPos       starting x location as a normalized coordinate (0 to 1)
     * @param yPos       starting y location as a normalized coordinate (0 to 1)
     *
     * Relative x and y locations in the range 0-1 are used for the center
     * of the game piece.
     */
    public Player(Context context, int id, int identifyingID, int color, String playerName, float xPos, float yPos) {
        super(context, id, xPos, yPos);

        name = playerName;
        this.identifyingPieceId = identifyingID;
        identifyingPiece = BitmapFactory.decodeResource(context.getResources(), identifyingID);

        paint = new Paint();
        this.color = color;
        paint.setColorFilter(new LightingColorFilter(color, 0));

        restrictMovement = true;
    }

    /**
     * Draw the player
     *
     * @param canvas      Canvas we are drawing on
     * @param marginX     Margin X value in pixels
     * @param marginY     Margin Y value in pixels
     * @param gameSize    Size of the game area in pixels
     */
    @Override
    public void draw(Canvas canvas, int marginX, int marginY, int gameSize) {
        canvas.save();

        // Convert x,y to pixels and add the margin, then draw
        canvas.translate(marginX + x * gameSize, marginY + y * gameSize);
        canvas.scale(getScale(), getScale());

        // This magic code makes the center of the piece at 0, 0
        canvas.translate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);

        // Draw the player
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(identifyingPiece, 0, 0, paint);
        canvas.restore();
    }

    /**
     * Load the bitmap
     */
    @Override
    public void reloadBitmap(Context context) {
        super.reloadBitmap(context);
        identifyingPiece = BitmapFactory.decodeResource(context.getResources(), identifyingPieceId);

        paint = new Paint();
        paint.setColorFilter(new LightingColorFilter(color, 0));
    }

    /**
     * Return the players name
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Get the player's score
     */
    public int getScore(){
       return score;
    }

    public void setScore(int score) { this.score = score;}

    /**
     * Function to increment the player's score
     */
    public void addScore(){
        score++;
    }

    /**
     * Get the player's color
     */
    public int getPlayerColor(){
        return color;
    }

    public String getName() {
        return name;
    }
}

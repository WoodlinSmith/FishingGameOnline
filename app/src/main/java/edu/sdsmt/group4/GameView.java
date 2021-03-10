package edu.sdsmt.group4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

class GameView extends View {

    /**
     * The game state
     */
    protected Game game;

    /**
     * Touch event handler
     */
    private TouchHandler touchHandler;

    /**
     * Constructor
     */
    public GameView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor
     */
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor
     */
    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize variables
     */
    private void init() {
        touchHandler = new TouchHandler();
        game = new Game(this, touchHandler);
    }

    /**
     * set the Game state
     *
     * @param game the game state
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Draw the game
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        game.draw(canvas);

        updateTouchHandler();
    }

    /**
     * Update the touch handler
     */
    private void updateTouchHandler() {
        touchHandler.setGameSize(game.getGameSize());
        touchHandler.setMarginX(game.getMarginX());
        touchHandler.setMarginY(game.getMarginY());
    }

    /**
     * Handles a touch event
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(game.getMovable() != null)
            return touchHandler.onTouchEvent(this, game.getMovable(), event);

        return true;
    }

    /**
     * get the touch handler
     *
     * @return the touch handler
     */
    public TouchHandler getTouchHandler() { return touchHandler;
    }
}

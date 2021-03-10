package edu.sdsmt.group4;

import android.view.MotionEvent;
import android.view.View;

public class TouchHandler {

    /**
     * First touch status
     */
    private Touch touch1 = new Touch();

    /**
     * Second touch status
     */
    private Touch touch2 = new Touch();

    /**
     * Left margin in pixels
     */
    private int marginX;
    /**
     * Top margin in pixels
     */
    private int marginY;

    /**
     * The size of the game in pixels
     */
    private int gameSize;
    /**
     * Currently moving gamepiece
     */
    private GamePiece currentlyMoving = null;

    public void setMarginX(int marginX) {
        this.marginX = marginX;
    }

    public void setMarginY(int marginY) {
        this.marginY = marginY;
    }

    public void setGameSize(int gameSize) {
        this.gameSize = gameSize;
    }

    /**
     * Handle a touch event
     *
     * @param movable
     * @param event The touch event
     */
    public boolean onTouchEvent(View view, GamePiece movable, MotionEvent event) {
        currentlyMoving = movable;
        int id = event.getPointerId(event.getActionIndex());

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touch1.id = id;
                touch2.id = -1;
                getPositions(view, event);
                touch1.copyToLast();
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (touch1.id >= 0 && touch2.id < 0) {
                    touch2.id = id;
                    getPositions(view, event);
                    touch2.copyToLast();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touch1.id = -1;
                touch2.id = -1;
                view.invalidate();
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                if (id == touch2.id) {
                    touch2.id = -1;
                } else if (id == touch1.id) {
                    // Make what was touch2 now be touch1 by
                    // swapping the objects.
                    Touch t = touch1;
                    touch1 = touch2;
                    touch2 = t;
                    touch2.id = -1;
                }
                view.invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                getPositions(view, event);
                move();
                return true;
        }

        return view.onTouchEvent(event);
    }

    /**
     * Get the positions for the two touches and put them
     * into the appropriate touch objects.
     *
     * @param event the motion event
     */
    private void getPositions(View view, MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {

            // Get the pointer id
            int id = event.getPointerId(i);

            //
            // Convert an x,y location to a relative location in the
            // game area.
            //
            float x = (event.getX(i) - marginX) / gameSize;
            float y = (event.getY(i) - marginY) / gameSize;

            if (id == touch1.id) {
                touch1.copyToLast();
                touch1.x = x;
                touch1.y = y;
            } else if (id == touch2.id) {
                touch2.copyToLast();
                touch2.x = x;
                touch2.y = y;
            }
        }

        view.invalidate();
    }

    /**
     * Handle movement of the touches
     */
    private void move() {
        // If no touch1, we have nothing to do
        // This should not happen, but it never hurts
        // to check.
        if (touch1.id < 0) {
            return;
        }

        if (touch1.id >= 0) {
            // At least one touch
            // We are moving
            if (onTouched(touch1.lastX, touch1.lastY)) {

                // Don't go out of play area
                if (touch1.x < 0 || touch1.x >= 1 ||
                        touch1.y < 0 || touch2.y >= 1) {
                    return;
                }

                // Players can only move up to %25 of game area
                if(currentlyMoving.isMovementRestricted())
                    if (touch1.x < (currentlyMoving.getOrigX() - 0.25) || touch1.x >= (currentlyMoving.getOrigX() + 0.25) ||
                        touch1.y < (currentlyMoving.getOrigY() - 0.25) || touch1.y >= (currentlyMoving.getOrigY() + 0.25)) {
                    return;
                }

                touch1.computeDeltas();

                currentlyMoving.move(touch1.dX, touch1.dY);
            }
        }

        if (touch2.id >= 0) {
            // Two touches

            /*
             * Rotation
             */
            if (currentlyMoving.canRotate) {
                float angle1 = angle(touch1.lastX, touch1.lastY, touch2.lastX, touch2.lastY);
                float angle2 = angle(touch1.x, touch1.y, touch2.x, touch2.y);
                float da = angle2 - angle1;
                currentlyMoving.rotate(da);
            }

            /*
             * Scale
             */
            if (currentlyMoving.canScale) {
                float previousDistance = calcDistance(touch1.lastX, touch2.lastX, touch1.lastY, touch2.lastY);
                float currentDistance = calcDistance(touch1.x, touch2.x, touch1.y, touch2.y);

                // Guard against division by zero
                if (previousDistance != 0) {
                    currentlyMoving.Scale(currentDistance / previousDistance);
                }
            }

        }
    }

    private float calcDistance(float x1, float x2, float y1, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Determine the angle for two touches
     *
     * @param x1 Touch 1 x
     * @param y1 Touch 1 y
     * @param x2 Touch 2 x
     * @param y2 Touch 2 y
     * @return computed angle in degrees
     */
    private float angle(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    /**
     * Handle a touch message. This is when we get an initial touch
     *
     * @param x x location for the touch, relative to the puzzle - 0 to 1 over the puzzle
     * @param y y location for the touch, relative to the puzzle - 0 to 1 over the puzzle
     * @return true if the touch is handled
     */
    private boolean onTouched(float x, float y) {
        return currentlyMoving.hit(x, y, gameSize, currentlyMoving.getScale());
    }

    /**
     * Local class to handle the touch status for one touch.
     * We will have one object of this type for each of the
     * two possible touches.
     */
    private class Touch {
        /**
         * Touch id
         */
        public int id = -1;

        /**
         * Current x location
         */
        public float x = 0;

        /**
         * Current y location
         */
        public float y = 0;

        /**
         * Previous x location
         */
        public float lastX = 0;

        /**
         * Previous y location
         */
        public float lastY = 0;

        /**
         * Change in x value from previous
         */
        public float dX = 0;

        /**
         * Change in y value from previous
         */
        public float dY = 0;

        /**
         * Copy the current values to the previous values
         */
        public void copyToLast() {
            lastX = x;
            lastY = y;
        }

        /**
         * Compute the values of dX and dY
         */
        public void computeDeltas() {
            dX = x - lastX;
            dY = y - lastY;
        }
    }
}

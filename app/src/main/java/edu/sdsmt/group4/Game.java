package edu.sdsmt.group4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Game {

    /**
     * Percentage of the display width or height that
     * is occupied by the game.
     */
    private final static float SCALE_IN_VIEW = 0.95f;
    /**
     * Constant string use to save and restore parameters to a bundle
     */
    private final static String PARAMETERS = "PARAM";
    /**
     * The view the game is displayed in
     */
    private GameView view;

    private TouchHandler touchHandler;
    /**
     * The current parameters
     */
    private Parameters params = new Parameters();

    private DatabaseTimeout timeout;
    /**
     * The size of the game in pixels
     */
    private int gameSize;
    /**
     * Left margin in pixels
     */
    private int marginX;
    /**
     * Top margin in pixels
     */
    private int marginY;
    /**
     * Paint for filling the area the game is played in
     */
    private Paint fillPaint;
    /**
     * Rectangle we will use for intersection testing
     */
    private Rect overlap = new Rect();

    /**
     * Collection of game pieces
     */
    public ArrayList<GamePiece> gamePieces = new ArrayList<>();

    private String localPlayerName;

    private Timer dbTimer;

    /**
        Capture tool
     */
    private CaptureTool captureTool;

    public Game(GameView gameView) {
        init(gameView);
    }

    public Game(GameView gameView, String localPlayer) {
        initDBTimeout();
        init(gameView);

        localPlayerName = localPlayer;
    }

    private void init(final GameView gameView) {
        view = gameView;
        touchHandler = gameView.getTouchHandler();

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.BLUE);

        dbTimer=new Timer();
        dbTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeout.setQuery(gameView);
            }
        },2000,1000);


    }

    /**
     * Setter for captureTool
     */

    public void setCaptureTool(CaptureTool captureTool) {
        this.captureTool = captureTool;
        setCurrentlyMoving(captureTool);
    }

    /**
     * Constructor
     */
    public Game(GameView View, TouchHandler handler) {
        this.view = View;
        this.touchHandler = handler;

        // Create paint for filling the area the puzzle will
        // be solved in.
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.BLUE);
    }

    /**
     * Inits the timeout task
     */
    public void initDBTimeout()
    {
        timeout=new DatabaseTimeout();
    }

    public void nextTurn() {
        params.playersTurnIndex++;
        if (params.playersTurnIndex >= params.players.size()) {
            params.playersTurnIndex = 0;
            params.round++;
        }

        setOrigLoc();

        setCurrentlyMoving(getCurrentPlayer());
    }

    /**
     * Function to set the original location of a game piece
     */
    private void setOrigLoc() {
        getCurrentPlayer().setOrigLoc();
    }

    public boolean isGameOver() {
        return params.round >= params.numberOfRounds || params.fishes.size() == 0;
    }

    public String getPlayerTurnInfo() {
        return getCurrentPlayer().toString();
    }

    public String getRound() {
        return String.valueOf(params.round+1);
    }

    public void addCollectibles(Context context) {
        Random rnd = new Random();
        for(int i = 0; i < 20; i++){
            //float rnd1 = rnd.nextDouble();

            Fish newFish = (new Fish(context, R.drawable.clownfish,
                    (float) (0.1 + (0.9 - 0.1) * rnd.nextDouble()), (float) (0.1 + (0.9 - 0.1) *
                    rnd.nextDouble())));
            gamePieces.add(newFish);
            params.fishes.add(newFish);
        }
    }

    public void addPlayer(Context context, String name, float xPos, float yPos, int color, int score) {
        Player newPlayer = new Player(context, R.drawable.fisherman, R.drawable.fisherman_outfit, color, name, xPos, yPos);

        newPlayer.setScore(score);
        gamePieces.add(newPlayer);
        params.players.add(newPlayer);
    }

    public void setNumberOfRounds(int rounds) {
        params.numberOfRounds = rounds;
    }

    public int getGameSize() {
        return gameSize;
    }

    public int getMarginX() {
        return marginX;
    }

    public int getMarginY() {
        return marginY;
    }

    public void draw(Canvas canvas) {

        int wid = canvas.getWidth();
        int hit = canvas.getHeight();

        // Determine the minimum of the two dimensions
        int minDim = Math.min(wid, hit);
        gameSize = (int) (minDim * SCALE_IN_VIEW);

        // Compute the margins so we center the puzzle
        marginX = (wid - gameSize) / 2;
        marginY = (hit - gameSize) / 2;

        //
        // Draw the outline of the game area
        //
        canvas.drawRect(marginX, marginY, marginX + gameSize, marginY + gameSize, fillPaint);

        for (GamePiece piece : gamePieces) {
            piece.draw(canvas, marginX, marginY, gameSize);
        }

        if(captureTool != null)
            captureTool.draw(canvas,marginX, marginY, gameSize);
    }


    public Player getCurrentPlayer() {
        if(params.players.isEmpty())
            return null;

        return params.players.get(params.playersTurnIndex);
    }

    /**
     * Function to return the winner
     *
     * @return name of the winner
     */
    public String getWinner() {

        //find player with largest points
        StringBuilder winner = new StringBuilder();
        int maxScore = -1;
        int currScore = -1;
        boolean tied = false;
        for(Player player:params.players){
            currScore = player.getScore();
            if(currScore > maxScore){
                winner = new StringBuilder(player.toString());
                maxScore = currScore;
                tied = false;
            }
            else if(currScore == maxScore){
                winner.append(" and ").append(player.toString());
                tied = true;
            }
        }

        // if the winner tied return tied.
        if(tied)
            winner.append(" tied");
        else
            winner.append(" won");

        return winner.toString();
    }

    /**
     * Function to return the points for each player
     */
    public String getPlayerPoints() {
        StringBuilder pointSummary = new StringBuilder();
        for(Player player:params.players){
            pointSummary.append(player.toString()).append(" caught ").append(player.getScore())
                    .append(" fish\n");
        }
        return pointSummary.toString();
    }

    public GamePiece getMovable() {
        return params.currentlyMoving;
    }

    public String getLocalPlayer() {
        return localPlayerName;
    }

    public int getNumberOfRounds() {
        return params.numberOfRounds;
    }

    private static class Parameters implements Serializable {

        //Total number of rounds
        public int numberOfRounds = 1;
        //Current round number
        public int round = 0;
        public int playersTurnIndex = 0;
        public String playersTurn;

        /**
         * Currently moving gamepiece
         */
        public GamePiece currentlyMoving = null;

        /**
         * Collection of fish pieces
         */
        public ArrayList<GamePiece> fishes = new ArrayList<>();

        /**
         * Collection of game pieces
         */
        public ArrayList<Player> players = new ArrayList<>();
    }

    /**
     * Function to check if the capture tool and capture object overlapped
     *
     * @param fish a potential fish catch
     * @param tool the current tool use to catch
     * @return true if the objects overlapped, false if not
     */
    public boolean checkOverlap(GamePiece fish, GamePiece tool){
        Rect rectFish;
        Rect rectTool;

        //find rectangular bound for each object
        rectFish = setRect(fish);
        rectTool = setRect(tool);

        //if the rectangles overlap, check for pixel overlap
        if(!Rect.intersects(rectFish, rectTool)){
            return false;
        }

        // Determine where the two images overlap
        overlap.set(rectFish);
        final boolean intersect = overlap.intersect(rectTool);

        // We have overlap. Now see if we have any pixels in common
        for(int r=overlap.top; r<overlap.bottom;  r++) {

            int fishY = r - rectFish.top;
            int toolY = r - rectTool.top;

            for(int c=overlap.left;  c<overlap.right;  c++) {

                int fishX = c - rectFish.left;
                int toolX = c - rectTool.left;

                int a = fish.movedPiece.getPixel(fishX, fishY);
                int b = tool.movedPiece.getPixel(toolX, toolY);

                if ((fish.movedPiece.getPixel(fishX, fishY) & 0x80000000) != 0 &&
                        (tool.movedPiece.getPixel(toolX, toolY) & 0x80000000) != 0) {
                    Log.i("collision", "Overlap " + r + "," + c);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Sets the gamepiece to be dragged to current moving gamepiece
     * Which is fisherman for playactivity and tool for captureActivity
     */

    public void setCurrentlyMoving(GamePiece gamePiece){
        params.currentlyMoving = gamePiece;
    }
    /**
     * Function to create a bounding rectangle around a game piece
     *
     * @param gamePiece the game piece node
     * @return the bounding rectangle
     */
    public Rect setRect(GamePiece gamePiece){
        //create new rect
        Rect rect = new Rect();

        //find height and width of the piece's bitmap
        int pieceHeight = gamePiece.movedPiece.getHeight();
        int pieceWidth = gamePiece.movedPiece.getWidth();

        //find the coordinates of the piece
        float pieceLeft = marginX + gamePiece.getX() * gameSize - pieceWidth/2;
        float pieceTop =  marginY + gamePiece.getY() * gameSize - pieceHeight/2;
        float pieceRight = marginX + gamePiece.getX() * gameSize + pieceWidth/2;
        float pieceBottom = marginY + gamePiece.getY() * gameSize + pieceHeight/2;

        //set the new rectangle
        rect.set((int)pieceLeft, (int)pieceTop,(int)pieceRight, (int)pieceBottom );

        return rect;
    }

    /**
     * Function to remove fish and add a point to the current player's score
     *
     * @param fish the caught fish
     * @param player the current player
     */
    public GamePiece catchFish(GamePiece fish, Player player){
        //remove the fish from the list of game pieces
        gamePieces.remove(fish);
        player.addScore();
        return fish;
    }

    /**
     * Function to return the current player's color
     *
     * @return int value of the current color
     */
    public int getPlayerColor() {
        if(getCurrentPlayer() == null)
            return Color.WHITE;

        return getCurrentPlayer().getPlayerColor();
    }

    /**
     * Function to go through the list of fish and check if it is overlapping with the current tool.
     * If the fish is overlapping, it catches the fish according to the current tool's probability.
     */
    public void attemptCatch(){
        GamePiece tool = captureTool;
        //check if the current tool is overlapping with fish

        //list of removed fish
        ArrayList<GamePiece> caughtFish = new ArrayList<>();

        GamePiece closestFish = null;
        float minDis = 1000000;

        int count = 0;
        for(GamePiece fish: params.fishes ){
            if(checkOverlap(fish, tool)) {
                float dis = toolToFishDistance(tool,fish);
                if(dis < minDis) {
                    closestFish = fish;
                    minDis = dis;
                }
                count++;
            }
        }

        int numToCollect = count;

        if(captureTool.isApplyProbability())
            numToCollect = calcNetProbability(count);

        if(captureTool.isCatchOnlyOne())
            caughtFish.add(catchFish(closestFish,getCurrentPlayer()));

        else{
            //check each fish to see if was caught
            int i = 0;
            for(GamePiece fish: params.fishes){
                if(checkOverlap(fish, tool) && i < numToCollect){
                    caughtFish.add(catchFish(fish, getCurrentPlayer()));
                    i++;
                }
            }
        }

        //remove caught fish from the fish list
        for(GamePiece fish: caughtFish){
            params.fishes.remove(fish);
        }

        caughtFish.clear();
    }

    public GamePiece getCaptureTool() {
        return captureTool;
    }

    /**
     * Calculates the number of fish to capture according to the scale factor.
     * @param count
     * @return
     */

    public int calcNetProbability(int count){

        //No capture possible if scale factor is greater than 5 times the original
        if(captureTool.getScale() > 4)
            return 0;

        //Probability doesn't go beyond 0.75 even when the net is made smaller
        if(captureTool.getScale() < 1 )
            return (int)(0.75*count);

        /*Explanation:
         * for scale = 1, probability is 1.0-(0.25*1) = 0.75
         * for scale = 4, probability is 1.0-(0.25*4) = 0
         */

        return (int)((1.0-(0.25*captureTool.getScale()))*count);
    }

    float toolToFishDistance(GamePiece c, GamePiece f){
        return (float)Math.sqrt((c.x-f.x)*(c.x-f.x) + (c.y-f.y)*(c.y-f.y));
    }

    /**
     * Saves the Pieces/players branch to firebase
     * @param data a reference of where to save to
     */
    public void saveJSONObject(DatabaseReference data)
    {
        data.child("Pieces").setValue(params);



        Map<String, Player> map = new HashMap<>();

        for (Player player: params.players) {
            data.child("players/" + player.getName()).setValue(player);
            map.put(player.getName(), player);
        }
    }

    /**
     * Loads the gameboard from firebase.
     * @param dataSnapshot the current grabbed data
     */
    public void loadJSON(DataSnapshot dataSnapshot) {

        if(!dataSnapshot.exists())
            return;

        if(!(dataSnapshot.child("Pieces").exists()))
            return;

        //Get basic information
        params.numberOfRounds = dataSnapshot.child("Pieces/numberOfRounds").getValue(int.class);
        params.round = dataSnapshot.child("Pieces/round").getValue(int.class);
        params.playersTurnIndex = dataSnapshot.child("Pieces/playersTurnIndex").getValue(int.class);
        gamePieces.clear();

        params.fishes.clear();

        //Get fish locations
        for(DataSnapshot fish : dataSnapshot.child("Pieces/fishes").getChildren()){

            Fish newFish = (new Fish(view.getContext(),
                    R.drawable.clownfish,
                    fish.child("x").getValue(float.class),
                    fish.child("y").getValue(float.class)));
            gamePieces.add(newFish);
            params.fishes.add(newFish);
        }

        if(!dataSnapshot.child("players").exists())
            return;

        params.players.clear();
        //Get player locations
        for(DataSnapshot player : dataSnapshot.child("players").getChildren()){
            addPlayer(view.getContext(),
                    player.child("name").getValue(String.class),
                    player.child("x").getValue(float.class),
                    player.child("y").getValue(float.class),
                    player.child("playerColor").getValue(int.class),
                    player.child("score").getValue(int.class));
        }

        if(getCurrentPlayer().getName().equals(localPlayerName))
            setCurrentlyMoving(getCurrentPlayer());
        else
            setCurrentlyMoving(null);

        if(params != null) {
            for (GamePiece piece : gamePieces) {
                piece.reloadBitmap(view.getContext());
            }
        }
    }

    /**
     * Updates the player's timestamp to signify activity
     */
    public void updateTimestamp()
    {
        DatabaseReference playerPieceRef= FirebaseInit.getDb().getReference(
                "Timestamps/players/"+params.playersTurnIndex);
        playerPieceRef.child("timestamp").setValue(ServerValue.TIMESTAMP);
    }

    /**
     * Cancels the timer to build new timeout queries
     */
    public void cancelTimer()
    {
        dbTimer.cancel();
    }
}
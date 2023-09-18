package com.usc.minesweeper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 12;

    private static final int NUMBER_OF_BOMBS = 4;
    private static int FLAGS_LEFT = NUMBER_OF_BOMBS;
    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private boolean[][] bombLocation;
    private boolean[][] flagLocation;
    private boolean[][] uncheckedLocation;
    private int[][] adjacentValues;
    //"State" variable, whether or not user is in flag mode
    public static boolean flagMode = false;
    public Handler handler;
    public static int timer = 0;
    boolean gameOver = false;
    String winOrLoss = "";
    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getIntent().getBooleanExtra("restartGame", false)) {
            // Reset the game here
            resetGame();
        }

        cell_tvs = new ArrayList<>();
        initializeGrid();
        initializeTimer();
        setFlagsTextView();


        // Method (2): add four dynamically created cells
        GridLayout grid = findViewById(R.id.gridLayout01);
        for (int i = 0; i<ROW_COUNT; i++) {
            for (int j=0; j<COLUMN_COUNT; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize( 16 );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                //for some weird reason, if i change this line i get
                // "Channel is unrecoverably broken and will be disposed!"
                //and program fails to compile. couldn't find anything to explain it
                tv.setTextColor(Color.GREEN);
                //
                tv.setBackgroundColor(Color.GREEN);

                tv.setOnClickListener(this::onClickTV);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
        }
    }

    private void initializeGrid() {
        Random rand = new Random(System.currentTimeMillis());
        bombLocation = new boolean[ROW_COUNT][COLUMN_COUNT];
        flagLocation = new boolean[ROW_COUNT][COLUMN_COUNT];
        uncheckedLocation = new boolean[ROW_COUNT][COLUMN_COUNT];
        adjacentValues = new int[ROW_COUNT][COLUMN_COUNT];

        //randomly places bombs
        for(int i=0; i<NUMBER_OF_BOMBS; ++i){
            int bombIndex = rand.nextInt(ROW_COUNT * COLUMN_COUNT);
            int bombRow = bombIndex/COLUMN_COUNT;
            int bombColumn = bombIndex % COLUMN_COUNT;
            Log.d("Debugger", "Bomb #" + i + " is placed at [" + bombRow + "][" + bombColumn + "]");
            if(bombLocation[bombRow][bombColumn]) --i;
            else bombLocation[bombRow][bombColumn] = true;
        }

        //initializes all arrays values
        for(int i=0; i<ROW_COUNT; ++i){
            for(int j=0; j<COLUMN_COUNT; ++j){
                uncheckedLocation[i][j] = true;
                adjacentValues[i][j] = getAdjacents(i, j);
            }
        }
    }
    public int getAdjacents(int i, int j) {
        int k = 0;
        int rows = ROW_COUNT;
        int columns = COLUMN_COUNT;

        if (i + 1 < rows && bombLocation[i + 1][j]) ++k;
        if (i - 1 >= 0 && bombLocation[i - 1][j]) ++k;
        if (j + 1 < columns && bombLocation[i][j + 1]) ++k;
        if (j - 1 >= 0 && bombLocation[i][j - 1]) ++k;
        if (i + 1 < rows && j + 1 < columns && bombLocation[i + 1][j + 1]) ++k;
        if (i - 1 >= 0 && j - 1 >= 0 && bombLocation[i - 1][j - 1]) ++k;
        if (i + 1 < rows && j - 1 >= 0 && bombLocation[i + 1][j - 1]) ++k;
        if (i - 1 >= 0 && j + 1 < columns && bombLocation[i - 1][j + 1]) ++k;

        return k;
    }
    public void initializeTimer(){
        TextView tv = findViewById(R.id.timerCount);
        handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                String time = String.format("%03d", timer);
                tv.setText(time);

                ++timer;
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void setFlagsTextView(){
        TextView tv = findViewById(R.id.flagCount);
        tv.setText(String.valueOf(FLAGS_LEFT));
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;

        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;

        if(gameOver) { sendIntent(); return; }
        //if there is a flag on a gray, allow the user to remove
        if(flagMode && tv.getCurrentTextColor() == Color.BLACK) toggleFlag(tv, i, j);
        if(tv.getCurrentTextColor() != Color.GREEN) return;

        //redundant code
        if (!flagMode) {
            if(bombLocation[i][j]){
                //user clicks a mine -> end the game
                Log.d("Debugger", "Player has clicked the mine. Game is now ending");
                endGame("loss");
            }
            else if(!flagLocation[i][j]){
                //set the number of numbers around it
                revealCells(i, j);
            }
        }
        else toggleFlag(tv, i, j);
        checkWinCondition();
    }

    public void revealCells(int i, int j) {
        if (i < 0 || j < 0 || i >= ROW_COUNT || j >= COLUMN_COUNT || !uncheckedLocation[i][j]) return;

        TextView tv = getTextViewByCoordinates(i, j);

        tv.setBackgroundColor(Color.LTGRAY);
        tv.setTextColor(Color.BLACK);
        uncheckedLocation[i][j] = false;
        if (adjacentValues[i][j] != 0) {
            if(flagLocation[i][j]) toggleFlag(tv, i, j);
            tv.setText(String.valueOf(adjacentValues[i][j]));
            return;
        }


        // recursively reveal neighboring cells
        revealCells(i + 1, j);
        revealCells(i - 1, j);
        revealCells(i, j + 1);
        revealCells(i, j - 1);
        revealCells(i + 1, j + 1);
        revealCells(i - 1, j - 1);
        revealCells(i - 1, j + 1);
        revealCells(i + 1, j - 1);
    }


    public TextView getTextViewByCoordinates(int i, int j) {
        for (TextView tv : cell_tvs) {
            int n = findIndexOfCellTextView(tv);
            int row = n / COLUMN_COUNT;
            int column = n % COLUMN_COUNT;

            if (row == i && column == j) {
                return tv;
            }
        }
        return null;
    }

    public void toggleFlag(TextView tv, int i, int j){
        //if there already exists a bomb there, get rid of it
        if(flagLocation[i][j]){
            tv.setText("");
            flagLocation[i][j] = false;
            FLAGS_LEFT++;
        }
        //if there isnt, insert it
        else{
            tv.setText(getResources().getString(R.string.flag));
            flagLocation[i][j] = true;
            FLAGS_LEFT--;
        }
        setFlagsTextView();
    }

    public void switchMode(View view){
        TextView tv = (TextView) view;
        flagMode = !flagMode;

        Log.d("Debugger", "Icon currently is " + tv.getText() + ". flagMode is " + flagMode);
        if(tv.getText().equals(getResources().getString(R.string.flag))) tv.setText(getResources().getString(R.string.pick));
        else tv.setText(getResources().getString(R.string.flag));
    }

    public void endGame(String winOrLoss_){
        gameOver = true;
        winOrLoss = winOrLoss_;
        //ends the timer thread before moving on
        handler.removeCallbacksAndMessages(null);

        revealBombs();

        //In the case that a user selects somewhere outside of the GridLayout
        ConstraintLayout root = findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendIntent();
            }
        });
    }
    public void sendIntent(){
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("result", winOrLoss);
        intent.putExtra("timer", String.valueOf(timer));
        startActivity(intent);
    }

    public void revealBombs(){
        for(int i=0; i<ROW_COUNT; ++i){
            for(int j=0; j<COLUMN_COUNT; ++j){
                if(bombLocation[i][j]){
                    TextView tv = getTextViewByCoordinates(i, j);
                    tv.setText(getResources().getString(R.string.mine));
                }
            }
        }
    }

    public void checkWinCondition(){
        if (Arrays.deepEquals(bombLocation, uncheckedLocation)) endGame("win");
    }

    public void resetGame(){
        Log.d("Debugger", "Resetting game");
        FLAGS_LEFT = NUMBER_OF_BOMBS;
        flagMode = false;
        timer = 0;
    }
}
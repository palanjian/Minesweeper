package com.usc.minesweeper;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 12;

    private static final int NUMBER_OF_BOMBS = 4;
    private static int BOMBS_LEFT = NUMBER_OF_BOMBS;
    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private boolean[][] bombLocation;

    //"State" variable, whether or not user is in flag mode
    public static boolean flagMode = false;


    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();
        initializeBombs();
        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i<ROW_COUNT; i++) {
            for (int j=0; j<COLUMN_COUNT; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize( 16 );//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GREEN);
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

    private void initializeBombs() {
        Random rand = new Random(System.currentTimeMillis());
        bombLocation = new boolean[ROW_COUNT][COLUMN_COUNT];
        for(int i=0; i<NUMBER_OF_BOMBS; ++i){
            int bombIndex = rand.nextInt(ROW_COUNT * COLUMN_COUNT);
            int bombRow = bombIndex/COLUMN_COUNT;
            int bombColumn = bombIndex % COLUMN_COUNT;
            Log.d("Debugger", "Bomb #" + i + " is placed at [" + bombRow + "][" + bombColumn + "]");
            if(bombLocation[bombRow][bombColumn]) --i;
            else bombLocation[bombRow][bombColumn] = true;
        }
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
        //tv.setText(String.valueOf(i)+String.valueOf(j));
        //tv.setTextColor(Color.GRAY);

        //redundant code
        if (tv.getCurrentTextColor() == Color.GREEN && !flagMode) {
            if(bombLocation[i][j]){
                tv.setBackgroundColor(Color.RED);
                BOMBS_LEFT--;
            }
            else{
                tv.setBackgroundColor(Color.LTGRAY);
            }
        }
        else if (tv.getCurrentTextColor() == Color.GRAY && flagMode){
            //implement flag functionality
        }
    }
}
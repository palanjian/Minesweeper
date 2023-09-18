package com.usc.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        String timer = intent.getStringExtra("timer");

        TextView timerTv = (TextView)findViewById(R.id.timer);
        TextView resultTv = (TextView)findViewById(R.id.result);
        TextView messageTv = (TextView)findViewById(R.id.message);

        timerTv.setText("Used " + timer + " seconds.");

        if(result.equals("win")){
            resultTv.setText("You won.");
            messageTv.setText("Good Job!");
        }
        else{
            resultTv.setText("You lost.");
            messageTv.setText("Try Again!");
        }
    }

    public void playAgain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("restartGame", true);
        startActivity(intent);
    }
}
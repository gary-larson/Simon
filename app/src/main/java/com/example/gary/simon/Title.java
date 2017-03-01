package com.example.gary.simon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;


/**
 * Created by antonio on 2/19/2017.
 */

public class Title extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,
        View.OnClickListener {
    //declare variables
    private int top_Score = 0;
    private static final String DATA_FILENAME = "simon.txt";
    private boolean gameModeChosen= false;
    private int gameMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title);
        // test if instance was not restored initialize game and check for saved game added by Gary
        if (savedInstanceState == null) {
           readData();
        }

        Button b = (Button) findViewById(R.id.play_button);
        b.setOnClickListener(this);
        b = (Button) findViewById(R.id.score_button);
        b.setOnClickListener(this);
        b= (Button)findViewById(R.id.gameInfo_button);
        b.setOnClickListener(this);
        RadioGroup rg =(RadioGroup)findViewById(R.id.game_mode);
        rg.setOnCheckedChangeListener(this);

        //Set up listener for about button
        findViewById(R.id.about_button).setOnClickListener(new AboutApp());
    }
    /******************************************************************************************
     * method will start game, display developer's information, top score, and instruction
     * layout
     *****************************************************************************************/
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.play_button) {
            // if gameMode is not selected display alertDialog message
            if(gameModeChosen == false){
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Please select game mode");
                builder.setPositiveButton("OK", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            //else start game
            else {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("gameMode", gameMode);
                startActivity(intent);
            }
        }
        if(view.getId() == R.id.score_button){

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("Here is the to top score: " +top_Score);
            builder.setPositiveButton("OK", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if(view.getId() == R.id.gameInfo_button){
            Intent intent = new Intent(getApplicationContext(), Instructions.class);
            startActivity(intent);
        }
    }
    /******************************************************************************************
    * method will select what game mode will be set up
     * GameMode= 1  - Normal
     * GameMode = 2 - Fast
     * GameMode= 3 - Insane mode(sequence will change every round)
     *****************************************************************************************/
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
        if(checkId == R.id.one_radioButton){
            gameModeChosen = true;
            gameMode =1;
        }
        if(checkId ==R.id.two_radioButton){
            gameModeChosen =true;
            gameMode = 2;
        }
        if(checkId == R.id.insane_radioButton){
            gameModeChosen =true;
            gameMode = 3;
        }

    }

    /******************************************************************************************
     * Class creates alertDialog to display apps' information about the developers and
     * credits to all sounds and images used.-By Antonio Ramos
     *****************************************************************************************/
    private class AboutApp implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String message = "<html>" +
                    "<h2>About App</h2>" +
                    "<p><b>Programmers: </b>Gary Larson And Antonio Ramos <br>" +
                    "<h2>Sounds</h2>" +
                    "<b>Source:</b>Sound associated with Simon's Button Image<br>" +
                    "<b>Creator:</b>Simon Dalzell<br>" +
                    "<b>Description:</b>Organ - quiet - A4 (NT5_Man3Quiet_155_rr1.wav)<br>" +
                    "<b>Link: </b> <a href='http://www.freesound.org/people/Samulis/sounds/373717/'>source website</a><br>" +
                    "<b>Description:</b>Organ - quiet - C4 (NT5_Man3Quiet_146_rr1.wav)<br>" +
                    "<b>Link: </b> <a href='http://www.freesound.org/people/Samulis/sounds/373714/'>source website</a><br>" +
                    "<b>Description:</b>Organ - quiet - D#4 (NT5_Man3Quiet_149_rr1.wav)<br>" +
                    "<b>Link: </b> <a href='http://www.freesound.org/people/Samulis/sounds/373715/'>source website</a><br>" +
                    "<b>Description:</b>Organ - quiet - F4 (NT5_Man3Quiet_152_rr1.wav)<br>" +
                    "<b>Link: </b> <a href='http://www.freesound.org/people/Samulis/sounds/373716/'>source website</a><br>" +
                    "<b>License: </b> Creative Commons 0<br>" +
                    "<b>Source:</b>Sound to alert player time is up<br>" +
                    "<b>Creator:</b>hypocore<br>" +
                    "<b>Description:</b>Buzzer<br>" +
                    "<b>Link: </b> <a href='http://www.freesound.org/people/hypocore/sounds/164090/'>source website</a><br>" +
                    "<h2>Images </h2>" +
                    "<b>Source:</b> Background Image <br>" +
                    "<b>Creator:</b> Viscious-Speed <br>" +
                    "<b>Link: </b> <a href='http://openclipart.org/detail/173820/" +
                    "colourful-abstract-background/content/space-boss-battle-theme'>" +
                    "source website</a><br>" +
                    "<b>License: </b> ?" +

                    "</p></html>";
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(Html.fromHtml(message));
            builder.setPositiveButton("OK", null);

            AlertDialog dialog = builder.create();
            dialog.show();

            // must be done after the call to show();
            // allows anchor tags to work
            TextView tv = (TextView) dialog.findViewById(android.R.id.message);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
    // read game state from file added by Gary
    private void readData() {

        try {
            // Log.i("INFO", "---------- Entered Read");
            FileInputStream fis = openFileInput(DATA_FILENAME);
            Scanner scanner = new Scanner(fis);

            // Log.i("INFO", "---------- Read Setup Done");
            if (scanner.hasNext()) {
                // Log.i("INFO", "---------- Read has DATA");
             //   int i;

                top_Score = scanner.nextInt();

            }
            scanner.close();
        } catch (FileNotFoundException e) {
           // AlertDialog.Builder builder = new AlertDialog.Builder(this);
           // builder.setMessage("NO top scores. Please play game to record a top score");
           // builder.setPositiveButton("OK", null);

          //  AlertDialog dialog = builder.create();
           // dialog.show();

            // Log.i("INFO", "---------- Read Exception");
            // ok if file does not exist
        }
    }
}
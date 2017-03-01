package com.example.gary.simon;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
//import android.text.Html;
//import android.text.method.LinkMovementMethod;
//import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener{

    // Set Constants for the three different speeds of the game
    private final int BUTTON_DELAY_1 = 500;
    private final int BUTTON_DELAY_2 = 400;
    private final int BUTTON_DELAY_3 = 300;
    private final int PLAYER_RESPONSE_MULTIPLIER = 4;

    // Set constants for saving and comparing sequences
    private final int TOP_LEFT = 1;
    private final int TOP_RIGHT = 2;
    private final int BOTTOM_LEFT = 3;
    private final int BOTTOM_RIGHT = 4;
    private final int MAX_SEQ_LENGTH = 90;

    // setup member variables
    private Timer timer;
    private Timer playerRespondTimer;
    // iIb is current ImageButton, iDr is current Drawable, iDelay is current speed of the game
    private int iIb, iDr, iDelay = BUTTON_DELAY_1;
    private int count = 0, playerCount = 0;

    //Soundpool Variables
    private SoundPool soundpool;
    private Set<Integer> soundsLoaded;
    private int sound_bl_Id;
    private int sound_br_Id;
    private int sound_tl_Id;
    private int sound_tr_Id;
    private int buzzer_Id;

    //countdown thread variables
    private CountDownTask countDownTask;
    private boolean delayCountDown = false;
    private final int [] countNumbers= {R.drawable.three1,R.drawable.two1,R.drawable.one1};

    //players variables
    private boolean enablePlayerButtons;      //flag disables players input until countdown is complete
    private int checkAnswer= 0;
    private NextRoundTask nextRoundTask;
    private boolean check;
    private int topScore= 0;
    private int currentScore= 0;

    // Gameplay Variables
    private int[] simonSequence;
    private int cycleThruSequence =0;
    private boolean isSimonsTurn = true;
    private int simonSeqCurrent = 0;
    private Random rand  = new Random();

    private static final String DATA_FILENAME = "simon.txt";

    //Variable is used to select game mode
    private int gameMode =2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize sequence Array
        simonSequence = new int[MAX_SEQ_LENGTH];



        // SoundPool variable initialization
        soundsLoaded = new HashSet<Integer>();

        // setup random generator
        //rand = new Random();

        // Set up the listeners for the four buttons (all the same listener)
        ImageButton ib = (ImageButton) findViewById(R.id.topLeft_imageButton);
        ib.setOnClickListener(this);
        ib = (ImageButton) findViewById(R.id.topRight_imageButton);
        ib.setOnClickListener(this);
        ib = (ImageButton) findViewById(R.id.bottomLeft_imageButton);
        ib.setOnClickListener(this);
        ib = (ImageButton) findViewById(R.id.bottomRight_imageButton);
        ib.setOnClickListener(this);

        // for testing
        // iDelay = BUTTON_DELAY_2;
        // iDelay = BUTTON_DELAY_3;

        //set up the listener for countdown thread
        Button b = (Button) findViewById(R.id.newGame_button);
        b.setOnClickListener(this);

        // test if instance was not restored initialize game and check for saved game added by Gary
        if (savedInstanceState == null) {
            readData();
            TextView tv = (TextView)findViewById(R.id.topScore_textView);
            tv.setText(String.valueOf(topScore));
        }
        Intent mIntent = getIntent();
        gameMode = mIntent.getIntExtra("gameMode", 0);
        if (gameMode == 3) {
            iDelay = BUTTON_DELAY_3;
        } else if (gameMode == 2) {
            iDelay = BUTTON_DELAY_2;
        } else {
            iDelay = BUTTON_DELAY_1;
        }
        setUpCurrentSequence();

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
                int i;
                //simonSeqCurrent = scanner.nextInt();
                topScore = scanner.nextInt();
                simonSeqCurrent = scanner.nextInt();
                for (i = 0; i < simonSeqCurrent; i++)
                    simonSequence[i] = scanner.nextInt();
                if (scanner.hasNextInt()) {
                    gameMode = scanner.nextInt();
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            // Log.i("INFO", "---------- Read Exception");
            // ok if file does not exist
        }
    }
    // call method to save game state to file added by Gary
    @Override
    protected void onStop() {
        super.onStop();
        // Log.i("INFO", "---------- Write CALLED");
        writeData();
    }
    // write game state to file added by Gary
    private void writeData() {

        try {
            // Log.i("INFO", "---------- Entered Write");
            FileOutputStream fos = openFileOutput(DATA_FILENAME, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            PrintWriter pw = new PrintWriter(bw);
            int i;
            // Log.i("INFO", "---------- Write Setup Complete");

            //pw.println(simonSeqCurrent);
            pw.println(topScore);
            pw.println(simonSeqCurrent);
            for (i = 0; i < simonSeqCurrent; i++)
                pw.println(simonSequence[i]);
            // Log.i("count", "**********************"+ simonSequence[i]+ "** "+simonSeqCurrent );

            // Log.i("INFO", "---------- Write DATA COMPLETE");
            pw.println(gameMode);
            pw.close();
        } catch (FileNotFoundException e) {
            // Log.e("WRITE_ERR", "Cannot save data: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
        }
    }
    /*******************************************************************************************
    * Method creates simon sequence.
    ****************************************************************************************** */
    private void setUpCurrentSequence(){
        if (gameMode == 3) {
            for (int i = 0; i < simonSeqCurrent; i++) {
                simonSequence[i] = rand.nextInt(4) + 1;
            }
        }

        simonSequence[simonSeqCurrent] = rand.nextInt(4) + 1;

        simonSeqCurrent++;
    }

    private void playSimonSequence() {
       // isSimonsTurn = true;
        cycleThruSequence = 0;
        playSimonNext();
    }

    // method to change image to pressed image and start the timer
    private void setImageButton(int dr) {
        ImageButton ib = (ImageButton) findViewById(iIb);
        ib.setImageResource (dr);

        if (timer == null) {
            timer = new Timer();
            timer.schedule(new ButtonTask(), iDelay);
        }
    }

    // Method to reset the button to its original state
    private void resetImageButton(int dr) {
        ImageButton ib = (ImageButton) findViewById(iIb);
        ib.setImageResource (dr);
        if (isSimonsTurn) {
            playSimonNext();
        }

        else {
            // start timer for player to respond
           if (playerRespondTimer == null && check) {
                playerRespondTimer = new Timer();
                playerRespondTimer.schedule(new PlayerTimeExpiredTask(), BUTTON_DELAY_1 * PLAYER_RESPONSE_MULTIPLIER);
               playerCount++;
               // Log.i("STATUS", "Reset PLAYER TIMER " + playerCount );
           //} else {
              // Log.i("STATUS", "Reset PLAYER TIMER not fired NOT NULL");
           }
        }
    }

    private void playSimonNext() {
        if (simonSequence[cycleThruSequence] > 0) {
            switch (simonSequence[cycleThruSequence]) {
                case 1:
                    cycleThruSequence++;
                    iIb = R.id.topLeft_imageButton;
                    iDr = R.drawable.green_tl;
                    setImageButton(R.drawable.pressed_tl);
                    playSound(sound_tl_Id);
                    break;
                case 2:
                    cycleThruSequence++;
                    iIb = R.id.topRight_imageButton;
                    iDr = R.drawable.red_tr;
                    setImageButton(R.drawable.pressed_tr);
                    playSound(sound_tr_Id);
                    break;
                case 3:
                    cycleThruSequence++;
                    iIb = R.id.bottomLeft_imageButton;
                    iDr = R.drawable.yellow_bl;
                    setImageButton(R.drawable.pressed_bl);
                    playSound(sound_bl_Id);
                    break;
                case 4:
                    cycleThruSequence++;
                    iIb = R.id.bottomRight_imageButton;
                    iDr = R.drawable.cyan_br;
                    setImageButton(R.drawable.pressed_br);
                    playSound(sound_br_Id);
                    break;
            }
             //simonSeqCurrent++; //removed by Gary
        } else {
            isSimonsTurn = false;
            // start timer for player to respond disabled for troubleshooting
            if (playerRespondTimer == null) {
                playerRespondTimer = new Timer();
                playerRespondTimer.schedule(new PlayerTimeExpiredTask(), BUTTON_DELAY_1 * PLAYER_RESPONSE_MULTIPLIER);
                playerCount++;
               // Log.i("STATUS", "PlaySimon PLAYER TIMER " + playerCount );
            }
        }
    }
    // method to handle button clicks if timer is running buttons are disabled
    @Override
    public void onClick(View v) {
        //if statement disables player input into countdown thread is complete
        if(enablePlayerButtons) {
            if (timer == null && !isSimonsTurn) {
                switch (v.getId()) {
                    case R.id.topLeft_imageButton:
                       // Log.i("Status", "Call Cancel #1 onClick tl");
                        cancelPlayer();     // stop timer for player to respond
                        iIb = R.id.topLeft_imageButton;
                        iDr = R.drawable.green_tl;
                        setImageButton(R.drawable.pressed_tl);
                        playSound(sound_tl_Id);
                        check = checkPlayerInPut(TOP_LEFT);
                        break;
                    case R.id.topRight_imageButton:
                       // Log.i("Status", "Call Cancel #2 onClick tr");
                        cancelPlayer();     // stop timer for player to respond
                        iIb = R.id.topRight_imageButton;
                        iDr = R.drawable.red_tr;
                        setImageButton(R.drawable.pressed_tr);
                        playSound(sound_tr_Id);
                        check = checkPlayerInPut(TOP_RIGHT);
                        break;
                    case R.id.bottomLeft_imageButton:
                       // Log.i("Status", "Call Cancel #3 onClick bl");
                        cancelPlayer();     // stop timer for player to respond
                        iIb = R.id.bottomLeft_imageButton;
                        iDr = R.drawable.yellow_bl;
                        setImageButton(R.drawable.pressed_bl);
                        playSound(sound_bl_Id);
                        check = checkPlayerInPut(BOTTOM_LEFT);
                        break;
                    case R.id.bottomRight_imageButton:
                       // Log.i("Status", "Call Cancel #4 onClick br");
                        cancelPlayer();     // stop timer for player to respond
                        iIb = R.id.bottomRight_imageButton;
                        iDr = R.drawable.cyan_br;
                        setImageButton(R.drawable.pressed_br);
                        playSound(sound_br_Id);
                        check = checkPlayerInPut(BOTTOM_RIGHT);
                        break;
                }
            }

            //Starts a new round if user answers enters correct sequence
            checkAnswer ++;
            if(check && checkAnswer >=simonSeqCurrent){
                updateScore();
                cancelPlayer();
                startNextRound();
            }
        }
        if(v.getId() == R.id.newGame_button){
            newGame();
           // Log.i("new Game", "newGame**********");
        }

    }
    /**********************************************************************************************
    * new game will reset current score and start new game- By Antonio Ramos
     *********************************************************************************************/
    private void newGame(){
        for(int i =0; i<simonSeqCurrent; i++){
            simonSequence[i] =0;
        }
        TextView tv =(TextView) findViewById(R.id.score_textView);
        tv.setText(String.valueOf(0));
         tv = (TextView)findViewById(R.id.gameOver_textview);
        tv.setText("");
        simonSeqCurrent=0;
        resetCheckValues();
        isSimonsTurn = true;
        cancelPlayer();
        setUpCurrentSequence();
        startCountdownGame();

    }
    /*********************************************************************************************
    * method will compare Simon's sequence to users input correct return true and start next round
     * if not correct buzz and ask user to start new game.
     *********************************************************************************************/
    private boolean checkPlayerInPut(int input){
        if(input == simonSequence[checkAnswer]){
            return true;
        }else {
            gameOver();
            return false;
        }
    }
    /*********************************************************************************************
     * method will clear game data from variables, display game over message, enablePlayer
     * button and reset timers/threads
     *********************************************************************************************/
    private void gameOver(){
        cancelPlayer();
        for(int i =0; i<simonSeqCurrent; i++){
            simonSequence[i] =0;
        }
        simonSeqCurrent=0;
        resetCheckValues();
       // cancelButton();
       // Log.i("Status", "Call Cancel #5 gameOver");

        isSimonsTurn = false;
        playSound(buzzer_Id);
        if(nextRoundTask != null){
            nextRoundTask = null;
        }
        gameOverTitle();
    }
    /**************************************************************************************
    * method updates and display current score and top score.
     *************************************************************************************/
    private void updateScore(){
        TextView tv =(TextView)findViewById(R.id.score_textView);
        currentScore = simonSeqCurrent * gameMode;
        tv.setText(String.valueOf(currentScore));

        if(currentScore > topScore){
            tv =(TextView) findViewById(R.id.topScore_textView);
            topScore = currentScore;
            tv.setText(String.valueOf(topScore));
        }
    }
    /**************************************************************************************
     * method display game over message and enables player's buttons
     *************************************************************************************/
    private void gameOverTitle(){
        enablePlayerButtons = false;
        TextView tv = (TextView)findViewById(R.id.gameOver_textview);
        tv.setText(R.string.game_over_label);
    }
    // Callback method for the timer resets image and cancels timer Count is for debugging
    private class ButtonTask extends TimerTask {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resetImageButton(iDr);
                   // Log.i("ButtonTask", "-------------- " + count);
                }
            });
            count++;
            cancelButton();
        }
    }

    private class PlayerTimeExpiredTask extends TimerTask {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playerOutOfTime();
                   // Log.i("PlayerExpiredTask", "-------------- AGAIN");
                    gameOverTitle();
                    gameOver();
                }
            });
          //  cancelPlayer();
          //  Log.i("Status", "Cancel #6 PlayerExpiredTask");
           // gameOverTitle();
        }

    }

    // method to cancel timer
    private void cancelButton() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        //if statement will cancels countDown Thread and reset countDownTask to null -By Antonio Ramos
        if(countDownTask != null) {
            countDownTask.cancel(true);
            countDownTask = null;
        }
    }
    // Cancels player Respond Timer
    private void cancelPlayer() {
        if (playerRespondTimer != null) {
            playerRespondTimer.cancel();
            playerRespondTimer = null;
           // Log.i("STATUS", "cancelPLAYER TIMER " + playerCount );
       // } else {
           // Log.i("Status", "cancelPlayer Timer is null");
        }

    }
    // use onResume to setup SoundPool
    @Override
    protected void onResume() {
        super.onResume();

        // SoundPool
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        SoundPool.Builder sb = new SoundPool.Builder();
        sb.setAudioAttributes(attrBuilder.build());
        sb.setMaxStreams(2);
        soundpool = sb.build();

        soundpool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                // status 0 is success
                if (status == 0) {
                    soundsLoaded.add(sampleId);
                   // Log.i("SOUNDPOOL", " Sound loaded " + sampleId);
              //  } else {
                   // Log.i("SOUNDPOOL", "Error loading sound " + status);
                }
            }
        });

        // Load sounds
        sound_bl_Id = soundpool.load(this, R.raw.sound_bl, 1);
        sound_br_Id = soundpool.load(this, R.raw.sound_br, 1);
        sound_tl_Id = soundpool.load(this, R.raw.sound_tl, 1);
        sound_tr_Id = soundpool.load(this, R.raw.sound_tr, 1);
        buzzer_Id = soundpool.load(this, R.raw.buzzer, 1);

        //set delayCountDown to true to delay countdown thread to allow game time to set up
        delayCountDown=true;

        resetCheckValues();
        startCountdownGame();
    }

    // cancel timer if activity loses focus
    @Override
    protected void onPause() {
        super.onPause();
        writeData();
        cancelButton();
      //  Log.i("Status", "Call Cancel #7 onPause");
        cancelPlayer();
        if (soundpool != null) {
            soundpool.release();
            soundpool = null;

            soundsLoaded.clear();
        }
    }

    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                            LOOK AT
          if we dont need lets get rid of onCreateOptionsMenu,onOptionsItemSelected
     !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    } */

    // plays sound related to button
    private void playSound (int iSound) {
        if (soundsLoaded.contains(iSound)) {
            soundpool.play(iSound, 1.0f, 1.0f, 0, 0, (float) iDelay / (float) BUTTON_DELAY_1);
        }
    }


    /******************************************************************************************
     * Method activates countdown thread to give user time to get ready before game begin/resumes.
     * If countdown thread is activate method prevents user from calling countdown thread until
     * multiple times.-By Antonio Ramos
     *****************************************************************************************/
    private void startCountdownGame(){

        if (countDownTask != null && countDownTask.getStatus() == AsyncTask.Status.FINISHED) {
            countDownTask = null;
           // Log.i("countDownTask","*****************NULL********");
        }

        if(countDownTask == null) {
            countDownTask = new CountDownTask();
            countDownTask.execute();

        }
    }
    /******************************************************************************************
     * Class runs countdown thread. Thread will cycle through image numbers from 3 to 1, change
     * background color between image numbers, and play beep sound for each image displayed.
     * -By Antonio Ramos
     *****************************************************************************************/
    class CountDownTask extends AsyncTask<Void, Integer, Integer>{
        ImageView im = (ImageView) findViewById(R.id.count);
        View layout = findViewById(R.id.content_main);
        TextView tv = (TextView)findViewById(R.id.switch_textview);

        //preset-up will make count imageView visible and change background color to red
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enablePlayerButtons = false;
            im.setVisibility(View.VISIBLE);
            layout.setBackgroundColor(0xffeb0404);


        }
        //pause thread three times with one second interval.
        @Override
        protected Integer doInBackground(Void... voids) {
            //try catch must be used with Thread.sleep method which allows thread to be interrupted
            try {
                for(int i = 0; i <countNumbers.length; i++) {
                    //if called from onResume will delay countdown thread to allow game time to set up
                    if(delayCountDown){
                        Thread.sleep(200);
                    }
                    publishProgress(i);
                    Thread.sleep(1000); //delay for one second
                }
                return null;
            }
            catch (InterruptedException e){
                return null;
            }
        }
        //updates images and sounds on the main UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values[0] == 1){
                layout.setBackgroundColor(0xfffffd15);
            }
            if(values[0] ==2){
                layout.setBackgroundColor(0xff33d133);
            }
            im.setImageResource(countNumbers[values[0]]);
        }
        //resets background images and makes number image view invisible
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            RelativeLayout rl = (RelativeLayout) findViewById(R.id.content_main);
            im.setImageResource(R.drawable.three1);
            im.setVisibility(View.INVISIBLE);
            rl.setBackgroundResource(R.drawable.background1);
            delayCountDown = false;
            enablePlayerButtons = true;

                // added to start game
                playSimonSequence();



        }
    }

    // Player did not respond in time
    private void playerOutOfTime() {
        playSound(buzzer_Id);
    }
    private void resetCheckValues(){
        checkAnswer =0;
        cycleThruSequence =0;
    }

    private void startNextRound(){
        nextRoundTask = new NextRoundTask();
        resetCheckValues();
        nextRoundTask.execute();


       // isSimonsTurn = true;

       // setUpCurrentSequence(); //for debugging
      //  playSimonSequence();      for debugging
    }
    /********************************************************************************************
    * class creates delay between rounds and starts next round
     ****************************************************************************************/
    private class NextRoundTask extends AsyncTask<Void, Void, Integer>{

        @Override
        protected Integer doInBackground(Void... voids) {
            try {

                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

                setUpCurrentSequence();
       //     if(gameMode !=  2 ){

       //     }
                cancelPlayer();
                isSimonsTurn = true;
                playSimonSequence();
        }
    }

}

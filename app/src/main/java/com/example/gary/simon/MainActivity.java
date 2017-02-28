package com.example.gary.simon;

import android.content.pm.ActivityInfo;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Random;
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
    private int count = 0;

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
    Random rand  = new Random();


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
        //Set up listener for about button
        findViewById(R.id.about_button).setOnClickListener(new AboutApp());

        setUpCurrentSequence();

    }
    /*******************************************************************************************
    * Method creates simon sequence.
    ****************************************************************************************** */
    private void setUpCurrentSequence(){
            simonSequence[simonSeqCurrent] = rand.nextInt(4) + 1;
    }

    private void playSimonSequence() {
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
           if (playerRespondTimer == null) {
                playerRespondTimer = new Timer();
                playerRespondTimer.schedule(new PlayerTimeExpiredTask(), iDelay * PLAYER_RESPONSE_MULTIPLIER);
           }
        }
    }

    private void playSimonNext() {
        if (simonSequence[simonSeqCurrent] > 0) {
            switch (simonSequence[simonSeqCurrent]) {
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
            simonSeqCurrent++;
        } else {
            isSimonsTurn = false;
            // start timer for player to respond
            if (playerRespondTimer == null) {
                playerRespondTimer = new Timer();
                playerRespondTimer.schedule(new PlayerTimeExpiredTask(), iDelay * PLAYER_RESPONSE_MULTIPLIER);
            }
        }
    }
    // method to handle button clicks if timer is running buttons are disabled
    @Override
    public void onClick(View v) {
        //if statement disables player input into countdown thread is complete
        if(enablePlayerButtons) {
            if (timer == null) {
                switch (v.getId()) {
                    case R.id.topLeft_imageButton:
                        // stop timer for player to respond
                        iIb = R.id.topLeft_imageButton;
                        iDr = R.drawable.green_tl;
                        setImageButton(R.drawable.pressed_tl);
                        playSound(sound_tl_Id);
                        check = checkPlayerInPut(TOP_LEFT);
                        break;
                    case R.id.topRight_imageButton:
                        // stop timer for player to respond
                        iIb = R.id.topRight_imageButton;
                        iDr = R.drawable.red_tr;
                        setImageButton(R.drawable.pressed_tr);
                        playSound(sound_tr_Id);
                        check = checkPlayerInPut(TOP_RIGHT);
                        break;
                    case R.id.bottomLeft_imageButton:
                        // stop timer for player to respond
                        iIb = R.id.bottomLeft_imageButton;
                        iDr = R.drawable.yellow_bl;
                        setImageButton(R.drawable.pressed_bl);
                        playSound(sound_bl_Id);
                        check = checkPlayerInPut(BOTTOM_LEFT);
                        break;
                    case R.id.bottomRight_imageButton:
                        // stop timer for player to respond
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
                playerRespondTimer.cancel();
                startNextRound();
            }
        }
        if(v.getId() == R.id.newGame_button){
            newGame();
            Log.i("new Game", "newGame**********");
        }

    }
    /**********************************************************************************************
    * new game will reset current score and start new game
     *********************************************************************************************/
    private void newGame(){
        TextView tv =(TextView) findViewById(R.id.score_textView);
        tv.setText(String.valueOf(0));
         tv = (TextView)findViewById(R.id.gameOver_textview);
        tv.setText("");
        simonSeqCurrent=0;
        startCountdownGame();
        resetCheckValues();
    }
    /*********************************************************************************************
    * method will compare Simon's sequence to users input correct return true and start next round
     * if not correct buzz and ask user to start new game.
     *********************************************************************************************/
    public boolean checkPlayerInPut(int input){
        if(input == simonSequence[checkAnswer]){
            return true;

        }else {
            PlayerTimeExpiredTask wrongAnswer = new PlayerTimeExpiredTask();
            wrongAnswer.run();
            TextView tv = (TextView)findViewById(R.id.gameOver_textview);
            tv.setText("GAME OVER!!");
            enablePlayerButtons = false;
            return false;
        }

    }
    /**************************************************************************************
    * method updates score and top score
     *************************************************************************************/
    private void updateScore(){
        TextView tv =(TextView)findViewById(R.id.score_textView);
        currentScore = simonSeqCurrent;
        tv.setText(String.valueOf(currentScore));

        if(currentScore > topScore){
            tv =(TextView) findViewById(R.id.topScore_textView);
            topScore = currentScore;
            tv.setText(String.valueOf(topScore));
        }
    }
    private void gameOverTitle(){
        enablePlayerButtons = false;
        TextView tv = (TextView)findViewById(R.id.gameOver_textview);
        tv.setText("GAME OVER!!");
    }
    // Callback method for the timer resets image and cancels timer Count is for debugging
    class ButtonTask extends TimerTask {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resetImageButton(iDr);
                    Log.i("ButtonTask", "-------------- " + count);
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
                    Log.i("PlayerExpiredTask", "-------------- AGAIN");
                    gameOverTitle();
                }
            });
            cancelPlayer();
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
                    Log.i("SOUNDPOOL", " Sound loaded " + sampleId);
                } else {
                    Log.i("SOUNDPOOL", "Error loading sound " + status);
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
        cancelButton();
        cancelPlayer();
        if (soundpool != null) {
            soundpool.release();
            soundpool = null;

            soundsLoaded.clear();
        }
    }

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
    }




    // plays sound related to button
    private void playSound (int iSound) {
        if (soundsLoaded.contains(iSound)) {
            soundpool.play(iSound, 1.0f, 1.0f, 0, 0, (float) iDelay / (float) BUTTON_DELAY_1);
        }
    }


    /******************************************************************************************
     * Class creates alertDialog to display apps' information about the developers and
     * credits to all sounds and images used.-By Antonio Ramos
     *****************************************************************************************/
    class AboutApp implements View.OnClickListener{
        @Override
        public void onClick(View view){
            String message = "<html>"+
                    "<h2>About App</h2>" +
                    "<p><b>Programmers: </b>Gary Larson And Antonio Ramos <br>"+
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
    /******************************************************************************************
     * Method activates countdown thread to give user time to get ready before game begin/resumes.
     * If countdown thread is activate method prevents user from calling countdown thread until
     * multiple times.-By Antonio Ramos
     *****************************************************************************************/
    public void startCountdownGame(){

        if(countDownTask != null && countDownTask.getStatus() == AsyncTask.Status.FINISHED){
            countDownTask = null;
        }
        if(countDownTask == null) {
            countDownTask = new CountDownTask();
            countDownTask.execute();
            Log.i("onClick", "inside onClick***************");
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

        //preset-up will make count imageView visible and change background color to red
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            im.setVisibility(View.VISIBLE);
            layout.setBackgroundColor(0xffeb0404);
            enablePlayerButtons = false;

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
        nextRoundTask.execute();
        resetCheckValues();
    }
    /********************************************************************************************
    * class creates delay between rounds and starts next round
     ****************************************************************************************/
    class NextRoundTask extends AsyncTask<Void, Integer, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            setUpCurrentSequence();
            playSimonSequence();

        }
    }

}

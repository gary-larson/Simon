package com.example.gary.simon;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener{

    // Set Constants for the three different speeds of the game
    private final int BUTTON_DELAY_1 = 500;
    private final int BUTTON_DELAY_2 = 400;
    private final int BUTTON_DELAY_3 = 300;

    // setup member variables
    private Timer timer;
    private int iIb, iDr, iDelay = BUTTON_DELAY_1;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the listeners for the four buttons (all the same listener)
        ImageButton ib = (ImageButton) findViewById(R.id.topLeft_imageButton);
        ib.setOnClickListener(this);
        ib = (ImageButton) findViewById(R.id.topRight_imageButton);
        ib.setOnClickListener(this);
        ib = (ImageButton) findViewById(R.id.bottomLeft_imageButton);
        ib.setOnClickListener(this);
        ib = (ImageButton) findViewById(R.id.bottomRight_imageButton);
        ib.setOnClickListener(this);
    }

    // method to change image to pressed image and start the timer
    private void setImageButton(int dr) {
        ImageButton ib = (ImageButton) findViewById(iIb);
        ib.setImageResource  (dr);
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new ButtonTask(), iDelay);
        }
    }

    // Method to reset the button to its original state
    private void resetImageButton(int dr) {
        ImageButton ib = (ImageButton) findViewById(iIb);
        ib.setImageResource  (dr);
    }

    // cancel timer if activity loses focus
    @Override
    protected void onPause() {
        super.onPause();
        cancelButton();
    }

    // method to cancel timer
    private void cancelButton() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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

    // method to handle button clicks if timer is running buttons are disabled
    @Override
    public void onClick(View v) {
        if (timer == null) {
            switch (v.getId()) {
                case R.id.topLeft_imageButton:
                    iIb = R.id.topLeft_imageButton;
                    iDr = R.drawable.green_tl;
                    setImageButton(R.drawable.pressed_tl);
                    break;
                case R.id.topRight_imageButton:
                    iIb = R.id.topRight_imageButton;
                    iDr = R.drawable.red_tr;
                    setImageButton(R.drawable.pressed_tr);
                    break;
                case R.id.bottomLeft_imageButton:
                    iIb = R.id.bottomLeft_imageButton;
                    iDr = R.drawable.yellow_bl;
                    setImageButton(R.drawable.pressed_bl);
                    break;
                case R.id.bottomRight_imageButton:
                    iIb = R.id.bottomRight_imageButton;
                    iDr = R.drawable.cyan_br;
                    setImageButton(R.drawable.pressed_br);
                    break;
            }
        }
    }
}

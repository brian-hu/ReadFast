package com.example.android.readfast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * This Activity contains the main game of the app based off of the
 * preferences and files form SettingsActivity
 */
public class GameActivity extends AppCompatActivity {

    private List<String> elementList; //list of all the "elements" (words/sentences) that can appear
    private SharedPreferences pref; //settings from the settings menu
    private File elementFile; //file containing a list of all the elements
    private int timerLength; //length of game time (from prefs)
    private int numOfElements; //number of elements that will appear on the screen (from prefs)
    private boolean wsState; //whether the elements are words or sentences (false = words, true = sentences)
    private List<Integer> elementIds; //List containing the view ids of the textviews created to display elements during game

    private TextView countdownTV; //TextView that counts down before the game starts
    private TextView startGameTV; //TextView that the user clicks on to start the game
    private TextView timerTV; //TextView that shows how much time left
    private Button finishButton; //Button that submits the user's answers
    private EditText answerEditText; //EditText for user to enter answers

    public static final String RESPONSES_KEY = "responseList"; //key for the intent extra that contains the user's resopnses
    public static final String ANSWERS_KEY = "answerList"; //key for the intent extra that contains the elements used in this instance of the game

    /**
     * Overrides the onCreate method, starts the Activity, initializes all
     * instance data and sets anonymous listeners
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //initialize Lists
        elementList = new ArrayList<>();
        elementIds = new ArrayList<>();

        //initialize preferences
        pref = getApplicationContext().getSharedPreferences(SettingsActivity.SHARED_PREFERENCES_FILE, 0);

        //initialize preferences from SharedPreferences
        timerLength = pref.getInt(SettingsActivity.TIMER_KEY, 1);
        wsState = pref.getBoolean(SettingsActivity.WS_STATE_KEY, false);
        numOfElements = pref.getInt(wsState ? SettingsActivity.NUM_OF_SENTENCES_KEY : SettingsActivity.NUM_OF_WORDS_KEY, 1);

        //fills the elementList with elements from elementFile
        File elementFileDir = getDir("element_lists", MODE_PRIVATE);
        elementFile = new File(elementFileDir.getAbsolutePath() +  "/" + (wsState ? "sentences_list.txt" : "words_list.txt"));
        try{
            Scanner sc = new Scanner(elementFile);
            while(sc.hasNext()){
                elementList.add(sc.nextLine());
            }
            sc.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        //intialize Views
        countdownTV = findViewById(R.id.countdownTV);
        timerTV = findViewById(R.id.gameTimerTV);
        startGameTV = findViewById(R.id.startGameTV);
        answerEditText = findViewById(R.id.answer_edit_text);
        finishButton = findViewById(R.id.finishGameButton);

        //set onClickListener for the finish button
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //splits EditText text into separate words and stores them in a List
                String text = answerEditText.getText().toString();
                String[] elementArr = text.replaceAll("[^a-zA-z ]", "").split("\\s+");
                List<String> answersList = new ArrayList<>();
                for(String s : elementArr){
                    answersList.add(s.toUpperCase());
                }
                //Gets the elements that were displayed on the screen from elementIds and stores their String values in another List
                List<String> origElementsList = new ArrayList<>();
                for(int id : elementIds){
                    TextView tv = findViewById(id);
                    if(wsState){ //if sentences, split up the sentence into separate words before adding to the List as well
                        String[] temp = tv.getText().toString().replaceAll("[.!?]", "").split("\\s+");
                        for(String s: temp){
                            origElementsList.add(s);
                        }
                    }
                    else origElementsList.add(tv.getText().toString());
                }

                Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
                intent.putStringArrayListExtra(ANSWERS_KEY, (ArrayList<String>) origElementsList);
                intent.putStringArrayListExtra(RESPONSES_KEY, (ArrayList<String>) answersList);
                startActivity(intent);
            }
        });
    }

    /**
     * onClickListener for the startGame TextView
     * Starts the countdown when the user clicks this textview
     * @param v
     */
    public void onClick(View v){
        startGameTV.setVisibility(TextView.GONE);
        countdown();
    }

    /**
     * Creates a standard 3, 2, 1 countdown on the countdown TextView
     * using a Handler and Runnable to avoid stopping the user's ability to interact with
     * the app
     */
    private void countdown(){
        countdownTV.setVisibility(TextView.VISIBLE);
        final long startTime = System.currentTimeMillis();
        final Handler countdownHandler = new Handler();
        Runnable countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long curTime = System.currentTimeMillis() - startTime;
                int counter = (int) curTime / 1000;
                if (counter < 3) {
                    countdownTV.setText(String.valueOf(3 - counter));
                    countdownHandler.postDelayed(this, 1000);
                }
                else{
                    countdownTV.setVisibility(TextView.INVISIBLE);
                    runTimer();
                    setTextViews();
                    countdownHandler.removeCallbacks(this);
                }
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    /**
     * Creates and initializes the textViews for each element that will be displayed
     */
    private void setTextViews(){
        ConstraintLayout gameLayout = findViewById(R.id.game_layout);
        for(int i = 0; i < numOfElements && i < elementList.size(); i++){
            int pos = (int) (Math.random() * (elementList.size() - i) + i); //calculates a random position after i

            TextView newText = new TextView(this);
            newText.setText(elementList.get(pos));
            newText.setId(View.generateViewId());
            newText.setTextSize(TypedValue.COMPLEX_UNIT_SP, wsState ? 40 : 30);
            if(wsState) {
                newText.setWidth(convertToPX(220));
                newText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            newText.setVisibility(View.INVISIBLE);
            elementIds.add(newText.getId());
            gameLayout.addView(newText);
            Collections.swap(elementList, i, pos); //swaps the element at pos with the element at i to assure that the element of pos is not used again
        }
        positionTextView();
    }

    /**
     * Randomly positions the TextViews referenced from the ids in the List elementIds
     */
    private void positionTextView(){
        for(int id : elementIds){
            final TextView temp = findViewById(id);
            //must use .post() method to get height and width of textViews for calculations, since they are set after the UI has been laid out
            temp.post(new Runnable() {
                @Override
                public void run() {
                    temp.setVisibility(View.VISIBLE);
                    do{
                        Point p = calculateRandPosition(temp);
                        temp.setX(p.x);
                        temp.setY(p.y);
                    } while(findIfViewsOverlaps(temp, elementIds));


                }
            });
        }
    }

    /**
     * Hides all elements referenced by the ids in the list elementIds
     */
    private void hideAllElements(){
        for(int id: elementIds){
            TextView temp = findViewById(id);
            temp.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Calculates a random position given a TextView
     * This method should only be used after the UI has been set and View width/height have been set
     * otherwise the View may be positioned off screen
     * @param view is the View that is being positioned
     * @return a Point containing the coordinates of the random position
     */
    private Point calculateRandPosition(final View view){
        int activityHeight = Resources.getSystem().getDisplayMetrics().heightPixels - getSoftKeyButtonsHeight();
        int activityWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int tvHeight = view.getHeight();
        int tvWidth = view.getWidth();
        int x = (int) (Math.random() * (activityWidth - tvWidth));
        int y = (int) (Math.random() * (activityHeight - tvHeight));

        return new Point(x, y);
    }

    /**
     * Starts a timer based off of timerLength in the timer TextView
     * Utilizes Handler and Runnable to avoid stopping the user from interacting
     * with the app
     */
    private void runTimer(){
        timerTV.setVisibility(View.VISIBLE);
        final long startTime = System.currentTimeMillis(); //stores the system time of when the timer starts
        final Handler timerHandler = new Handler();
        final Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long curTime = System.currentTimeMillis() - startTime; //stores how much time has passed since the timer started
                int timerVal = (int) (timerLength * 1000 - curTime); //calculates the amount of time left
                if(timerLength * 1000 > curTime){
                    if(timerVal <= 3000) timerTV.setTextColor(Color.RED);
                    timerTV.setText(String.format("%d.%d", timerVal / 1000, timerVal % 1000 / 100));
                    timerHandler.postDelayed(this, 100);
                }
                else{
                    timerTV.setVisibility(TextView.INVISIBLE);
                    hideAllElements();
                    loadAnswerScreen();
                    timerHandler.removeCallbacks(this);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    /**
     *
     * @param v is the View being tested for overlap
     * @param otherViewIds is the List of all the other Views currently on the screen
     * @return true if there is overlap, false if there is not
     */
    private boolean findIfViewsOverlaps(View v, List<Integer> otherViewIds){
        List<Integer> tempIdList = new ArrayList<>();
        for(int id : otherViewIds){
            tempIdList.add(id);
        }
        tempIdList.add(timerTV.getId());
        int[] viewPos = new int[2];
        v.getLocationOnScreen(viewPos);
        for(int id : tempIdList){
            View otherView = findViewById(id);
            if(id != v.getId() && otherView.getVisibility() == View.VISIBLE) {
                int[] otherViewPos = new int[2];
                otherView.getLocationOnScreen(otherViewPos);
                Rect viewRect = new Rect(viewPos[0] - 15, viewPos[1] - 15, viewPos[0] + v.getWidth() + 15, viewPos[1] + v.getHeight() + 15);
                Rect otherViewRect = new Rect(otherViewPos[0] - 15, otherViewPos[1] - 15, otherViewPos[0] + otherView.getWidth()  + 15, otherViewPos[1] + otherView.getHeight() + 15);
                if (viewRect.intersect(otherViewRect)) return true;
            }
        }
        return false;
    }

    /**
     * Gets the height of the soft key buttons
     * @return height of the soft key buttons
     */
    private int getSoftKeyButtonsHeight(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int usableHeight = dm.heightPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int realHeight = dm.heightPixels;
        if(realHeight > usableHeight) return realHeight - usableHeight;
        else return 0;
    }

    /**
     * Makes the EditText and Button that allow the user to answer and submit his/her
     * response visible
     */
    private void loadAnswerScreen(){
        answerEditText.setVisibility(View.VISIBLE);
        finishButton.setVisibility(View.VISIBLE);
    }

    /**
     * Converts density-independent pixels to pixels
     * @param dp
     * @return
     */
    private int convertToPX(int dp){
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * Overrides the onBacPressed method
     * Sends the user to the MainMenuActivity so that the user cannot
     * replay the game
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(GameActivity.this, MainMenu.class);
        startActivity(intent);
    }
}

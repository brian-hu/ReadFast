package com.example.android.readfast;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This Activity calculates the accuracy of the user's answers compared
 * to the elements displayed on the screen during the GameActivity and displays
 * this information to the user
 */
public class ResultsActivity extends AppCompatActivity {

    private List<String> answerKeyList; //List of the correct elements that were displayed during the game
    private List<String> userAnswersList; //List of the user's responses
    private List<String> incorrectElements; //List of all the elements that the user gets incorrect
    private Button playAgainButton; //Button that allows the user to play again with the same settings
    private Button homeButton; //Button that send the user back to the main menu
    private TextView percentageDisplay; //TextView that displays the percentage of words the user got correct

    /**
     * Overrides the onCreate method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        //initialize Lists
        answerKeyList = getIntent().getStringArrayListExtra(GameActivity.ANSWERS_KEY); //gets list from GameActivity via intent extras
        userAnswersList = getIntent().getStringArrayListExtra(GameActivity.RESPONSES_KEY); //gets list from GameActivity via intent extras
        incorrectElements = new ArrayList<>();

        //initializeViews
        percentageDisplay = findViewById(R.id.percentage_tv);
        playAgainButton = findViewById(R.id.play_again_button);
        homeButton = findViewById(R.id.home_button);

        //sets onClickListener for the play again button
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultsActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });
        //sets onClickListener for the home button
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultsActivity.this, MainMenu.class);
                startActivity(intent);
            }
        });

        //calculate the accuracy of the user's responses and display it
        double percent = calculateAccuracy();
        percentageDisplay.setText(String.format("Percentage Correct: %.1f%%", percent));
        populateColumns();
    }

    /**
     * Fills the two columns on the screen, the left column contains the correct answers
     * while the right column contains the user's responses
     */
    private void populateColumns(){
        LinearLayout origElementLayout = findViewById(R.id.original_elements_layout); //layout of the "left" column, containing the correct answers
        LinearLayout responseElementLayout = findViewById(R.id.response_elements_layout); //layout of the "right" column, containing user responses
        List<String> missedElements = new ArrayList<>(); //List that will contain all the words missed by the user
        //first loop displays all the correctly answered words side by side
        for(String s : answerKeyList){
            if(userAnswersList.contains(s)){
                TextView origTV = new TextView(this);
                TextView responseTV = new TextView(this);
                origTV.setText(s);
                responseTV.setText(s);
                origElementLayout.addView(origTV);
                responseElementLayout.addView(responseTV);
            }
            else missedElements.add(s); //add all missed words to the missedElements List to be displayed later
        }
        //second loop displays all the elements missed by the user in red in the left column
        for(String s : missedElements){
            TextView textView = new TextView(this);
            textView.setText(s);
            textView.setTextColor(Color.RED);
            origElementLayout.addView(textView);
        }
        //third loop displays all the elements entered by the user that were incorrect in red in the right column
        for(String s : incorrectElements){
            TextView textView = new TextView(this);
            textView.setText(s);
            textView.setTextColor(Color.RED);
            responseElementLayout.addView(textView);
        }
    }

    /**
     * Calculates the accuracy of the user's responses
     * @return the percentage of words that the user got correct
     */
    private double calculateAccuracy(){
        int correct = 0;
        for(String s : userAnswersList){
            if(answerKeyList.contains(s)) correct++;
            else incorrectElements.add(s);
        }
        return ((double) correct / answerKeyList.size()) * 100;
    }

    /**
     * Overrides the onBackPressed method to send the user back to the main menu
     * instead of GameActivity when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ResultsActivity.this, MainMenu.class);
        startActivity(intent);
    }
}

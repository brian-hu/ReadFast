package com.example.android.readfast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs; //SharedPreferences reference
    private SharedPreferences.Editor editor; //SharedPreferences editor
    private Switch wsSwitch; //Switch that can change the configuration of the app between words and sentences
    private Spinner numOfElementSpinner; //Spinner that contains options for number of elements
    private Spinner timerSpinner; //Spinner that contains options for timer length
    private TextView numOfElementsTV; //TextView that displays title for numOfElementSpinner
    private Button editElementListButton; //Button that opens up list editing activity

    /**
     * This constant is the key to access the current number of words setting in the SharedPreferences file myPrefs
     */
    public static final String NUM_OF_WORDS_KEY = "numOfWords";
    /**
     * This constant is the key to access the current number of sentences setting in the SharedPreferences file myPrefs
     */
    public static final String NUM_OF_SENTENCES_KEY = "numOfSentences";
    /**
     * This constant is the key to access the current timer length setting in the SharedPreferences file myPrefs
     */
    public static final String TIMER_KEY = "timer";
    /**
     * This constant is the key to access the current words/sentences configuration setting in the SharedPreferences file myPrefs
     */
    public static final String WS_STATE_KEY = "isWS";
    /**
     * This constant is the key to access the current timer default value setting in the SharedPreferences file myPrefs
     */
    public static final int TIMER_SPINNER_DEFAULT_VAL = 5;
    /**
     * This constant is the key to access the current default number of words setting in the SharedPreferences file myPrefs
     */
    public static final int NUM_OF_WORDS_DEFAULT_VAL = 3;
    /**
     * This constant is the key to access the current default number of sentences setting in the SharedPreferences file myPrefs
     */
    public static final int NUM_OF_SENTENCES_DEFAULT_VAL = 1;
    /**
     * This constant is name of the SharedPreferences file
     */
    public static final String SHARED_PREFERENCES_FILE = "myPrefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //initializing SharedPreferences and its editor
        prefs = getApplication().getSharedPreferences(SHARED_PREFERENCES_FILE, 0);
        editor = prefs.edit();


        //initializing wsSwitch
        wsSwitch = findViewById(R.id.wordsSentencesSwitch);
        if(prefs.getBoolean("firstTime", true)){ //sets defaults for the first time the app runs
            editor.putBoolean(WS_STATE_KEY, false);
        }
        else{
            wsSwitch.setChecked(prefs.getBoolean(WS_STATE_KEY, false));
        }

        //initializes numOfElements TextView based on state of wsSwitch
        numOfElementsTV = findViewById(R.id.numberOfElementsTextView);
        numOfElementsTV.setText(wsSwitch.isChecked() ? "Number of Sentences:" : "Number of Words:");

        //initializes numOfElementSpinner based on state of wsSwitch
        numOfElementSpinner = findViewById(R.id.elementSpinner);
        int eleSpinID = wsSwitch.isChecked() ? R.array.sentencesSelectionValues : R.array.wordsSelectionValues;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, eleSpinID, android.R.layout.simple_spinner_dropdown_item);
        numOfElementSpinner.setAdapter(adapter);
        if(prefs.getBoolean("firstTime", true)){ //sets defaults for the first time the app runs
            editor.putInt(NUM_OF_WORDS_KEY, NUM_OF_WORDS_DEFAULT_VAL);
            editor.putInt(NUM_OF_SENTENCES_KEY, NUM_OF_SENTENCES_DEFAULT_VAL);
            numOfElementSpinner.setSelection((wsSwitch.isChecked() ? NUM_OF_SENTENCES_DEFAULT_VAL : NUM_OF_WORDS_DEFAULT_VAL) - 1);
        }
        else{
            numOfElementSpinner.setSelection((wsSwitch.isChecked() ? prefs.getInt(NUM_OF_SENTENCES_KEY, NUM_OF_SENTENCES_DEFAULT_VAL) : prefs.getInt(NUM_OF_WORDS_KEY, NUM_OF_WORDS_DEFAULT_VAL)) - 1);
        }

        //initializes timer spinner based on state of wsSwitch
        timerSpinner = findViewById(R.id.timerSpinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.timerSelectionValues, android.R.layout.simple_spinner_dropdown_item);
        timerSpinner.setAdapter(adapter);
        if(prefs.getBoolean("firstTime", true)){ //sets defaults for the first time the app runs
            editor.putInt(TIMER_KEY, TIMER_SPINNER_DEFAULT_VAL);
            timerSpinner.setSelection(TIMER_SPINNER_DEFAULT_VAL - 1);
            editor.putBoolean("firstTime", false);
        }
        else{
            timerSpinner.setSelection((prefs.getInt(TIMER_KEY, TIMER_SPINNER_DEFAULT_VAL)) - 1);

        }
        editor.apply();

        //initializes elementListbutton
        editElementListButton = findViewById(R.id.elementListEditButton);

        //elementSpinner listener methods
        numOfElementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt(wsSwitch.isChecked() ? NUM_OF_SENTENCES_KEY : NUM_OF_WORDS_KEY, Integer.valueOf(numOfElementSpinner.getItemAtPosition(i).toString()));
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //timerSpinner listener methods
        timerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt(TIMER_KEY, i + 1);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //elementLsitButton listener methods
        editElementListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, WordListActivity.class);
                startActivity(intent);
            }
        });

        //wsSwitch listener methods
        wsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true) {
                    numOfElementsTV.setText("Number of Sentences:");
                    editElementListButton.setText("EDIT SENTENCES LIST");
                }
                else {
                    numOfElementsTV.setText("Number of Words:");
                    editElementListButton.setText("EDIT WORD LIST");
                }
                ArrayAdapter<CharSequence> tempAdapter = ArrayAdapter.createFromResource(getApplicationContext(), b ? R.array.sentencesSelectionValues : R.array.wordsSelectionValues, android.R.layout.simple_spinner_dropdown_item);
                numOfElementSpinner.setAdapter(tempAdapter);
                numOfElementSpinner.setSelection((b ? prefs.getInt(NUM_OF_SENTENCES_KEY, NUM_OF_SENTENCES_DEFAULT_VAL) : prefs.getInt(NUM_OF_WORDS_KEY, NUM_OF_WORDS_DEFAULT_VAL)) - 1);
                editor.putInt(b ? NUM_OF_SENTENCES_KEY : NUM_OF_WORDS_KEY, b ? prefs.getInt(NUM_OF_SENTENCES_KEY, NUM_OF_SENTENCES_DEFAULT_VAL) : prefs.getInt(NUM_OF_WORDS_KEY, NUM_OF_WORDS_DEFAULT_VAL));
                editor.putBoolean(WS_STATE_KEY, b);
                editor.apply();
            }
        });
    }
}

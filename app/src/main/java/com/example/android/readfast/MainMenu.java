package com.example.android.readfast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

/**
 * This is the launch Activity of the game. It provides options to either
 * play a game or modify the current settings
 */
public class MainMenu extends AppCompatActivity {

    private Button play; //play button
    private Button settings; //settings button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //creates new element list files for words and sentences if they don't already exist
        File elementListDir = getDir("element_lists", MODE_PRIVATE);
        try{
            new File(elementListDir.getAbsolutePath() + "/word_list.txt").createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        try{
            new File(elementListDir.getAbsolutePath() + "/sentence_list.txt").createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }

        //initializes the play button and sets onClickListener
        play = findViewById(R.id.playButton);
        play.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent temp = new Intent(MainMenu.this, GameActivity.class);
                startActivity(temp);
            }
        });

        //initializes the settings button and sets onClickListener
        settings = (findViewById(R.id.wordsButton));
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent temp = new Intent(MainMenu.this, SettingsActivity.class);
                startActivity(temp);
            }
        });
    }



}

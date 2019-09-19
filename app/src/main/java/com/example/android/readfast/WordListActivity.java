package com.example.android.readfast;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * This Activity provides a UI for editing the list of possible elements that will appear
 * during the game. The user can add and remove individual elements, or multiple at once.
 */
public class WordListActivity extends AppCompatActivity implements ElementListAdapter.ListItemListener, ElementListDialog.ElementListDialogListener{

    private RecyclerView elementListRV; //RecyclerView that allows scrolling functionality through elements
    private List<String> elementList; //List of all the elements
    private File elementFile; //elementFile containing the full list of elements
    private boolean wsState; //boolean determining whether the app is configured for words or sentences (false = words, true = sentences)
    private Button addButton; //Button to add elements
    private SharedPreferences prefs; //SharedPreferences to access preferences set in the SettingsActivity
    private ElementListAdapter adapter; //adapter that connects the Activity with the Views within the RecyclerView
    private ActionBar actionBar; //ActionBar on the top of the screen
    private List<String> deleteElementsList; //List of items checked and to be deleted
    private Menu actionBarMenu; //Menu of options on the ActionBar

    /**
     * Overrides onCreate method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

        //removes Title from the ActionBar
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        //initialize Lists
        elementList = new ArrayList<>();
        deleteElementsList = new ArrayList<>();

        //initializes SharedPreferences and initialize corresponding instance data
        prefs = getApplicationContext().getSharedPreferences(SettingsActivity.SHARED_PREFERENCES_FILE, 0);
        wsState = prefs.getBoolean(SettingsActivity.WS_STATE_KEY, false);

        //populates elementList with elements from elementFile
        File elementListDir = getDir("element_lists", MODE_PRIVATE);
        elementFile = new File(elementListDir.getAbsolutePath() + "/" + (wsState ? "sentences_list.txt" : "words_list.txt"));
        updateElementList(elementFile, elementList);

        //intializes and inflates RecyclerView
        elementListRV = findViewById(R.id.wordListRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        elementListRV.setLayoutManager(layoutManager);
        adapter = new ElementListAdapter(this);
        elementListRV.setAdapter(adapter);
        adapter.setElements(elementList);

        //initializes add button
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creates and display a dialog box
                DialogFragment fragment = new ElementListDialog();
                fragment.show(getFragmentManager(),"add elements");
            }
        });
    }

    /**
     * Overrides the OnListItemLongClock method
     * Creates and displays a popup menu when a list item is long clicked
     * @param element is the element contained by the list item
     * @param v is the view that was clicked
     */
    @Override
    public void onListItemLongClick(final String element, View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.delete_popup_menu_option:
                        removeElement(elementFile, element);
                        updateElementList(elementFile, elementList);
                        adapter.setElements(elementList);
                        return true;
                    case R.id.define_popup_menu_option:
                        return true;
                }
                return false;
            }
        });
        popup.inflate(R.menu.element_list_popup_menu);
        popup.show();
    }

    /**
     * Adds or removes the selected element from deleteElementsList based on whether the ListItem was checked or unchecked
     * @param element is the element that was selected
     * @param add determines whether the check box was checked or unchecked (false = unchecked, true = checked)
     */
    @Override
    public void onListItemCheckedChanged(String element, boolean add) {
        MenuItem deleteAllMenuItem = actionBarMenu.findItem(R.id.delete_all_actionbar_menu_option);
        if(add) deleteElementsList.add(element);
        else deleteElementsList.remove(element);
        deleteAllMenuItem.setVisible(deleteElementsList.size() > 0);
    }

    /**
     * Adds all the elements within the given String based on whether the app is currently configured for words or sentences
     * If sentences: splits the string at punctuation and adds full sentences
     * If words: splits the string at every space
     * @param newElements is the String containing the new elements
     */
    @Override
    public void addNewElements(String newElements) {
        String[] newElementsArr = wsState ? newElements.replaceAll("\\s+", " ").split("(?<=[!.?])") : newElements.replaceAll("[^a-zA-Z ]","").split("\\s+");
        for(String ele : newElementsArr){
            addElement(elementFile, ele.trim());
        }
        updateElementList(elementFile, elementList);
        adapter.setElements(elementList);
    }

    /**
     * Inflates the action bar menu
     * @param menu is the menu
     * @return true if method is successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.element_actionbar_menu, menu);
        actionBarMenu = menu;
        return true;
    }

    /**
     * Provides different functionality based on which item was selected
     * Clear: clears the entire element list
     * Delete Selcted: deletes all the checked items (elements in deleteElementList)
     * @param item is the selected MenuItem
     * @return true if valid option is selected, otherwise false
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.clear_actionbar_menu_option:
                clearFile(elementFile);
                updateElementList(elementFile, elementList);
                adapter.setElements(elementList);
                return true;
            case R.id.delete_all_actionbar_menu_option:
                for(String element : deleteElementsList){
                    removeElement(elementFile, element);
                }
                updateElementList(elementFile, elementList);
                adapter.setElements(elementList);
                return true;
        }
        return false;
    }

    /**
     * Adds a single element to a file
     * @param f is the file
     * @param element is the element to be added
     */
    private static void addElement(File f, String element){
        if(!element.isEmpty()){
            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
                writer.append(element.toUpperCase());
                writer.newLine();
                writer.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        clearRepeats(f);
    }

    /**
     * Removes all instances of an element from a file
     * @param f is the file
     * @param element is the element to be removed
     */
    private static void removeElement(File f, String element){
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try{
            File temp = new File(f.getAbsolutePath() + ".tmp");
            reader = new BufferedReader(new FileReader(f));
            writer = new BufferedWriter(new FileWriter(temp));
            String curLine;
            boolean wordFound = false;
            while((curLine = reader.readLine()) != null){
                if(curLine.trim().equalsIgnoreCase(element) && !wordFound) {
                    wordFound = true;
                    continue;
                }
                writer.write(curLine.toUpperCase());
                writer.newLine();
            }
            temp.renameTo(f);
        } catch(IOException e){
            e.printStackTrace();
        } finally {
            try{
                reader.close();
                writer.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the elementList based on a file
     * @param f is the file
     */
    private static void updateElementList(File f, List<String> elementList){
        elementList.clear();
        Scanner sc = null;
        try {
            sc = new Scanner(f);
            while(sc.hasNext()){
                elementList.add(sc.nextLine());
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(sc != null) sc.close();
        }
    }

    /**
     * Removes all duplicate elements from the file
     * @param f is the file
     */
    private static void clearRepeats(File f){
        Set<String> foundWords = new HashSet<>();
        List<String> repeats = new ArrayList<>();
        Scanner sc = null;
        try {
            sc = new Scanner(f);
            while(sc.hasNext()){
                String s = sc.nextLine();
                if(foundWords.contains(s)) repeats.add(s);
                foundWords.add(s);
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(sc != null) sc.close();
        }
        for(String s : repeats){
            removeElement(f, s);
        }
    }

    /**
     * Clears the entire file
     * @param f is the file
     */
    private static void clearFile(File f){
        f.delete();
        try{
            new File(f.getAbsolutePath()).createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

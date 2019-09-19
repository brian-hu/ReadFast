package com.example.android.readfast;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * This DialogFragment provides the user a space to add new elements
 */
public class ElementListDialog extends DialogFragment {
    /**
     * This interface provides functionality for when the user submits the dialog
     */
    public interface ElementListDialogListener{
        void addNewElements(String elements);
    }

    private ElementListDialogListener listener;
    private EditText editText;

    /**
     * Attemps to initialize listener based off of context
     * @param context is the context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (ElementListDialogListener) context;
        } catch(ClassCastException e){
            throw new ClassCastException(getActivity().toString()
                    + " must implement ElementListDialogListener");
        }
    }

    /**
     * Builds and inflates a new Dialog
     * @param savedInstanceState
     * @return the Dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_element_dialog, null);
        builder.setView(v)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.addNewElements(getEditTextString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ElementListDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    /**
     * gets the text in the EditText box
     * @return the text
     */
    private String getEditTextString(){
        editText = getDialog().findViewById(R.id.add_elements_edit_text);
        return editText.getText().toString();
    }
}

package com.example.android.readfast;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class functions as an adapter to populate the RecyclerView with Views and provides
 * access and functionality to the Views
 */
public class ElementListAdapter extends RecyclerView.Adapter<ElementListAdapter.ElementListAdapterViewHolder> {

    private List<String> elements;
    private final ListItemListener listener;

    /**
     * Constructs an ElementListAdapter with given listener
     * @param listener is a ListItemListener
     */
    public ElementListAdapter(ListItemListener listener) {
        this.listener = listener;
    }

    /**
     * This interface defines methods for when the ListItem is clicked, long clicked and or checkboxed
     */
    public interface ListItemListener {
        void onListItemLongClick(String word, View v);
        void onListItemCheckedChanged(String word, boolean add);
    }

    /**
     * Custom ViewHolder class that sets Views within each row of the RecyclerView
     */
    public class ElementListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public final TextView elementTextView; //TextView that will contain the element
        public final CheckBox elementCheckBox; //Checkbox for selecting multiple elements at once

        /**
         * Constructs an ElementListAdapterViewHolder in View v
         * @param v the View that the ViewHolder is constructed within
         */
        public ElementListAdapterViewHolder(View v) {
            super(v);
            //initialize Views
            elementTextView = v.findViewById(R.id.elementTextView);
            elementCheckBox = v.findViewById(R.id.elementCheckBox);
            //sets onCheckedChangeListener for check box, which calls ListItemListener's onCheckedChanged method
            elementCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String str = elementTextView.getText().toString();
                    listener.onListItemCheckedChanged(str, b);
                }
            }); //sets onCheckedChangedListener to this ViewHolder
            v.setOnLongClickListener(this); //sets OnLongClickListener to this ViewHolder
        }

        /**
         * OnLongClick listener for this ViewHolder, calls onListItemLongClick using the ListItem listener in ElementAdapter
         * @param view that was clicked
         * @return true if method succeeds
         */
        @Override
        public boolean onLongClick(View view) {
            String text = elementTextView.getText().toString();
            listener.onListItemLongClick(text, view);
            return true;
        }
    }

    /**
     * overrides onCreateViewHolder which create an ElementListViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ElementListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int itemID = R.layout.element_list_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(itemID, parent, false);
        return new ElementListAdapterViewHolder(view);
    }

    /**
     * Gets the number of elements(items)
     * @return the number of elements(items)
     */
    @Override
    public int getItemCount() {
        if (elements == null) return 0;
        return elements.size();
    }

    /**
     * Overrides onBindViewHolder
     * Sets the TextView of a ViewHolder to the element at the same position
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ElementListAdapterViewHolder holder, int position) {
        String element = elements.get(position);
        holder.elementTextView.setText(element);
    }

    /**
     * Sets the elements based on List of strings
     * @param elementList is the List
     */
    public void setElements(List<String> elementList){
        if (elementList != null) {
            elements = new ArrayList<>();
            elements.addAll(elementList);
        }
        notifyDataSetChanged();
    }
}
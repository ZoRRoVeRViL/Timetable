package com.example.timetable.fragments_settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.timetable.R;

public class CustomArrayAdapter<T> extends ArrayAdapter<T>
{
    private final LayoutInflater inflater;
    private final int itemLayoutResource;
    private int dropdownLayoutResource;

    public CustomArrayAdapter(Context context, int itemLayoutResource, T[] objects)
    {
        super(context, itemLayoutResource, objects);
        this.itemLayoutResource = itemLayoutResource;
        this.inflater = LayoutInflater.from(context);
    }

    public void setDropdownLayoutResource(int dropdownLayoutResource)
    {
        this.dropdownLayoutResource = dropdownLayoutResource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = inflater.inflate(itemLayoutResource, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.spinner_item_text);
        T item = getItem(position);

        if (item != null)
        {
            textView.setText(item.toString());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = inflater.inflate(dropdownLayoutResource, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.spinner_dropdown_item_text);
        T item = getItem(position);

        if (item != null)
        {
            textView.setText(item.toString());
        }

        return convertView;
    }
}
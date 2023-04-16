package com.example.miamproche;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {
    List<String> mData;
    List<String> mFilteredData;
    private LayoutInflater inflater;

    public ListAdapter(List<String> data) {
        mData = data;
        mFilteredData = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mFilteredData.size();
    }

    @Override
    public String getItem(int position) {
        return mFilteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_item, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.stringName)).setText(mFilteredData.get(position));
        return convertView;
    }

    public void filter(String constraint) {
        mFilteredData = new ArrayList<>();
        if (constraint != null && constraint.length() > 0) {
            for (String value : mData) {
                if (value.toUpperCase().contains(constraint.toUpperCase())) {
                    mFilteredData.add(value);
                }
            }
        }
        notifyDataSetChanged();
    }
}

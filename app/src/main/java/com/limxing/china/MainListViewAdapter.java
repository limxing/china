package com.limxing.china;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by limxing on 16/4/10.
 */
public class MainListViewAdapter extends BaseAdapter {
    private List<String> list;
    private Context context;

    public MainListViewAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView;
        if (view == null) {
            textView = new TextView(context);
        } else {
            textView = (TextView) view;
        }
        textView.setPadding(10, 20, 10, 20);
        textView.setText(list.get(i));
        return textView;
    }
}

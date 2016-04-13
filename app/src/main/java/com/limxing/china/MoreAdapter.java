package com.limxing.china;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by limxing on 16/4/13.
 */
public class MoreAdapter extends BaseAdapter {
    private final List<MoreBean> list;
    private final Context context;

    public MoreAdapter(List<MoreBean> list, Context context) {
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
    public View getView(int i, View convertview, ViewGroup viewGroup) {
        final MoreBean bean=list.get(i);
        View view;
        Holder holder = null;
        if (convertview == null) {
            holder = new Holder();
            view = View.inflate(context, R.layout.more_item, null);
            holder.more_item_name = (TextView) view.findViewById(R.id.more_item_name);
            holder.more_item_checkbox = (CheckBox) view.findViewById(R.id.more_item_checkbox);
            view.setTag(holder);
        } else {
            view = convertview;
            holder = (Holder) convertview.getTag();
        }
        holder.more_item_name.setText(new File(bean.getPath()).getName());
        holder.more_item_checkbox.setChecked(bean.isChecked());
        holder.more_item_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                bean.setChecked(b);
            }
        });
        return view;
    }

    class Holder {
        TextView more_item_name;
        CheckBox more_item_checkbox;
    }
}

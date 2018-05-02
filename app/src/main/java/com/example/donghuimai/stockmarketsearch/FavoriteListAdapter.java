package com.example.donghuimai.stockmarketsearch;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.donghuimai.stockmarketsearch.model.FavoriteItem;

import java.util.List;

/**
 * Created by donghuimai on 11/15/17.
 */

public class FavoriteListAdapter extends BaseAdapter {

    private Context context;
    private List<FavoriteItem> data;

    public FavoriteListAdapter(Context context, List<FavoriteItem> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.favorite_list_item, viewGroup, false);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 140;
        FavoriteItem item = data.get(i);
        TextView symbol = (TextView) view.findViewById(R.id.symbol);
        symbol.setText(item.getSymbol());
        TextView price = (TextView) view.findViewById(R.id.price);
        price.setText(item.getPrice());
        TextView change = (TextView) view.findViewById(R.id.change);
        String c = item.getChange();
        double cd = Double.parseDouble(c.substring(0, c.indexOf('(') - 1));
        if (cd < 0) {
            change.setTextColor(Color.parseColor("#FF0000"));
        } else {
            change.setTextColor(Color.parseColor("#00FF00"));
        }
        change.setText(item.getChange());
        return view;
    }
}

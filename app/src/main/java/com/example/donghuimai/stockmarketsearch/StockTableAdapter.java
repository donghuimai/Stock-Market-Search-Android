package com.example.donghuimai.stockmarketsearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.donghuimai.stockmarketsearch.model.TableItem;

import java.util.List;
import java.util.Map;

/**
 * Created by donghuimai on 11/17/17.
 */

public class StockTableAdapter extends BaseAdapter {

    private Context context;
    private List<TableItem> data;

    public StockTableAdapter(@NonNull Context context, List<TableItem> data) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.stock_table_item, viewGroup, false);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 140;
        TextView table_header = (TextView) view.findViewById(R.id.table_header);
        TextView table_col = (TextView) view.findViewById(R.id.table_col);
        TableItem item = data.get(i);
        table_header.setText(item.getHeader());
        table_col.setText(item.getData());
        if (i == 2) {
            ImageView image = (ImageView) view.findViewById(R.id.updown);
            if (Double.parseDouble(item.getData().substring(0, item.getData().indexOf('(') - 1)) < 0) {
                image.setImageResource(R.drawable.down);
            } else {
                image.setImageResource(R.drawable.up);
            }
            image.setVisibility(View.VISIBLE);
        }
        return view;
    }
}

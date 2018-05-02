package com.example.donghuimai.stockmarketsearch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.donghuimai.stockmarketsearch.model.NewsItem;

import java.util.List;

/**
 * Created by donghuimai on 11/18/17.
 */

public class NewsAdapter extends BaseAdapter {
    private Context context;
    private List<NewsItem> data;

    public NewsAdapter(Context context, List<NewsItem> data) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, viewGroup, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView author = (TextView) view.findViewById(R.id.author);
        TextView date = (TextView) view.findViewById(R.id.date);
        NewsItem item = data.get(i);
        title.setText(item.getTitle());
        author.setText("Author: " + item.getAuthor());
        date.setText("Date: " + item.getDate());
        return view;

    }
}

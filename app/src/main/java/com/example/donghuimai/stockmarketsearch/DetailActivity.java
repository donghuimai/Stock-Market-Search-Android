package com.example.donghuimai.stockmarketsearch;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.donghuimai.stockmarketsearch.TabsPagerAdapter;

/**
 * Created by donghuimai on 11/17/17.
 */

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String symbol = getIntent().getStringExtra("symbol").toUpperCase();

        getSupportActionBar().setTitle(symbol);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsPagerAdapter adapter = new TabsPagerAdapter(getSupportFragmentManager());
        Bundle args1 = new Bundle();
        args1.putString("symbol", symbol);
        CurrentFragment curr = new CurrentFragment();
        curr.setArguments(args1);
        adapter.addFrag(curr, "CURRENT");
        Bundle args2 = new Bundle();
        args2.putString("symbol", symbol);
        HistoricalFragment hist = new HistoricalFragment();
        hist.setArguments(args2);
        adapter.addFrag(hist, "HISTORICAL");
        Bundle args3 = new Bundle();
        args3.putString("symbol", symbol);
        NewsFragment news = new NewsFragment();
        news.setArguments(args3);
        adapter.addFrag(news, "NEWS");
        adapter.notifyDataSetChanged();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabbar);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

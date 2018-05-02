package com.example.donghuimai.stockmarketsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donghuimai.stockmarketsearch.model.NewsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by donghuimai on 11/17/17.
 */

public class NewsFragment extends Fragment {

    private final static String SERVER = "http://hw8server-env.us-east-1.elasticbeanstalk.com";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);
        rootView.findViewById(R.id.news_table).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.error_message).setVisibility(View.GONE);
        String symbol = getArguments().getString("symbol");
        getData(rootView, symbol);
        return rootView;
    }

    private void getData(final View view, String symbol) {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String newsUrl = SERVER + "/?news=" + symbol;
        JsonObjectRequest newsRequest = new JsonObjectRequest
                (Request.Method.GET, newsUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            renderNewsFeed(view, response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        view.findViewById(R.id.news_table).setVisibility(View.GONE);
                        view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                        view.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
                    }
                });
        requestQueue.add(newsRequest);
    }

    private void renderNewsFeed(final View view, JSONObject response) throws JSONException {
        if (!response.has("rss")) {
            view.findViewById(R.id.news_table).setVisibility(View.GONE);
            view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
            view.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
            return;
        }
        JSONArray newsLists = response.getJSONArray("rss").getJSONObject(0)
                .getJSONArray("channel").getJSONObject(0).getJSONArray("item");
        final List<NewsItem> data = new ArrayList<>();
        for (int i = 0; i < newsLists.length(); i++) {
            JSONObject news = newsLists.getJSONObject(i);
            String link = news.getJSONArray("link").get(0).toString();
            if (link.indexOf("article") != -1) {
                String title = news.getJSONArray("title").get(0).toString();
                String author = news.getJSONArray("sa:author_name").get(0).toString();
                String date = news.getJSONArray("pubDate").get(0).toString();
                date = date.substring(0, date.indexOf('-') - 1);
                date += " PST";
                NewsItem item = new NewsItem(title, author, date, link);
                data.add(item);
            }
        }
        ListView listView = (ListView) view.findViewById(R.id.news_table);
        final NewsAdapter adapter = new NewsAdapter(getContext(), data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NewsItem item = data.get(i);
                String url = item.getLink();
                Uri uri = Uri.parse(url);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
        view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }
}

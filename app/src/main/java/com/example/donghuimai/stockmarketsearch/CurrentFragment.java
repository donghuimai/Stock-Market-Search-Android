package com.example.donghuimai.stockmarketsearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donghuimai.stockmarketsearch.model.FavoriteItem;
import com.example.donghuimai.stockmarketsearch.model.TableItem;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by donghuimai on 11/14/17.
 */

public class CurrentFragment extends Fragment {

    private final static String SERVER = "http://hw8server-env.us-east-1.elasticbeanstalk.com";
    private final static DecimalFormat df2 = new DecimalFormat("0.00");
    private List<TableItem> data = new ArrayList<>();
    private String postChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_current, container, false);
        view.findViewById(R.id.stock_table).setVisibility(View.VISIBLE);
        view.findViewById(R.id.error_message).setVisibility(View.GONE);
        df2.setMinimumFractionDigits(2);
        final String symbol = getArguments().getString("symbol").toUpperCase();
        setFavoriteBtn(symbol, view);
        getData(symbol, view);
        loadIndicatorChart(symbol, view);
        final String prev_indicator = ((Spinner) view.findViewById(R.id.indicator)).getSelectedItem().toString();
        ((Spinner) view.findViewById(R.id.indicator)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {
                String curr_indicator = ((Spinner) view.findViewById(R.id.indicator)).getSelectedItem().toString();
                if (!curr_indicator.equals(prev_indicator)) {
                    view.findViewById(R.id.changeBtn).setEnabled(true);
                } else {
                    view.findViewById(R.id.changeBtn).setEnabled(false);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        view.findViewById(R.id.changeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadIndicatorChart(symbol, view);

            }
        });
        setShareToFB(symbol, view);
        return view;
    }

    private void setShareToFB(String symbol, View view) {
        final ShareDialog shareDialog = new ShareDialog(this);
        ImageButton shareToFb = (ImageButton) view.findViewById(R.id.shareToFb);
        shareToFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://export.highcharts.com/" + postChart))
                        .build();
                shareDialog.show(content);
            }
        });
    }


    private void loadIndicatorChart(final String symbol, final View view) {
        final String indicator = ((Spinner) view.findViewById(R.id.indicator)).getSelectedItem().toString().trim();
        final WebView chart = (WebView) view.findViewById(R.id.indicator_charts);
        chart.getSettings().setJavaScriptEnabled(true);
        chart.addJavascriptInterface(new JsInterface(), "js");
        chart.getSettings().setLoadsImagesAutomatically(true);
        chart.getSettings().setAppCacheEnabled(true);
        chart.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView v, String url) {
                chart.loadUrl("javascript:drawChart('" + symbol + "', '" + indicator + "');");
                view.findViewById(R.id.changeBtn).setEnabled(false);
            }
        });
        chart.loadUrl("file:///android_asset/indicatorsChart.html");



    }

    private void setFavoriteBtn(final String symbol, View view) {
        final ImageButton favoriteBtn = (ImageButton) view.findViewById(R.id.favoriteBtn);
        final SharedPreferences sp = getContext().getSharedPreferences(MainActivity.FAVORITE_LIST, MainActivity.MODE_PRIVATE);
        if (sp.contains(symbol)) {
            favoriteBtn.setImageResource(R.drawable.filled);
        } else {
            favoriteBtn.setImageResource(R.drawable.empty);
        }
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sp.edit();
                if (!sp.contains(symbol)) {
                    editor.putString(symbol, "favorite");
                    editor.apply();
                    favoriteBtn.setImageResource(R.drawable.filled);
                    addToFavorite(symbol);

                } else {
                    editor.remove(symbol);
                    editor.apply();
                    favoriteBtn.setImageResource(R.drawable.empty);
                    removeFromFavorite(symbol);
                }
            }
        });
    }

    private void removeFromFavorite(String symbol) {
        for (int i = 0; i < MainActivity.favoriteItemList.size(); i++) {
            if (MainActivity.favoriteItemList.get(i).getSymbol().equals(symbol)) {
                MainActivity.favoriteItemList.remove(i);
                break;
            }
        }
        MainActivity.listView.setAdapter(MainActivity.favoriteAdapter);
        MainActivity.favoriteAdapter.notifyDataSetChanged();
    }

    private void addToFavorite(String symbol) {
        MainActivity.favoriteItemList.add(new FavoriteItem(symbol, data.get(1).getData(), data.get(2).getData()));
        MainActivity.listView.setAdapter(MainActivity.favoriteAdapter);
        MainActivity.favoriteAdapter.notifyDataSetChanged();
    }

    private void getData(final String symbol, final View view) {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String quoteUrl = SERVER + "/?quote=" + symbol;
        JsonObjectRequest quoteRequest = new JsonObjectRequest
                (Request.Method.GET, quoteUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            renderStockTable(response, symbol, view);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        view.findViewById(R.id.stock_table).setVisibility(View.GONE);
                        view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                        view.findViewById(R.id.error_message).setVisibility(View.VISIBLE);

                    }
                });
        quoteRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(quoteRequest);
    }


    private void renderStockTable(JSONObject response, String symbol, View view) throws JSONException {
        if (response.has("Error Message")) {
            view.findViewById(R.id.stock_table).setVisibility(View.GONE);
            view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
            view.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
            return;
        }
        String timestamp = response.getJSONObject("Meta Data").get("3. Last Refreshed").toString();
        boolean closeToday = false;
        if (timestamp.indexOf(":") == -1 || timestamp.length() == 10) {
            timestamp += " 16:00:00 EST";
            closeToday = true;
        } else {
            timestamp += " EST";
        }
        JSONObject timeseriesData = response.getJSONObject("Time Series (Daily)");
        Iterator dates = timeseriesData.keys();
        JSONObject lastday = timeseriesData.getJSONObject(dates.next().toString());
        String lastPrice = lastday.get("4. close").toString();
        double lastPriced = Double.parseDouble(lastPrice);
        lastPrice = df2.format(Double.parseDouble(lastPrice));
        String open = lastday.get("1. open").toString();
        open = df2.format(Double.parseDouble(open));
        String volume = lastday.get("5. volume").toString();
        String low = lastday.get("3. low").toString();
        low = df2.format(Double.parseDouble(low));
        String high = lastday.get("2. high").toString();
        high = df2.format(Double.parseDouble(high));
        String lastClose = timeseriesData.getJSONObject(dates.next().toString()).get("4. close").toString();
        double lastClosed = Double.parseDouble(lastClose);
        lastClose = df2.format(Double.parseDouble(lastClose));
        String close = "";
        if (closeToday) {
            close = lastPrice;
        } else {
            close = lastClose;
        }
        String change = df2.format(Double.parseDouble(lastPrice) - Double.parseDouble(lastClose));
        double changePercent = (lastPriced - lastClosed) / lastClosed * 100;
        String cPercent = df2.format(changePercent) + "%";
        data.add(new TableItem("Stock Symbol", symbol.toUpperCase()));
        data.add(new TableItem("Last Price", lastPrice));
        data.add(new TableItem("Change", change + " (" + cPercent + ") "));
        data.add(new TableItem("Timestamp", timestamp));
        data.add(new TableItem("Open", open));
        data.add(new TableItem("Close", close));
        data.add(new TableItem("Day's Range", low + " - " + high));
        data.add(new TableItem("Volume", volume));


        ListView listView = (ListView) view.findViewById(R.id.stock_table);
        StockTableAdapter adapter = new StockTableAdapter(getContext(), data);
        listView.setAdapter(adapter);
        view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private class JsInterface {
        @JavascriptInterface
        public void getChartData(String chartData) {
            postChart = chartData;

        }

    }
}


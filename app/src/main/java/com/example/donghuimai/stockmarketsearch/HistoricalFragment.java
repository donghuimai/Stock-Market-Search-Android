package com.example.donghuimai.stockmarketsearch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by donghuimai on 11/17/17.
 */

public class HistoricalFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_historical, container, false);
        rootView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.error_message).setVisibility(View.GONE);
        final String symbol = getArguments().getString("symbol");

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String quoteUrl = "http://hw8server-env.us-east-1.elasticbeanstalk.com/?quote=" + symbol;
        JsonObjectRequest quoteRequest = new JsonObjectRequest
                (Request.Method.GET, quoteUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.has("Error Message")) {
                            rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                            rootView.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
                            return;
                        }
                        final WebView historical = (WebView) rootView.findViewById(R.id.historical);
                        historical.getSettings().setJavaScriptEnabled(true);
                        historical.getSettings().setLoadsImagesAutomatically(true);
                        historical.getSettings().setAppCacheEnabled(true);

                        historical.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                historical.loadUrl("javascript:drawChart('" + symbol + "')");
                                rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);

                            }
                        });
                        historical.loadUrl("file:///android_asset/historicalChart.html");


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                        rootView.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
                    }
                });
        quoteRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(quoteRequest);

        return rootView;
    }
}

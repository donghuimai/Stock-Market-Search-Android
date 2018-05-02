package com.example.donghuimai.stockmarketsearch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donghuimai.stockmarketsearch.model.FavoriteItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by donghuimai on 11/14/17.
 */

public class MainActivity extends AppCompatActivity {
    private static String[] stocks;
    private final static String SERVER = "http://hw8server-env.us-east-1.elasticbeanstalk.com";
    private final static DecimalFormat df2 = new DecimalFormat("0.00");
    public final static String FAVORITE_LIST = "FavoriteList";
    public static List<FavoriteItem> favoriteItemList;
    public static ListView listView ;
    public static FavoriteListAdapter favoriteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
        findViewById(R.id.refresh_progress_bar).setVisibility(View.GONE);
        loadFavoriteList();

        listView =  (ListView) findViewById(R.id.favorites);
        favoriteAdapter =  new FavoriteListAdapter(MainActivity.this, favoriteItemList);

        listView.setAdapter(favoriteAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("symbol", favoriteItemList.get(i).getSymbol().toUpperCase());
                startActivityForResult(intent, 100);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int index, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Remove from Favorites?");
                String[] items = new String[] {"No", "Yes"};
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            Toast.makeText(getApplicationContext(), "Selected No",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            SharedPreferences sp = getSharedPreferences(FAVORITE_LIST, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            Log.d("remove", favoriteItemList.get(index).getSymbol().toLowerCase());
                            editor.remove(favoriteItemList.get(index).getSymbol().toLowerCase());
                            editor.apply();
                            favoriteItemList.remove(index);
                            favoriteAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Selected Yes",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.x = (int) view.getX();
                params.y = (int) view.getY();
                dialog.getWindow().setAttributes(params);
                dialog.show();
                return true;
            }
        });


        final AutoCompleteTextView symbol_input = (AutoCompleteTextView)
                findViewById(R.id.stock_symbol);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);


        symbol_input.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = symbol_input.getText().toString();
                if (input.trim().length() == 0) {
                    return;
                }
                String url = SERVER + "/?search=" + input;
                JsonArrayRequest autocompleteRequest = new JsonArrayRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                            @Override
                            public void onResponse(JSONArray response) {

                                try {
                                    if (response.length() == 0) {
                                        return;
                                    }
                                    stocks = new String[Math.min(5, response.length())];
                                    for (int i = 0; i < Math.min(5, response.length()); i++) {
                                        StringBuilder stock = new StringBuilder();
                                        JSONObject jresponse = response.getJSONObject(i);
                                        stock.append(jresponse.get("Symbol") + " - " + jresponse.get("Name"));
                                        stock.append(" (" + jresponse.get("Exchange") + ")");
                                        stocks[i] = stock.toString();
                                    }
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                                            android.R.layout.simple_dropdown_item_1line, stocks);
                                    symbol_input.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("autocomplete error", error.toString());

                            }
                        });
                requestQueue.add(autocompleteRequest);

            }
        });

        TextView getQuoteBtn = (TextView) findViewById(R.id.getQuoteBtn);
        getQuoteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String symbol = symbol_input.getText().toString().trim();
                if (symbol.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please enter a stock name or symbol",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (symbol.indexOf("-") != -1) {
                        symbol = symbol.substring(0, symbol.indexOf('-') - 1);
                    }
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("symbol", symbol.toUpperCase());
                    startActivityForResult(intent, 100);
                }
            }
        });

        TextView clearBtn = (TextView) findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AutoCompleteTextView) findViewById(R.id.stock_symbol)).setText("");
            }
        });

        Switch autoRefresh = (Switch) findViewById(R.id.autoRefresh);
        autoRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            loadFavoriteList();
                        }
                    }, 5000, 5000);

                }
            }
        });

        ImageButton refreshBtn = (ImageButton) findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.refresh_progress_bar).setVisibility(View.VISIBLE);
                loadFavoriteList();
                findViewById(R.id.refresh_progress_bar).setVisibility(View.GONE);
            }
        });

        final Spinner sort = (Spinner) findViewById(R.id.sort);
        final Spinner order = (Spinner) findViewById(R.id.order);

        final List<FavoriteItem> defaultList = new ArrayList<>(favoriteItemList);
        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sortBy = sort.getSelectedItem().toString();
                if (sortBy.equals("Price")) {
                    Collections.sort(favoriteItemList, new Comparator<FavoriteItem>() {
                        @Override
                        public int compare(FavoriteItem t1, FavoriteItem t2) {
                            return (int) (Double.parseDouble(t1.getPrice()) - Double.parseDouble(t2.getPrice()));
                        }
                    });

                } else if (sortBy.equals("Symbol")) {
                    Collections.sort(favoriteItemList, new Comparator<FavoriteItem>() {
                        @Override
                        public int compare(FavoriteItem t1, FavoriteItem t2) {
                            return t1.getSymbol().compareTo(t2.getSymbol());
                        }
                    });

                } else if (sortBy.equals("Change")) {
                    Collections.sort(favoriteItemList, new Comparator<FavoriteItem>() {
                        @Override
                        public int compare(FavoriteItem t1, FavoriteItem t2) {
                            String c1 = t1.getChange();
                            c1 = c1.substring(0, c1.indexOf("("));
                            String c2 = t1.getChange();
                            c2 = c2.substring(0, c2.indexOf("("));
                            return (int) (Double.parseDouble(c1) - Double.parseDouble(c2));
                        }
                    });

                } else if (sortBy.equals("Default")) {
                    favoriteItemList = new ArrayList<>(defaultList);
                }
                if (order.getSelectedItem().toString().equals("Descending")) {
                    Collections.reverse(favoriteItemList);
                }
                favoriteAdapter.notifyDataSetChanged();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        final String prev_order = order.getSelectedItem().toString();
        order.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String curr_order = order.getSelectedItem().toString();
                if (!curr_order.equals("Order") && !curr_order.equals(prev_order)) {
                    Collections.reverse(favoriteItemList);
                    favoriteAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadFavoriteList() {
        favoriteItemList =  new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        SharedPreferences sp = getSharedPreferences(FAVORITE_LIST, MODE_PRIVATE);
        Map<String, ?> favoriteList = sp.getAll();
        for (Map.Entry<String, ?> entry : favoriteList.entrySet()) {
            final String symbol = entry.getKey();
            String quoteUrl = SERVER + "/?quote=" + symbol;
            JsonObjectRequest favoriteRequest = new JsonObjectRequest
                    (Request.Method.GET, quoteUrl, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (!response.has("Time Series (Daily)")) {
                                    Log.d("favorite error", "no data");
                                    return;
                                }
                                JSONObject timeseriesData = response.getJSONObject("Time Series (Daily)");
                                Iterator dates = timeseriesData.keys();
                                JSONObject lastday = timeseriesData.getJSONObject(dates.next().toString());
                                String lastPrice = lastday.get("4. close").toString();
                                double lastPriced = Double.parseDouble(lastPrice);
                                lastPrice = df2.format(Double.parseDouble(lastPrice));
                                String lastClose = timeseriesData.getJSONObject(dates.next().toString()).get("4. close").toString();
                                double lastClosed = Double.parseDouble(lastClose);
                                lastClose = df2.format(Double.parseDouble(lastClose));
                                String change = df2.format(Double.parseDouble(lastPrice) - Double.parseDouble(lastClose));
                                double changePercent = (lastPriced - lastClosed) / lastClosed * 100;
                                String cPercent = df2.format(changePercent) + "%";
                                favoriteItemList.add(new FavoriteItem(symbol.toUpperCase(), lastPrice, change + " (" + cPercent + ")"));
                                favoriteAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("favorite error", error.toString());
                        }
                    });
            favoriteRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                    2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(favoriteRequest);
        }
    }
}










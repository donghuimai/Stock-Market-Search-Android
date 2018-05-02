package com.example.donghuimai.stockmarketsearch.model;

/**
 * Created by donghuimai on 11/17/17.
 */

public class FavoriteItem {
    private String symbol;
    private String price;
    private String change;

    public FavoriteItem(String symbol, String price, String change) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPrice() {
        return price;
    }

    public String getChange() { return change; }
}

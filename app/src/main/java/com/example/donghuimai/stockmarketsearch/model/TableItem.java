package com.example.donghuimai.stockmarketsearch.model;

/**
 * Created by donghuimai on 11/17/17.
 */

public class TableItem {
    private String header;
    private String data;

    public TableItem(String header, String data) {
        this.header = header;
        this.data = data;
    }

    public String getHeader() {
        return header;
    }

    public String getData() {
        return data;
    }
}

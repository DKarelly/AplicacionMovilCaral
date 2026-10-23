package com.example.py_damb2026ii.model;

import com.example.py_damb2026ii.network.DiscEntry;

import java.util.List;

public class RptaObtenerDiscos {
    private int code;
    private List<DiscEntry> data;
    private String msge;

    public int getCode() {
        return code;
    }

    public List<DiscEntry> getData() {
        return data;
    }

    public String getMsge() {
        return msge;
    }
}

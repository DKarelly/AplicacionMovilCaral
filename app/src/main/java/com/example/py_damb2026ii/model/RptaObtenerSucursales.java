package com.example.py_damb2026ii.model;

import com.example.py_damb2026ii.network.SucursalEntry;

import java.util.List;

public class RptaObtenerSucursales {
    private int code;
    private List<SucursalEntry> data;
    private String msge;

    public int getCode() {
        return code;
    }

    public List<SucursalEntry> getData() {
        return data;
    }

    public String getMsge() {
        return msge;
    }
}

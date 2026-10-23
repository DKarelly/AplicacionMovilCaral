package com.example.py_damb2026ii.model;

import com.example.py_damb2026ii.network.IncidentEntry;

import java.util.List;

public class RptaObtenerIncidentes {
    private int code;
    private List<IncidentEntry> data;
    private String msge;

    public int getCode() {
        return code;
    }

    public List<IncidentEntry> getData() {
        return data;
    }

    public String getMsge() {
        return msge;
    }
}

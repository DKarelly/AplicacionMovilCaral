
package com.example.py_damb2026ii.model;

import com.example.py_damb2026ii.network.UbicacionTecnicoEntry;
import java.util.List;

public class RptaObtenerUbicacionesTecnicos {

    private int code;
    private List<UbicacionTecnicoEntry> data;
    private String msge;

    public int getCode() {
        return code;
    }

    public List<UbicacionTecnicoEntry> getData() {
        return data;
    }

    public String getMsge() {
        return msge;
    }
}

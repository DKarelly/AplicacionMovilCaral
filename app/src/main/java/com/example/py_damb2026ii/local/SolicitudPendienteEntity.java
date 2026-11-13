package com.example.py_damb2026ii.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "solicitudes_pendientes")
public class SolicitudPendienteEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String titulo;
    public String descripcion;
    public String categoria;
    public String prioridad;
    public int id_sucursal;
    public Integer id_activo;
    public String codigo_qr_activo;
    public String ruta_foto1;
    public String ruta_foto2;
    public String id_usuario;
    public String fecha_creacion;
    public String estado;
}

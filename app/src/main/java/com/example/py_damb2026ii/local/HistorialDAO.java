package com.example.py_damb2026ii.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistorialDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HistorialEntity> historial);

    @Query("SELECT * FROM historial_cache WHERE id_incidente = :idIncidente ORDER BY id ASC")
    List<HistorialEntity> getHistorialPorIncidente(int idIncidente);

    @Query("DELETE FROM historial_cache WHERE id_usuario_cache = :idUsuarioCache")
    void deleteByUsuario(String idUsuarioCache);

    @Query("DELETE FROM historial_cache")
    void deleteAll();
}

package com.example.py_damb2026ii.Interface;

import com.example.py_damb2026ii.model.RequAuth;
import com.example.py_damb2026ii.model.RequInsertarDisco;
import com.example.py_damb2026ii.model.RequInsertarIncidente;
import com.example.py_damb2026ii.model.RptaAuth;
import com.example.py_damb2026ii.model.RptaInsertarDisco;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.example.py_damb2026ii.model.RptaObtenerDiscos;
import com.example.py_damb2026ii.model.RptaObtenerIncidentes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface JCPyAnyApi {

    @POST("auth")
    Call<RptaAuth> auth(@Body RequAuth requAuth);

    @GET("api_obtener_discos")
    Call<RptaObtenerDiscos> obtenerDiscos(@Header("Authorization") String authorization);

    @POST("api_insertar_disco")
    Call<RptaInsertarDisco> insertarDisco(@Header("Authorization") String authorization,
                                              @Body RequInsertarDisco requInsertarDisco);

    @GET("api_obtenerIncidente")
    Call<RptaObtenerIncidentes> obtenerIncidentes(@Header("Authorization") String authorization);

    @POST("/api_insertarIncidente")
    Call<RptaInsertarIncidente> insertarIncidente(@Header("Authorization") String authorization,
                                                  @Body RequInsertarIncidente requInsertarIncidente);

}

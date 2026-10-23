package com.example.py_damb2026ii.Interface;

import com.example.py_damb2026ii.model.RequAuth;
import com.example.py_damb2026ii.model.RequInsertarDisco;
import com.example.py_damb2026ii.model.RequInsertarIncidente;
import com.example.py_damb2026ii.model.RequInsertarSucursal;
import com.example.py_damb2026ii.model.RptaAuth;
import com.example.py_damb2026ii.model.RptaInsertarDisco;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.example.py_damb2026ii.model.RptaInsertarSucursal;
import com.example.py_damb2026ii.model.RptaObtenerDiscos;
import com.example.py_damb2026ii.model.RptaObtenerIncidentes;
import com.example.py_damb2026ii.model.RptaObtenerSucursales;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

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

    @GET("api_buscarSucursalNombre/{p_nombre}")
    Call<RptaObtenerSucursales> buscarSucursalNombre(@Header("Authorization") String authorization,
                                                    @Path("p_nombre") String nombre);

    @GET("api_obtenerSucursal")
    Call<RptaObtenerSucursales> obtenerSucursales(@Header("Authorization") String authorization);

    @POST("api_insertarSucursal")
    Call<RptaInsertarSucursal> insertarSucursal(@Header("Authorization") String authorization,
                                                @Body RequInsertarSucursal requInsertarSucursal);

}

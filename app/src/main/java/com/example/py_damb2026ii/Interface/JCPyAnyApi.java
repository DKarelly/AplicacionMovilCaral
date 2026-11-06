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
import com.example.py_damb2026ii.model.RptaDetalleIncidente;
import com.example.py_damb2026ii.model.RptaObtenerUsuarios;
import com.example.py_damb2026ii.model.RequInsertarUsuario;
import com.example.py_damb2026ii.model.RptaInsertarUsuario;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


import java.util.List;
import java.util.Map;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import com.example.py_damb2026ii.model.RptaObtenerUbicacionesTecnicos;

public interface JCPyAnyApi {

    @POST("auth")
    Call<RptaAuth> auth(@Body RequAuth requAuth);

    @GET("api_obtener_discos")
    Call<RptaObtenerDiscos> obtenerDiscos(@Header("Authorization") String authorization);

    @POST("api_insertar_disco")
    Call<RptaInsertarDisco> insertarDisco(@Header("Authorization") String authorization,
                                              @Body RequInsertarDisco requInsertarDisco);

    // Incidentes (según Flask API)
    @GET("api_obtenerIncidentes")
    Call<RptaObtenerIncidentes> obtenerIncidentes(@Header("Authorization") String authorization);


    @GET("api_obtenerIncidente/{p_id}")
    Call<RptaDetalleIncidente> obtenerDetalleIncidente(
            @Header("Authorization") String authorization,
            @Path("p_id") int idIncidente
    );

    // Buscar usuarios por rol
    @GET("api_buscarUsuarioRol/{p_rol}")
    Call<RptaObtenerUsuarios> buscarUsuariosPorRol(
            @Header("Authorization") String authorization,
            @Path("p_rol") String rol
    );

    @GET("api_obtenerTecnicosIncidente/{p_id}")
    Call<RptaObtenerUsuarios> obtenerTecnicosPorIncidente(
            @Header("Authorization") String authorization,
            @Path("p_id") int idIncidente
    );

    @Multipart
    @POST("api_registrarIncidente")
    Call<RptaInsertarIncidente> insertarIncidente(
            @Header("Authorization") String authorization,
            @PartMap Map<String, RequestBody> campos,
            @Part List<MultipartBody.Part> fotos
    );


    @PUT("api_asignarTecnico/{p_id}")
    Call<RptaInsertarIncidente> asignarTecnico(@Header("Authorization") String authorization,
                                               @Path("p_id") int idIncidente,
                                               @Body java.util.Map<String, Integer> body);

    @PUT("api_cambiarEstadoIncidente/{p_id}")
    Call<RptaInsertarIncidente> cambiarEstadoIncidente(@Header("Authorization") String authorization,
                                                       @Path("p_id") int idIncidente,
                                                       @Body java.util.Map<String, String> body);

    // UBICACIÓN DEL TÉCNICO

    @POST("api_actualizarUbicacionTecnico")
    Call<RptaInsertarIncidente> actualizarUbicacionTecnico(
            @Header("Authorization") String authorization,
            @Body Map<String, Object> body
    );

    @GET("api_ubicacionesTecnicos")
    Call<RptaObtenerUbicacionesTecnicos> obtenerUbicacionesTecnicos(
            @Header("Authorization") String authorization
    );

    // Sucursales (según Flask API)
    @GET("api_buscarSucursalNombre/{p_nombre}")
    Call<RptaObtenerSucursales> buscarSucursalNombre(@Header("Authorization") String authorization,
                                                    @Path("p_nombre") String nombre);

    @GET("api_listarSucursales")
    Call<RptaObtenerSucursales> obtenerSucursales(@Header("Authorization") String authorization);

    @POST("api_insertarSucursal")
    Call<RptaInsertarSucursal> insertarSucursal(@Header("Authorization") String authorization,
                                                @Body RequInsertarSucursal requInsertarSucursal);

    @DELETE("api_eliminarSucursal/{p_id}")
    Call<RptaInsertarSucursal> eliminarSucursal(@Header("Authorization") String authorization,
                                                @Path("p_id") int idSucursal);

    // GESTIÓN DE COLABORADORES

    @GET("api_listarUsuarios")
    Call<RptaObtenerUsuarios> obtenerUsuarios(
            @Header("Authorization") String authorization
    );

    @POST("api_insertarUsuario")
    Call<RptaInsertarUsuario> insertarUsuario(
            @Header("Authorization") String authorization,
            @Body RequInsertarUsuario usuario
    );

}

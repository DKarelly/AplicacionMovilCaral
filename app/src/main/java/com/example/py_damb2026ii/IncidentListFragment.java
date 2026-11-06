package com.example.py_damb2026ii;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentIncidentListBinding;
import com.example.py_damb2026ii.model.RptaObtenerIncidentes;
import com.example.py_damb2026ii.network.IncidentEntry;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IncidentListFragment extends Fragment {
    private FragmentIncidentListBinding binding;
    private SharedPreferences prefs;
    private IncidentCardRecyclerViewAdapter adapter;
    private final List<IncidentEntry> datos = new ArrayList<>();
    private String filtro="Todos";
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding=FragmentIncidentListBinding.inflate(inflater,container,false);
        prefs=requireActivity().getSharedPreferences("USAT_SP",Context.MODE_PRIVATE);
        String rol=prefs.getString("USER_ROLE","Solicitante");
        binding.txtRoleMini.setText(rol);
        binding.txtListTitle.setText("Administrador".equalsIgnoreCase(rol) ? "Todos los incidentes" :
                "Técnico".equalsIgnoreCase(rol) ? "Asignados a mí" : "Mis incidentes");
        binding.txtAvatarCircle.setText(iniciales(prefs.getString("USER_NAME",""),rol));
        binding.txtAvatarCircle.setOnClickListener(v -> {
            // Perfil y cierre de sesión están en el menú inferior persistente.
        });
        adapter=new IncidentCardRecyclerViewAdapter(new ArrayList<>());
        binding.recyclerViewIncidents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewIncidents.setAdapter(adapter);
        binding.btnFilterTodos.setOnClickListener(v -> seleccionar("Todos"));
        binding.btnFilterAbierto.setOnClickListener(v -> seleccionar("Abierto"));
        binding.btnFilterProceso.setOnClickListener(v -> seleccionar("En proceso"));
        binding.btnFilterResuelto.setOnClickListener(v -> seleccionar("Resuelto"));
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){filtrar();}
            public void afterTextChanged(Editable e){}
        });
        seleccionar("Todos");
        cargar();
        return binding.getRoot();
    }
    @Override public void onResume(){super.onResume();if(binding!=null && adapter!=null)cargar();}
    private String iniciales(String nombre,String rol){
        if(nombre==null || nombre.trim().isEmpty())return "Administrador".equalsIgnoreCase(rol)?"AD":"Técnico".equalsIgnoreCase(rol)?"TI":"SO";
        String limpio=nombre.split("@")[0].trim();
        String[] palabras=limpio.split("[ ._\\-]+");
        if(palabras.length>=2 && !palabras[1].isEmpty()) return (""+Character.toUpperCase(palabras[0].charAt(0))+Character.toUpperCase(palabras[1].charAt(0)));
        return palabras[0].length()>=2?palabras[0].substring(0,2).toUpperCase(Locale.ROOT):palabras[0].toUpperCase(Locale.ROOT);
    }
    private void seleccionar(String estado){
        filtro=estado;
        estilizar(binding.btnFilterTodos,"Todos".equals(estado));
        estilizar(binding.btnFilterAbierto,"Abierto".equals(estado));
        estilizar(binding.btnFilterProceso,"En proceso".equals(estado));
        estilizar(binding.btnFilterResuelto,"Resuelto".equals(estado));
        filtrar();
    }
    private void estilizar(MaterialButton b,boolean elegido){
        b.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),elegido?R.color.softBlue:R.color.white));
        b.setTextColor(ContextCompat.getColor(requireContext(),elegido?R.color.colorPrimary:R.color.textSecondary));
        b.setStrokeColor(ContextCompat.getColorStateList(requireContext(),elegido?R.color.colorPrimary:R.color.softBorder));
        b.setStrokeWidth(elegido?2:1);
    }

    private String normalizarEstado(String estado) {
        if (estado == null) return "";

        return estado.trim()
                .replace("_", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private void filtrar() {

        if (binding == null || adapter == null) return;

        String q = binding.inputSearch.getText() == null
                ? ""
                : binding.inputSearch.getText().toString()
                .toLowerCase(Locale.ROOT).trim();

        List<IncidentEntry> salida = new ArrayList<>();

        for (IncidentEntry x : datos) {

            String estadoActual = normalizarEstado(x.estado);
            String estadoFiltro = normalizarEstado(filtro);

            boolean coincideEstado;

            if ("todos".equals(estadoFiltro)) {
                coincideEstado = true;

            } else if ("resuelto".equals(estadoFiltro)) {
                coincideEstado = estadoActual.equals("resuelto")
                        || estadoActual.equals("cerrado");

            } else {
                coincideEstado = estadoActual.equals(estadoFiltro);
            }

            String titulo = x.titulo == null
                    ? ""
                    : x.titulo.toLowerCase(Locale.ROOT);

            String descripcion = x.descripcion == null
                    ? ""
                    : x.descripcion.toLowerCase(Locale.ROOT);

            String codigo = String.format(
                    Locale.ROOT, "INC-%03d", x.id
            ).toLowerCase(Locale.ROOT);

            String id = String.valueOf(x.id);

            boolean coincideTexto = q.isEmpty()
                    || titulo.contains(q)
                    || descripcion.contains(q)
                    || codigo.contains(q)
                    || id.contains(q);

            if (coincideEstado && coincideTexto) {
                salida.add(x);
            }
        }

        adapter.updateData(salida);

        binding.listMessage.setVisibility(
                salida.isEmpty() ? View.VISIBLE : View.GONE
        );

        binding.listMessage.setText(
                datos.isEmpty()
                        ? "No hay incidentes para mostrar"
                        : "No hay coincidencias con los filtros"
        );
    }

    private void cargar(){
        if(binding==null)return;
        binding.listMessage.setVisibility(View.VISIBLE);
        binding.listMessage.setText("Cargando incidentes...");
        String token=prefs.getString("TOKEN_JWT","");
        JCPyAnyApi api=new Retrofit.Builder().baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create()).build().create(JCPyAnyApi.class);
        api.obtenerIncidentes("JWT "+token).enqueue(new Callback<RptaObtenerIncidentes>(){
            @Override public void onResponse(@NonNull Call<RptaObtenerIncidentes> c,@NonNull Response<RptaObtenerIncidentes> r){
                if(binding==null||!isAdded())return;
                RptaObtenerIncidentes body=r.body();
                if(!r.isSuccessful()||body==null||body.getCode()!=1||body.getData()==null){
                    binding.listMessage.setVisibility(View.VISIBLE);
                    binding.listMessage.setText("No se pudo consultar los incidentes. HTTP "+r.code());
                    return;
                }
                datos.clear();datos.addAll(body.getData());filtrar();
            }
            @Override public void onFailure(@NonNull Call<RptaObtenerIncidentes> c,@NonNull Throwable t){
                if(binding==null||!isAdded())return;
                binding.listMessage.setVisibility(View.VISIBLE);
                binding.listMessage.setText("Sin conexión con el servidor. Revisa Internet y vuelve a abrir esta pantalla.");
            }
        });
    }
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}

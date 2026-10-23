package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentHomeBinding;
import com.example.py_damb2026ii.model.RptaObtenerDiscos;
import com.example.py_damb2026ii.network.DiscEntry;
import com.example.py_damb2026ii.network.ProductEntry;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentHomeBinding binding;

    private SharedPreferences sharedPref;
    private List<DiscEntry> listaDiscos;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_home, container, false);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        /*
        binding.btnCargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerDatosApi();
            }
        });
         */

        obtenerDatosApi(() ->{
            RecyclerView recyclerView = binding.recyclerView;
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
            /*ProductCardRecyclerViewAdapter adapter = new ProductCardRecyclerViewAdapter(
                    ProductEntry.initProductEntryList(getResources()));*/
            DiscCardRecyclerViewAdapter adapter = new DiscCardRecyclerViewAdapter(listaDiscos);
            recyclerView.setAdapter(adapter);
            int largePadding = getResources().getDimensionPixelSize(R.dimen.usat_product_grid_spacing);
            int smallPadding = getResources().getDimensionPixelSize(R.dimen.usat_product_grid_spacing_small);
            recyclerView.addItemDecoration(new ProductGridItemDecoration(largePadding, smallPadding));
        });



        return binding.getRoot();
    }

    private void obtenerDatosApi(Runnable listaCargada){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JCPyAnyApi jcPyAnyApi = retrofit.create(JCPyAnyApi.class);
        String token = sharedPref.getString("TOKEN_JWT","");
        Call<RptaObtenerDiscos> call = jcPyAnyApi.obtenerDiscos("JWT " + token);
        call.enqueue(new Callback<RptaObtenerDiscos>() {
            @Override
            public void onResponse(Call<RptaObtenerDiscos> call, Response<RptaObtenerDiscos> response) {
                if(!response.isSuccessful()){
                    Log.d("USAT_APP", "Código: " + response.code());
                }else{
                    /*
                    RptaObtenerDiscos rptaObtenerDiscos = response.body();
                    String contenido = "";
                    contenido += "Code: " + String.valueOf(rptaObtenerDiscos.getCode()) + "\n";
                    contenido += "Data: " + rptaObtenerDiscos.getData() + "\n";
                    contenido += "Msge: " + rptaObtenerDiscos.getMsge() + "\n";
                    binding.txtDatos.append(contenido);
                     */
                    RptaObtenerDiscos rptaObtenerDiscos = response.body();
                    HomeFragment.this.listaDiscos = rptaObtenerDiscos.getData();
                    if (listaCargada != null) {
                        listaCargada.run();
                    }
                }
            }

            @Override
            public void onFailure(Call<RptaObtenerDiscos> call, Throwable throwable) {
                Log.d("USAT_APP", throwable.getMessage());
            }
        });
    }
}
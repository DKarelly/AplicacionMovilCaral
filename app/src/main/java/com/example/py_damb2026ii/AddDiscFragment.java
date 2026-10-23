package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentAddDiscBinding;
import com.example.py_damb2026ii.model.RequInsertarDisco;
import com.example.py_damb2026ii.model.RptaInsertarDisco;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddDiscFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDiscFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentAddDiscBinding binding;
    private SharedPreferences sharedPref;

    public AddDiscFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDiscFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddDiscFragment newInstance(String param1, String param2) {
        AddDiscFragment fragment = new AddDiscFragment();
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
        binding = FragmentAddDiscBinding.inflate(inflater, container, false);

        binding.addDiscButton.setOnClickListener(v -> insertarDisco(() ->{
            Snackbar.make(binding.getRoot(), "Disco insertado correctamente", Snackbar.LENGTH_LONG)
                    .setAnchorView(requireActivity().findViewById(R.id.fab))
                    .setAction("Action", null).show();
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.HomeFragment);
        }));

        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_add_disc, container, false);
        return binding.getRoot();
    }

    private void insertarDisco(Runnable transaccionOK){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JCPyAnyApi jcPyAnyApi = retrofit.create(JCPyAnyApi.class);
        String token = sharedPref.getString("TOKEN_JWT","");
        RequInsertarDisco requInsertarDisco = new RequInsertarDisco();
        requInsertarDisco.setTitulo(binding.tituloEditText.getText().toString());
        requInsertarDisco.setAutor(binding.autorEditText.getText().toString());
        requInsertarDisco.setPrecio(binding.precioEditText.getText().toString());
        requInsertarDisco.setUrl(binding.urlEditText.getText().toString());
        Call<RptaInsertarDisco> call = jcPyAnyApi.
                insertarDisco("JWT " + token, requInsertarDisco);
        call.enqueue(new Callback<RptaInsertarDisco>() {
            @Override
            public void onResponse(Call<RptaInsertarDisco> call, Response<RptaInsertarDisco> response) {
                if(!response.isSuccessful()){
                    Log.d("USAT_APP", "Código: " + response.code());
                }else{
                    RptaInsertarDisco rptaInsertarDisco = response.body();
                    if (rptaInsertarDisco.getCode()==1){
                        if (transaccionOK != null) {
                            transaccionOK.run();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<RptaInsertarDisco> call, Throwable throwable) {

            }
        });
    }
}
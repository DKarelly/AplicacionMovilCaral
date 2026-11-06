package com.example.py_damb2026ii;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.py_damb2026ii.model.RptaObtenerUsuarios;

import java.util.ArrayList;
import java.util.List;

public class CollaboratorCardRecyclerViewAdapter
        extends RecyclerView.Adapter<CollaboratorCardRecyclerViewAdapter.Holder> {

    private final List<RptaObtenerUsuarios.Usuario> usuarios =
            new ArrayList<>();

    public CollaboratorCardRecyclerViewAdapter(
            List<RptaObtenerUsuarios.Usuario> lista) {

        if (lista != null) {
            usuarios.addAll(lista);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.usat_collaborator_card, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull Holder holder,
            int position) {

        RptaObtenerUsuarios.Usuario usuario = usuarios.get(position);

        holder.nombre.setText(usuario.getNombre());
        holder.correo.setText(usuario.getCorreo());
        holder.rol.setText(usuario.getRol());

        String sucursal = usuario.getNombreSucursal();

        holder.sucursal.setText(
                "Sucursal: " +
                        (sucursal == null || sucursal.trim().isEmpty()
                                ? "Sin asignar"
                                : sucursal)
        );

        boolean activo = usuario.isActivo();

        holder.estado.setText(activo ? "Activo" : "Inactivo");

        holder.estado.setTextColor(
                Color.parseColor(activo ? "#39866C" : "#C96F68")
        );
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView nombre;
        TextView correo;
        TextView rol;
        TextView sucursal;
        TextView estado;

        Holder(@NonNull View itemView) {
            super(itemView);

            nombre = itemView.findViewById(R.id.collaborator_name);
            correo = itemView.findViewById(R.id.collaborator_email);
            rol = itemView.findViewById(R.id.collaborator_role);
            sucursal = itemView.findViewById(R.id.collaborator_branch);
            estado = itemView.findViewById(R.id.collaborator_status);
        }
    }
}
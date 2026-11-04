package com.example.py_damb2026ii;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.py_damb2026ii.network.IncidentEntry;
import com.example.py_damb2026ii.util.SlaHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IncidentCardRecyclerViewAdapter extends RecyclerView.Adapter<IncidentCardViewHolder> {

    private final List<IncidentEntry> listaCompleta = new ArrayList<>();
    private final List<IncidentEntry> listaFiltrada = new ArrayList<>();

    public IncidentCardRecyclerViewAdapter(List<IncidentEntry> incidentList) {
        if (incidentList != null) {
            listaCompleta.addAll(incidentList);
            listaFiltrada.addAll(incidentList);
        }
    }

    /* Filtra por estado y prioridad. null = "Todos". */
    public void filtrar(String estado, String prioridad) {
        listaFiltrada.clear();
        for (IncidentEntry inc : listaCompleta) {
            boolean coincideEstado = (estado == null) || estado.equalsIgnoreCase(inc.estado);
            boolean coincidePrioridad = (prioridad == null) || prioridad.equalsIgnoreCase(inc.prioridad);
            if (coincideEstado && coincidePrioridad) {
                listaFiltrada.add(inc);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncidentCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.usat_incident_card, parent, false);
        return new IncidentCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentCardViewHolder holder, int position) {
        IncidentEntry incident = listaFiltrada.get(position);

        // Número de ticket #INC-001
        holder.incidentCode.setText(String.format(Locale.getDefault(), "#INC-%03d", incident.id));
        holder.incidentTitle.setText(incident.titulo);
        holder.incidentDescription.setText(incident.descripcion);
        holder.incidentCategory.setText("Categoría: " + incident.categoria);
        holder.incidentStatus.setText("Estado: " + incident.estado);

        // Prioridad con color: rojo = Alta, naranja = Media, verde = Baja
        holder.incidentPriority.setText("Prioridad: " + incident.prioridad);
        holder.incidentPriority.setTextColor(SlaHelper.colorPrioridad(incident.prioridad));

        // Técnico asignado
        if (incident.tecnico != null && !incident.tecnico.isEmpty()) {
            holder.incidentTecnico.setText("Técnico: " + incident.tecnico);
        } else {
            holder.incidentTecnico.setText("Técnico: sin asignar");
        }

        // Semáforo SLA
        long fechaRegistro = SlaHelper.parsearFecha(incident.fecha_registro);
        if (fechaRegistro > 0) {
            long ahora = System.currentTimeMillis();
            int sla = SlaHelper.calcularSla(incident.prioridad, incident.estado, fechaRegistro, ahora);
            int color = SlaHelper.colorSla(sla);

            holder.incidentDate.setText(SlaHelper.formatearFecha(fechaRegistro));
            holder.incidentSla.setText(SlaHelper.textoSla(sla, incident.prioridad, fechaRegistro, ahora));
            pintarEtiqueta(holder.incidentSla, color);
            holder.incidentSlaBar.setBackgroundColor(color);
        } else {
            holder.incidentDate.setText(incident.fecha_registro);
            holder.incidentSla.setText("SLA: sin fecha");
            pintarEtiqueta(holder.incidentSla, SlaHelper.COLOR_GRIS);
            holder.incidentSlaBar.setBackgroundColor(SlaHelper.COLOR_GRIS);
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("id_incidente", incident.id);
            Navigation.findNavController(v).navigate(R.id.IncidentDetailFragment, args);
        });
    }

    /* Pinta la etiqueta del SLA. */
    private void pintarEtiqueta(View view, int color) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setCornerRadius(40f);
        fondo.setColor(color);
        view.setBackground(fondo);
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }
}
package com.example.py_damb2026ii;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.py_damb2026ii.network.IncidentEntry;

import java.util.List;

public class IncidentCardRecyclerViewAdapter extends RecyclerView.Adapter<IncidentCardViewHolder> {
    private final List<IncidentEntry> incidentList;

    public IncidentCardRecyclerViewAdapter(List<IncidentEntry> incidentList) {
        this.incidentList = incidentList;
    }

    @NonNull
    @Override
    public IncidentCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.usat_incident_card, parent, false);
        return new IncidentCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentCardViewHolder holder, int position) {
        if (incidentList != null && position < incidentList.size()) {
            IncidentEntry incident = incidentList.get(position);
            holder.incidentTitle.setText(incident.titulo);
            holder.incidentDescription.setText(incident.descripcion);
            holder.incidentCategory.setText("Categoría: " + incident.categoria);
            holder.incidentPriority.setText("Prioridad: " + incident.prioridad);
            holder.incidentStatus.setText("Estado: " + incident.estado);
            holder.incidentDate.setText(incident.fecha_registro);
        }
    }

    @Override
    public int getItemCount() {
        return incidentList != null ? incidentList.size() : 0;
    }
}

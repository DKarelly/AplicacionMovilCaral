package com.example.py_damb2026ii;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IncidentCardViewHolder extends RecyclerView.ViewHolder {

    public TextView incidentCode;
    public TextView incidentPriorityChip;
    public TextView incidentTitle;
    public TextView incidentDate;
    public TextView incidentTecnico;
    public TextView incidentSolicitante;
    public TextView incidentStatusChip;

    public IncidentCardViewHolder(@NonNull View itemView) {
        super(itemView);

        incidentCode = itemView.findViewById(R.id.incident_code);
        incidentPriorityChip = itemView.findViewById(R.id.incident_priority_chip);
        incidentTitle = itemView.findViewById(R.id.incident_title);
        incidentDate = itemView.findViewById(R.id.incident_date);
        incidentTecnico = itemView.findViewById(R.id.incident_tecnico);
        incidentSolicitante = itemView.findViewById(R.id.incident_solicitante);
        incidentStatusChip = itemView.findViewById(R.id.incident_status_chip);
    }
}
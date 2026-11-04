package com.example.py_damb2026ii;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IncidentCardViewHolder extends RecyclerView.ViewHolder {
    public TextView incidentTitle;
    public TextView incidentDescription;
    public TextView incidentCategory;
    public TextView incidentPriority;
    public TextView incidentStatus;
    public TextView incidentDate;
    public TextView incidentCode;
    public TextView incidentSla;
    public TextView incidentTecnico;
    public View incidentSlaBar;

    public IncidentCardViewHolder(@NonNull View itemView) {
        super(itemView);
        incidentTitle = itemView.findViewById(R.id.incident_title);
        incidentDescription = itemView.findViewById(R.id.incident_description);
        incidentCategory = itemView.findViewById(R.id.incident_category);
        incidentPriority = itemView.findViewById(R.id.incident_priority);
        incidentStatus = itemView.findViewById(R.id.incident_status);
        incidentDate = itemView.findViewById(R.id.incident_date);
        incidentCode = itemView.findViewById(R.id.incident_code);
        incidentSla = itemView.findViewById(R.id.incident_sla);
        incidentTecnico = itemView.findViewById(R.id.incident_tecnico);
        incidentSlaBar = itemView.findViewById(R.id.incident_sla_bar);
    }
}
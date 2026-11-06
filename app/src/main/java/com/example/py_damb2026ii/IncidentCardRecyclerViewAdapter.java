package com.example.py_damb2026ii;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.py_damb2026ii.network.IncidentEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;

public class IncidentCardRecyclerViewAdapter extends RecyclerView.Adapter<IncidentCardViewHolder> {


    private final List<IncidentEntry> incidentList = new ArrayList<>();

    public IncidentCardRecyclerViewAdapter(List<IncidentEntry> incidentList) {
        updateData(incidentList);
    }

    public void updateData(List<IncidentEntry> nuevos) {
        this.incidentList.clear();

        if (nuevos != null) {
            this.incidentList.addAll(nuevos);
        }

        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public IncidentCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.usat_incident_card, parent, false);
        return new IncidentCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentCardViewHolder holder, int position) {
        if (incidentList == null || position >= incidentList.size()) return;

        IncidentEntry incident = incidentList.get(position);

        holder.incidentCode.setText(String.format(Locale.getDefault(), "INC-%03d", incident.id));
        holder.incidentTitle.setText(valorSeguro(incident.titulo));
        holder.incidentDate.setText(formatearFechaCliente(incident.fecha_registro));
        holder.incidentTecnico.setText(valorSeguro(incident.tecnico, "Sin asignar"));
        holder.incidentSolicitante.setText("Solicita: " + valorSeguro(incident.solicitante, "Sin solicitante"));
        holder.incidentPriorityChip.setText(valorSeguro(incident.prioridad, "Media"));
        holder.incidentStatusChip.setText(valorSeguro(incident.estado, "Abierto"));

        holder.incidentTitle.setTypeface(Typeface.DEFAULT_BOLD);

        aplicarChipPrioridad(holder.incidentPriorityChip, incident.prioridad);
        aplicarChipEstado(holder.incidentStatusChip, incident.estado);

        holder.itemView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("id_incidente", incident.id);

            Navigation.findNavController(v).navigate(
                    R.id.IncidentDetailFragment,
                    args
            );
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor : "";
    }

    private String valorSeguro(String valor, String defecto) {
        return (valor != null && !valor.trim().isEmpty()) ? valor : defecto;
    }

    private void aplicarChipPrioridad(android.widget.TextView textView, String prioridad) {
        int bg = Color.parseColor("#F6E5E1");
        int fg = Color.parseColor("#D07B6B");

        if ("Media".equalsIgnoreCase(prioridad)) {
            bg = Color.parseColor("#F7EFD9");
            fg = Color.parseColor("#C38A15");
        } else if ("Baja".equalsIgnoreCase(prioridad)) {
            bg = Color.parseColor("#E4F2E8");
            fg = Color.parseColor("#5C9B78");
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bg);
        drawable.setCornerRadius(100f);
        textView.setBackground(drawable);
        textView.setTextColor(fg);
    }

    private void aplicarChipEstado(android.widget.TextView textView, String estado) {
        int bg = Color.parseColor("#EAF4FB");
        int fg = Color.parseColor("#4D88B3");

        if ("Resuelto".equalsIgnoreCase(estado) || "Cerrado".equalsIgnoreCase(estado)) {
            bg = Color.parseColor("#E7F3EA");
            fg = Color.parseColor("#5C9B78");
        } else if ("En proceso".equalsIgnoreCase(estado)) {
            bg = Color.parseColor("#EAF4FB");
            fg = Color.parseColor("#4D88B3");
        } else if ("Abierto".equalsIgnoreCase(estado)) {
            bg = Color.parseColor("#EEF4F7");
            fg = Color.parseColor("#6C8698");
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bg);
        drawable.setCornerRadius(100f);
        textView.setBackground(drawable);
        textView.setTextColor(fg);
    }

    private String formatearFechaCliente(String fechaApi) {
        if (fechaApi == null || fechaApi.trim().isEmpty()) return "";

        try {
            SimpleDateFormat entrada =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            entrada.setTimeZone(TimeZone.getTimeZone("GMT"));

            Date fecha = entrada.parse(fechaApi);

            if (fecha == null) return fechaApi;

            SimpleDateFormat salida =
                    new SimpleDateFormat("dd MMM yyyy · h:mm a", new Locale("es", "PE"));

            String resultado = salida.format(fecha);
            resultado = resultado.replace("AM", "a. m.").replace("PM", "p. m.");

            return resultado;
        } catch (Exception e) {
            return fechaApi;
        }
    }
}
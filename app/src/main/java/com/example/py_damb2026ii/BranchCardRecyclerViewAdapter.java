package com.example.py_damb2026ii;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.py_damb2026ii.network.SucursalEntry;

import java.util.List;

public class BranchCardRecyclerViewAdapter extends RecyclerView.Adapter<BranchCardViewHolder> {
    private final List<SucursalEntry> branchList;

    public BranchCardRecyclerViewAdapter(List<SucursalEntry> branchList) {
        this.branchList = branchList;
    }

    @NonNull
    @Override
    public BranchCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.usat_branch_card, parent, false);
        return new BranchCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchCardViewHolder holder, int position) {
        if (branchList != null && position < branchList.size()) {
            SucursalEntry branch = branchList.get(position);
            holder.branchName.setText(branch.nombre);
            holder.branchAddress.setText(branch.direccion);
            holder.branchLatLon.setText("Lat: " + branch.latitud + ", Lon: " + branch.longitud);
            holder.branchId.setText("ID: " + branch.id);
        }
    }

    @Override
    public int getItemCount() {
        return branchList != null ? branchList.size() : 0;
    }
}

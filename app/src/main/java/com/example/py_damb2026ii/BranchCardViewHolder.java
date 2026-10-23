package com.example.py_damb2026ii;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BranchCardViewHolder extends RecyclerView.ViewHolder {
    public TextView branchName;
    public TextView branchAddress;
    public TextView branchLatLon;
    public TextView branchId;

    public BranchCardViewHolder(@NonNull View itemView) {
        super(itemView);
        branchName = itemView.findViewById(R.id.branch_name);
        branchAddress = itemView.findViewById(R.id.branch_address);
        branchLatLon = itemView.findViewById(R.id.branch_lat_lon);
        branchId = itemView.findViewById(R.id.branch_id);
    }
}

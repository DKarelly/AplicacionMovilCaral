package com.example.py_damb2026ii;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.NetworkImageView;

public class DiscCardViewHolder extends RecyclerView.ViewHolder{
    public NetworkImageView discImage;
    public TextView discTitle;
    public TextView discPrice;

    public DiscCardViewHolder(@NonNull View itemView) {
        super(itemView);
        discImage = itemView.findViewById(R.id.product_image);
        discTitle = itemView.findViewById(R.id.product_title);
        discPrice = itemView.findViewById(R.id.product_price);
    }
}

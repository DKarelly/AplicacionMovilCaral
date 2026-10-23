package com.example.py_damb2026ii;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.py_damb2026ii.network.DiscEntry;
import com.example.py_damb2026ii.network.ImageRequester;
import com.example.py_damb2026ii.network.ProductEntry;

import java.util.List;

public class DiscCardRecyclerViewAdapter extends RecyclerView.Adapter<DiscCardViewHolder>{
    private List<DiscEntry> discList;
    private ImageRequester imageRequester;

    DiscCardRecyclerViewAdapter(List<DiscEntry> discList) {
        this.discList = discList;
        imageRequester = ImageRequester.getInstance();
    }

    @NonNull
    @Override
    public DiscCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.usat_product_card, parent, false);
        return new DiscCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscCardViewHolder holder, int position) {
        if (discList != null && position < discList.size()) {
            DiscEntry disc = discList.get(position);
            holder.discTitle.setText(disc.titulo);
            holder.discPrice.setText(disc.precio);
            imageRequester.setImageFromUrl(holder.discImage, disc.url);
        }
    }

    @Override
    public int getItemCount() {
        return discList.size();
    }
}

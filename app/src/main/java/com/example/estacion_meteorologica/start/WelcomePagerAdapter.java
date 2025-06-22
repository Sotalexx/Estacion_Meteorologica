package com.example.estacion_meteorologica.start;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estacion_meteorologica.R;
import com.example.estacion_meteorologica.models.ItemSlide;

import java.util.List;

public class WelcomePagerAdapter extends RecyclerView.Adapter<WelcomePagerAdapter.SlideViewHolder> {
    private final List<ItemSlide> slideItems;

    public WelcomePagerAdapter(List<ItemSlide> items) {
        this.slideItems = items;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_welcome_page, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        ItemSlide item = slideItems.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return slideItems.size();
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
            description = itemView.findViewById(R.id.text_description);

        }
    }
}


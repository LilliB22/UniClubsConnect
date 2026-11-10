package com.example.uniclubconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<Integer> images;
    private final List<String> labels;

    public ImageAdapter(List<Integer> images, List<String> labels) {
        this.images = images;
        this.labels = labels;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView label;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageView);
            label = itemView.findViewById(R.id.imageLabel);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.image.setImageResource(images.get(position));
        holder.label.setText(labels.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
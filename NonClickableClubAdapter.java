package com.example.uniclubconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniclubconnect.model.Club;
import java.util.ArrayList;

public class NonClickableClubAdapter extends RecyclerView.Adapter<NonClickableClubAdapter.ClubViewHolder> {

    private final ArrayList<Club> clubList;

    public NonClickableClubAdapter(ArrayList<Club> clubList) {
        this.clubList = clubList;
    }

    @NonNull
    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_club, parent, false);
        return new ClubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {
        Club club = clubList.get(position);
        holder.txtClubName.setText(club.getName());
        holder.txtClubDescription.setText(club.getDescription());

        // Disable clicks
        holder.itemView.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return clubList.size();
    }

    static class ClubViewHolder extends RecyclerView.ViewHolder {
        TextView txtClubName, txtClubDescription;

        public ClubViewHolder(@NonNull View itemView) {
            super(itemView);
            txtClubName = itemView.findViewById(R.id.txtClubName);
            txtClubDescription = itemView.findViewById(R.id.txtClubDescription);
        }
    }
}

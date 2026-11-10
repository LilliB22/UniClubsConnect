package com.example.uniclubconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.uniclubconnect.databinding.FragmentClickedClubBinding;
import com.example.uniclubconnect.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ClickedClub extends Fragment {

    private FragmentClickedClubBinding binding;
    private DatabaseReference database;
    private ClickableClubAdapter clickableClubAdapter;
    private final ArrayList<Club> clubList = new ArrayList<>();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentClickedClubBinding.inflate(inflater, container, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        database = FirebaseDatabase.getInstance().getReference().child("Clubs");

        setupRecyclerView();
        loadClubs();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        clickableClubAdapter = new ClickableClubAdapter(clubList, clubId -> {
            // Open Menu and pass selected clubId
            Menu menuFragment = new Menu();
            Bundle bundle = new Bundle();
            bundle.putString("clubId", clubId);
            menuFragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, menuFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.recycleClubs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycleClubs.setAdapter(clickableClubAdapter);
    }

    private void loadClubs() {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clubList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Club club = data.getValue(Club.class);
                    if (club != null) clubList.add(club);
                }
                clickableClubAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Failed to load clubs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
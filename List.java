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

import com.example.uniclubconnect.databinding.FragmentListBinding;
import com.example.uniclubconnect.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class List extends Fragment {

    private FragmentListBinding binding;
    private DatabaseReference database;
    private NonClickableClubAdapter clubAdapter;
    private final ArrayList<Club> clubList = new ArrayList<>();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(inflater, container, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Initialize NON-clickable adapter
        clubAdapter = new NonClickableClubAdapter(clubList);
        binding.recyclerClubs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerClubs.setAdapter(clubAdapter);

        database = FirebaseDatabase.getInstance().getReference().child("Clubs");

        loadClubs();
        setupFabForAdvisors();

        return binding.getRoot();
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
                if (isAdded()) clubAdapter.notifyDataSetChanged();

                if (clubList.isEmpty()) {
                    Toast.makeText(requireContext(), "No clubs available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFabForAdvisors() {
        // Hide button by default
        binding.fabAddClub.setVisibility(View.GONE);

        if (currentUserId == null) return;

        // Check if user role is advisor
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("role");
        userRef.get().addOnSuccessListener(snapshot -> {
            String role = snapshot.getValue(String.class);
            if ("advisor".equals(role)) {
                binding.fabAddClub.setVisibility(View.VISIBLE);
                binding.fabAddClub.setOnClickListener(v -> {
                    // Open AddClub fragment
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, new AddClub())
                            .addToBackStack(null)
                            .commit();
                });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(),
                        "Failed to check user role: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
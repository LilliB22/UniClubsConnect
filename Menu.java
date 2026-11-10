package com.example.uniclubconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uniclubconnect.databinding.FragmentMenuBinding;

public class Menu extends Fragment {

    private FragmentMenuBinding binding;
    private String clubId; // selected clubId

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentMenuBinding.inflate(inflater, container, false);

        // Get clubId from arguments
        if (getArguments() != null) {
            clubId = getArguments().getString("clubId");
        }

        binding.Chat.setOnClickListener(v -> {
            ClubChat clubChatFragment = new ClubChat();
            Bundle bundle = new Bundle();
            bundle.putString("clubId", clubId);
            clubChatFragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, clubChatFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.Calendar.setOnClickListener(v -> {
            Calendar calendarFragment = new Calendar();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, calendarFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
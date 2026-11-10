package com.example.uniclubconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.uniclubconnect.databinding.FragmentAddToFeedBinding;

import java.util.Arrays;
import java.util.List;

public class AddToFeed extends Fragment {

    private FragmentAddToFeedBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAddToFeedBinding.inflate(inflater, container, false);

        List<Integer> images = Arrays.asList(
                R.drawable.img
        );

        List<String> labels = Arrays.asList(
                "Test"
        );

        ImageAdapter adapter = new ImageAdapter(images, labels);

        binding.recyclerImages.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerImages.setAdapter(adapter);

        return binding.getRoot();
    }
}
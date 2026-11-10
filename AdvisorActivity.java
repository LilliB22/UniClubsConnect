package com.example.uniclubconnect;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.uniclubconnect.databinding.ActivityAdvisorBinding;

public class AdvisorActivity extends AppCompatActivity {

    private ActivityAdvisorBinding binding;

    // Keep fragment instances
    private Fragment ListFragment;
    private Fragment clickedClubFragment;
    private Fragment addToFeedFragment;
    private Fragment infoFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdvisorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize fragments once
        ListFragment = new List();
        clickedClubFragment = new ClickedClub();
        addToFeedFragment = new AddToFeed();
        infoFragment = new Info();

        // Load default fragment
        replaceFragment(ListFragment);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_aList) {
                replaceFragment(ListFragment);
                return true;
            } else if (id == R.id.nav_aMenu) {
                replaceFragment(clickedClubFragment);
                return true;
            } else if (id == R.id.nav_aFeed) {
                replaceFragment(addToFeedFragment);
                return true;
            } else if (id == R.id.nav_aInfo) {
                replaceFragment(infoFragment);
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
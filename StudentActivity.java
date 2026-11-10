package com.example.uniclubconnect;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.uniclubconnect.databinding.ActivityStudentBinding;

public class StudentActivity extends AppCompatActivity {

    private ActivityStudentBinding binding;

    private Fragment ListFragment;
    private Fragment clickedClubFragment;
    private Fragment feedFragment;
    private Fragment infoFragment;
    private Fragment activeFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize fragments
        ListFragment = new List();
        clickedClubFragment = new ClickedClub();
        feedFragment = new AddToFeed();
        infoFragment = new Info();

        // Add all fragments to container, hide all except Slist
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, infoFragment, "INFO").hide(infoFragment)
                .add(R.id.container, feedFragment, "FEED").hide(feedFragment)
                .add(R.id.container, clickedClubFragment, "CLICKED").hide(clickedClubFragment)
                .add(R.id.container, ListFragment, "SlIST")
                .commit();

        activeFragment = ListFragment;

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment target = null;
            int id = item.getItemId();
            if (id == R.id.nav_sList) {
                target = ListFragment;
            } else if (id == R.id.nav_sMenu) {
                target = clickedClubFragment;
            } else if (id == R.id.nav_sFeed) {
                target = feedFragment;
            } else if (id == R.id.nav_sInfo) {
                target = infoFragment;
            }
            if (target != null && target != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(target)
                        .commit();
                activeFragment = target;
            }
            return true;
        });
    }
}
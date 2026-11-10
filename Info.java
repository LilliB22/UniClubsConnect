package com.example.uniclubconnect;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.uniclubconnect.databinding.FragmentInfoBinding;

public class Info extends Fragment {

    private FragmentInfoBinding binding;
    private String clubId;
    private String clubName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            clubId = args.getString("clubId");
            clubName = args.getString("clubName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentInfoBinding.inflate(inflater, container, false);

        setupButtons();

        return binding.getRoot();
    }

    private void setupButtons() {

        binding.AddtoFeed.setOnClickListener(v -> openNextFragment(new AddToFeed()));
        binding.EditClub.setOnClickListener(v -> openNextFragment(new EditClub()));
        binding.ApproveStudents.setOnClickListener(v -> openNextFragment(new PendingMember()));
        binding.AddMeeting.setOnClickListener(v -> openNextFragment(new AddMeeting()));
        binding.Treasury.setOnClickListener(v -> openNextFragment(new Treasury()));
        binding.AddDocuments.setOnClickListener(v -> openNextFragment(new AddDocument()));

    }

    private void openNextFragment(Fragment fragment) {
        Bundle args = new Bundle();
        args.putString("clubId", clubId);
        args.putString("clubName", clubName);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
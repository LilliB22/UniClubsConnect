package com.example.uniclubconnect;
import android.util.Base64;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.uniclubconnect.databinding.FragmentAddClubBinding;
import com.example.uniclubconnect.model.Club;
import com.example.uniclubconnect.security.Encrypt;
import com.example.uniclubconnect.security.RSA;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddClub extends Fragment {

    private FragmentAddClubBinding binding;
    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    private final List<String> categoryList = new ArrayList<>();
    private final List<String> studentList = new ArrayList<>();
    private final List<String> advisorList = new ArrayList<>();
    private final HashMap<String, String> studentNameToUID = new HashMap<>();
    private final HashMap<String, String> advisorNameToUID = new HashMap<>();

    public AddClub() {
        categoryList.add("Technology");
        categoryList.add("Sports");
        categoryList.add("Recreation");
        categoryList.add("Academic");
        categoryList.add("Greek");
        categoryList.add("Religion");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddClubBinding.inflate(inflater, container, false);

        setupCategorySpinner();
        loadStudentsIntoSpinners();
        loadAdvisorsIntoSpinners();

        binding.buttonAddClub.setOnClickListener(v -> addClub());

        return binding.getRoot();
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void loadStudentsIntoSpinners() {
        database.child("Users").get().addOnSuccessListener(snapshot -> {
            studentList.clear();
            studentList.add("Select Student");
            studentNameToUID.clear();

            for (DataSnapshot user : snapshot.getChildren()) {
                String name = user.child("name").getValue(String.class);
                String role = user.child("role").getValue(String.class);
                String uid = user.getKey();
                if ("student".equals(role) && name != null && uid != null) {
                    studentList.add(name);
                    studentNameToUID.put(name, uid);
                }
            }

            setSpinnerAdapter(binding.spinnerPresident, studentList);
            setSpinnerAdapter(binding.spinnerVice, studentList);
            setSpinnerAdapter(binding.spinnerTreasurer, studentList);
            setSpinnerAdapter(binding.spinnerSecretary, studentList);
            setSpinnerAdapter(binding.spinnerRelations, studentList);

        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Failed loading students: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void loadAdvisorsIntoSpinners() {
        database.child("Users").get().addOnSuccessListener(snapshot -> {
            advisorList.clear();
            advisorList.add("Select Advisor");
            advisorNameToUID.clear();

            for (DataSnapshot user : snapshot.getChildren()) {
                String name = user.child("name").getValue(String.class);
                String role = user.child("role").getValue(String.class);
                String uid = user.getKey();
                if ("advisor".equals(role) && name != null && uid != null) {
                    advisorList.add(name);
                    advisorNameToUID.put(name, uid);
                }
            }

            setSpinnerAdapter(binding.spinnerAdvisor1, advisorList);
            setSpinnerAdapter(binding.spinnerAdvisor2, advisorList);

        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Failed loading advisors: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void setSpinnerAdapter(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
    }

    private void addClub() {
        String clubId = database.child("Clubs").push().getKey();
        if (clubId == null) {
            Toast.makeText(requireContext(), "Failed to generate club ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String clubName = binding.clubNameInput.getText().toString();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String description = binding.descriptionInput.getText().toString();
        String time = binding.timeInput.getText().toString();
        String place = binding.placeInput.getText().toString();

        String president = studentNameToUID.getOrDefault(binding.spinnerPresident.getSelectedItem().toString(), "");
        String vice = studentNameToUID.getOrDefault(binding.spinnerVice.getSelectedItem().toString(), "");
        String treasurer = studentNameToUID.getOrDefault(binding.spinnerTreasurer.getSelectedItem().toString(), "");
        String secretary = studentNameToUID.getOrDefault(binding.spinnerSecretary.getSelectedItem().toString(), "");
        String relations = studentNameToUID.getOrDefault(binding.spinnerRelations.getSelectedItem().toString(), "");

        String advisor1 = advisorNameToUID.getOrDefault(binding.spinnerAdvisor1.getSelectedItem().toString(), "");
        String advisor2 = advisorNameToUID.getOrDefault(binding.spinnerAdvisor2.getSelectedItem().toString(), "");

        Club club = new Club(
                clubId, clubName, category, description,
                advisor1, advisor2, president, vice,
                treasurer, relations, secretary, time, place
        );

        // Generate AES key
        byte[] aesKey = Encrypt.generateAESKey();

        // Save club and distribute AES keys
        database.child("Clubs").child(clubId).setValue(club)
                .addOnSuccessListener(aVoid -> distributeAESKeys(clubId, aesKey, club))
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to add club: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void distributeAESKeys(String clubId, byte[] aesKey, Club club) {
        database.child("Users").get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot user : snapshot.getChildren()) {
                String userId = user.getKey();
                if (userId == null) continue;

                String role = user.child("role").getValue(String.class);
                boolean isMember = userId.equals(club.getPresident()) || userId.equals(club.getVice()) ||
                        userId.equals(club.getTreasurer()) || userId.equals(club.getSecretary()) ||
                        userId.equals(club.getRelations()) || userId.equals(club.getAdvisor1()) ||
                        userId.equals(club.getAdvisor2());

                if (!isMember) continue;

                // Fetch public key from Firebase
                String publicE = user.child("rsaE").getValue(String.class);
                String publicN = user.child("rsaN").getValue(String.class);
                if (publicE == null || publicN == null) continue;

                try {
                    BigInteger e = new BigInteger(publicE);
                    BigInteger n = new BigInteger(publicN);
                    BigInteger encryptedAESBigInt = RSA.encryptAESKey(aesKey, e, n);
                    String encryptedBase64 = Base64.encodeToString(encryptedAESBigInt.toByteArray(), Base64.NO_WRAP);

                    // Store under user's node
                    String node = "student".equals(role) ? "students" : "advisors";
                    database.child(node).child(userId).child("clubAESKeys").child(clubId)
                            .setValue(encryptedBase64);

                    // Store under club node for reference
                    database.child("Clubs").child(clubId).child("encryptedKeys").child(userId)
                            .setValue(encryptedBase64);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            updateUserPositions(clubId, club);
            Toast.makeText(requireContext(), "Club added successfully!", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Failed fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void updateUserPositions(String clubId, Club club) {
        database.child("Users").get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot user : snapshot.getChildren()) {
                String userId = user.getKey();
                if (userId == null) continue;

                String role = user.child("role").getValue(String.class);
                if ("student".equals(role)) {
                    if (userId.equals(club.getPresident()))
                        database.child("Users").child(userId).child("positions").child(clubId).setValue("president");
                    else if (userId.equals(club.getVice()))
                        database.child("Users").child(userId).child("positions").child(clubId).setValue("vice");
                    else if (userId.equals(club.getTreasurer()))
                        database.child("Users").child(userId).child("positions").child(clubId).setValue("treasurer");
                    else if (userId.equals(club.getSecretary()))
                        database.child("Users").child(userId).child("positions").child(clubId).setValue("secretary");
                    else if (userId.equals(club.getRelations()))
                        database.child("Users").child(userId).child("positions").child(clubId).setValue("relations");
                } else if ("advisor".equals(role)) {
                    if (userId.equals(club.getAdvisor1()) || userId.equals(club.getAdvisor2()))
                        database.child("Users").child(userId).child("advisorOf").child(clubId).setValue(club.getName());
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Failed updating user positions: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}

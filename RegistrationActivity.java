package com.example.uniclubconnect;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.example.uniclubconnect.databinding.ActivityRegistrationBinding;
import com.example.uniclubconnect.security.RSA;
import com.example.uniclubconnect.security.RSAKeyStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private ActivityRegistrationBinding binding;
    private FirebaseAuth auth;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        String[] roles = {"Student", "Advisor"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);

        binding.registerButton.setOnClickListener(view -> {
            String name = binding.NameEdittext.getText().toString().trim();
            String email = binding.emailEdittext.getText().toString().trim();
            String password = binding.passwordEdittext.getText().toString().trim();
            String role = binding.roleSpinner.getSelectedItem().toString().toLowerCase();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(name, email, password, role);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void registerUser(String name, String email, String password, String role) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();

                        // Generate RSA keypair
                        RSA.RSAKeyPair kp = RSA.generateKeyPair(2048);

                        // Save private exponent d locally
                        RSAKeyStorage.saveD(this, uid, kp.d.toString());

                        // Save public key (e, n) in Firebase
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("role", role);
                        userMap.put("rsaE", kp.e.toString());
                        userMap.put("rsaN", kp.n.toString());

                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(uid)
                                .setValue(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Database error.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
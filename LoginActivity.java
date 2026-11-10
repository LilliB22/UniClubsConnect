package com.example.uniclubconnect;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.uniclubconnect.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private com.example.uniclubconnect.databinding.ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(view -> {
            String email = binding.emailEdittext.getText().toString().trim();
            String password = binding.passwordEdittext.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });

        binding.goRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (auth.getCurrentUser() != null) {
                            String uid = auth.getCurrentUser().getUid();
                            DatabaseReference db = FirebaseDatabase.getInstance().getReference();

                            db.child("Users").child(uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Object roleObj = snapshot.child("role").getValue();
                                                if (roleObj != null) {
                                                    String role = roleObj.toString().toLowerCase();
                                                    switch (role) {
                                                        case "student":
                                                            startActivity(new Intent(LoginActivity.this, StudentActivity.class));
                                                            finish();
                                                            break;
                                                        case "advisor":
                                                            startActivity(new Intent(LoginActivity.this, AdvisorActivity.class));
                                                            finish();
                                                            break;
                                                        default:
                                                            Toast.makeText(LoginActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Role not defined for this user", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, "User authentication error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Friendly error messages for common auth exceptions
                        Exception ex = task.getException();
                        if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        } else if (ex instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, "No account found with this email", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + (ex != null ? ex.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
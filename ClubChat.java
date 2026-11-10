package com.example.uniclubconnect;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.uniclubconnect.databinding.FragmentClubChatBinding;
import com.example.uniclubconnect.model.Message;
import com.example.uniclubconnect.security.Decrypt;
import com.example.uniclubconnect.security.Encrypt;
import com.example.uniclubconnect.security.RSAKeyStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ClubChat extends Fragment {

    private FragmentClubChatBinding binding;
    private DatabaseReference chatRef;
    private ArrayList<Message> messageList;
    private MessageAdapter messageAdapter;
    private String clubId;
    private byte[] aesKey;

    private String uid;
    private String role;
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentClubChatBinding.inflate(inflater, container, false);

        if (getArguments() != null)
            clubId = getArguments().getString("clubId");

        if (clubId == null)
            throw new IllegalStateException("ClubChat: clubId is NULL");

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, uid);
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerChat.setAdapter(messageAdapter);

        chatRef = FirebaseDatabase.getInstance().getReference("clubChats").child(clubId);

        fetchUserRoleAndAESKey();

        return binding.getRoot();
    }
    private void fetchUsername() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.child("name").get().addOnSuccessListener(snapshot -> {
            username = snapshot.getValue(String.class);
            if (username == null || username.isEmpty()) username = "Unknown";
        }).addOnFailureListener(e -> username = "Unknown");
    }


    private void fetchUserRoleAndAESKey() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.child("role").get().addOnSuccessListener(snapshot -> {
            role = snapshot.getValue(String.class);
            if (role == null) role = "student";
            fetchAESKey();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to get user role: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );

    }

    private void fetchAESKey() {
        String node = "student".equals(role) ? "students" : "advisors";
        DatabaseReference keyRef = FirebaseDatabase.getInstance()
                .getReference(node)
                .child(uid)
                .child("clubAESKeys")
                .child(clubId);

        keyRef.get().addOnSuccessListener(snapshot -> {
            String encryptedBase64 = snapshot.getValue(String.class);
            if (encryptedBase64 == null) {
                Toast.makeText(getContext(), "AES key not found.", Toast.LENGTH_LONG).show();
                return;
            }

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            userRef.get().addOnSuccessListener(userSnap -> {
                try {
                    String nStr = userSnap.child("rsaN").getValue(String.class);
                    if (nStr == null) {
                        Toast.makeText(getContext(), "Missing modulus n in Firebase.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    BigInteger n = new BigInteger(nStr);

                    String dStr = RSAKeyStorage.getD(requireContext(), uid);
                    if (dStr == null) {
                        Toast.makeText(getContext(), "Private key not found locally.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    BigInteger d = new BigInteger(dStr);

                    byte[] encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP);
                    BigInteger encryptedAES = new BigInteger(1, encryptedBytes);

                    BigInteger decryptedAESBigInt = encryptedAES.modPow(d, n);
                    aesKey = fixAESKeyLength(decryptedAESBigInt.toByteArray());
                    fetchUsername();
                    binding.btnSend.setOnClickListener(v -> sendMessage());
                    loadMessages();

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Failed to decrypt AES key: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private byte[] fixAESKeyLength(byte[] keyBytes) {
        byte[] key = new byte[16];
        if (keyBytes.length >= 16) {
            System.arraycopy(keyBytes, keyBytes.length - 16, key, 0, 16);
        } else {
            System.arraycopy(keyBytes, 0, key, 16 - keyBytes.length, keyBytes.length);
        }
        return key;
    }

    private byte[] padMessage(byte[] message) {
        int padLength = 16 - (message.length % 16);
        byte[] padded = new byte[message.length + padLength];
        System.arraycopy(message, 0, padded, 0, message.length);
        return padded;
    }

    private void sendMessage() {
        String text = binding.txtMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        try {
            byte[] plainText = padMessage(text.getBytes(StandardCharsets.UTF_8));
            byte[][] roundKeys = Encrypt.KeySchedule(aesKey);
            byte[] encryptedText = Encrypt.AESEncrypt(roundKeys, plainText);
            String cipherBase64 = Base64.encodeToString(encryptedText, Base64.NO_WRAP);

            String msgId = chatRef.push().getKey();
            if (msgId == null) return;

            Message msg = new Message(msgId, uid,username, cipherBase64, System.currentTimeMillis());
            chatRef.child(msgId).setValue(msg);
            binding.txtMessage.setText("");

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to encrypt message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        chatRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Message msg = child.getValue(Message.class);
                    if (msg != null) {
                        try {
                            byte[][] roundKeys = Encrypt.KeySchedule(aesKey);
                            byte[] encryptedBytes = Base64.decode(msg.getMessage(), Base64.NO_WRAP);
                            byte[] decryptedText = Decrypt.AESDecrypt(roundKeys, encryptedBytes);
                            msg.setMessage(new String(decryptedText, StandardCharsets.UTF_8).trim());
                        } catch (Exception e) {
                            msg.setMessage("[Cannot decrypt message]");
                        }
                        messageList.add(msg);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty())
                    binding.recyclerChat.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
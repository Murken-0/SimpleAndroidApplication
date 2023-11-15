package ru.mirea.muravievvr.mireaproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yandex.mapkit.MapKitFactory;

import java.util.Objects;

import ru.mirea.muravievvr.mireaproject.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private ActivityLoginBinding binding;
    private final String MAPKIT_API_KEY = "d8cdd37d-1f9b-422b-a7e3-e571ff1b4708";
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.androidId.setText(getString(R.string.android_id_text, getAndroidID()));

        MapKitFactory.setApiKey(MAPKIT_API_KEY);

        FirebaseApp.initializeApp(LoginActivity.this);
        mAuth = FirebaseAuth.getInstance();

        binding.emailCreateAccountButton.setOnClickListener(v ->
                createAccount(binding.fieldEmail.getText().toString(), binding.fieldPassword.getText().toString()));

        binding.signOutButton.setOnClickListener(v -> signOut());

        binding.emailSignInButton.setOnClickListener(v ->
                signIn(binding.fieldEmail.getText().toString(), binding.fieldPassword.getText().toString()));

        binding.verButton.setOnClickListener(v -> {
            sendEmailVerification();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        });

        binding.goToAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private String getAndroidID() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            binding.status.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            binding.emailPasswordButtons.setVisibility(View.GONE);
            binding.emailPasswordFields.setVisibility(View.GONE);
            binding.signOutButton.setVisibility(View.VISIBLE);
            binding.verButton.setVisibility(View.VISIBLE);
            binding.verButton.setEnabled(!user.isEmailVerified());
            binding.signOutButton.setVisibility(View.VISIBLE);
            binding.goToAppButton.setVisibility(View.VISIBLE);
        }
        else {
            binding.status.setText(R.string.signed_out);
            binding.emailPasswordButtons.setVisibility(View.VISIBLE);
            binding.emailPasswordFields.setVisibility(View.VISIBLE);
            binding.signOutButton.setVisibility(View.GONE);
            binding.verButton.setVisibility(View.GONE);
            binding.goToAppButton.setVisibility(View.GONE);
        } }
    private void createAccount(String email, String password) {
        if (email == null || email.isEmpty()
            || password == null || password.isEmpty()) {
            Log.i(TAG, "Empty fields");
            Toast.makeText(LoginActivity.this, "Enter all details!", Toast.LENGTH_SHORT).show();
            return;
        }
        String hashedPassword = ShaHasher.hash(password);
        Log.d(TAG, "createAccount:" + email + " | " + hashedPassword);
        mAuth.createUserWithEmailAndPassword(email, hashedPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure");
                        Toast.makeText(LoginActivity.this, "Creation Failed",Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    } });

    }
    private void signIn(String email, String password) {
        String hashedPassword = ShaHasher.hash(password);
        Log.d(TAG, "signIn:" + email+ " | " + hashedPassword);
        mAuth.signInWithEmailAndPassword(email, hashedPassword).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithEmail:success:");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            }
            else {
                Log.w(TAG, "signInWithEmail:failure:", task.getException());
                Toast.makeText(LoginActivity.this, R.string.auth_failed,
                        Toast.LENGTH_SHORT).show(); updateUI(null);
            }
            if (!task.isSuccessful()) {
                binding.status.setText(R.string.auth_failed); }
        });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification() {
        binding.verButton.setEnabled(false);
        final FirebaseUser user = mAuth.getCurrentUser();
        Objects.requireNonNull(user).sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    binding.verButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                            "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
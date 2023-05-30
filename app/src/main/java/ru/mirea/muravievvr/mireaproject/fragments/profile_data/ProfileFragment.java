package ru.mirea.muravievvr.mireaproject.fragments.profile_data;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import ru.mirea.muravievvr.mireaproject.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private final String KEY = "8Ef7g539GHjnVn1K";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        loadProfileInfo();

        binding.buttonSave.setOnClickListener(view1 -> saveProfileInfo());

        return view;
    }

    private void saveProfileInfo() {
        String name = binding.editTextName.getText().toString();
        String login = binding.editTextLogin.getText().toString();
        String password = binding.editTextPassword.getText().toString();

        if (name.isEmpty() || login.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        String encryptedPassword = "";
        try {
            encryptedPassword = encryptPassword(password);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Name", name);
        editor.putString("Login", login);
        editor.putString("Password", encryptedPassword);
        editor.apply();

        Toast.makeText(getActivity(), "Profile information saved", Toast.LENGTH_SHORT).show();
    }

    private void loadProfileInfo() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("Name", "");
        String login = sharedPreferences.getString("Login", "");
        String encryptedPassword = sharedPreferences.getString("Password", "");

        String password = "";
        try {
            password = decryptPassword(encryptedPassword);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        binding.editTextName.setText(name);
        binding.editTextLogin.setText(login);
        binding.editTextPassword.setText(password);
    }

    private String encryptPassword(String password) throws Exception {
        byte[] keyBytes = KEY.getBytes("UTF-8");

        Key aesKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(password.getBytes("UTF-8"));

        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    private String decryptPassword(String encryptedPassword) throws Exception {
        byte[] keyBytes = KEY.getBytes("UTF-8");

        byte[] encryptedBytes = Base64.decode(encryptedPassword, Base64.DEFAULT);

        Key aesKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, "UTF-8");
    }
}
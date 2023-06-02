package ru.mirea.muravievvr.mireaproject.fragments.maps;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import ru.mirea.muravievvr.mireaproject.MapsActivity;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentMapsBinding;

public class MapsFragment extends Fragment {
    private FragmentMapsBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        Intent intent = new Intent(getContext(), MapsActivity.class);
        startActivity(intent);
        return view;
    }
}
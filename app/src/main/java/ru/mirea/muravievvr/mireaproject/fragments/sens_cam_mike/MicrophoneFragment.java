package ru.mirea.muravievvr.mireaproject.fragments.sens_cam_mike;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.mirea.muravievvr.mireaproject.R;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentCameraBinding;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentMicrophoneBinding;

public class MicrophoneFragment extends Fragment {

    private FragmentMicrophoneBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMicrophoneBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }
}
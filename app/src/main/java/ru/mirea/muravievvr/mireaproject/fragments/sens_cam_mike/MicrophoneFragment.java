package ru.mirea.muravievvr.mireaproject.fragments.sens_cam_mike;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import ru.mirea.muravievvr.mireaproject.R;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentCameraBinding;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentMicrophoneBinding;

public class MicrophoneFragment extends Fragment {
    private static final int REQUEST_CODE_PERMISSION = 200;
    private FragmentMicrophoneBinding binding;
    private MediaRecorder mediaRecorder;
    private Handler handler;
    private Runnable runnable;
    private boolean isRecording;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMicrophoneBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.buttonStart.setOnClickListener(v -> {
            int audioRecordPermissionStatus = ContextCompat.checkSelfPermission(requireActivity(),
                    android.Manifest.permission.RECORD_AUDIO);
            if (audioRecordPermissionStatus == PackageManager.PERMISSION_GRANTED) {
                binding.buttonStart.setEnabled(false);
                binding.buttonStop.setEnabled(true);
                isRecording = true;
                startRecording();
            } else {
                ActivityCompat.requestPermissions(requireActivity(), new String[] {
                        android.Manifest.permission.RECORD_AUDIO
                }, REQUEST_CODE_PERMISSION);
            }

        });

        binding.buttonStop.setEnabled(false);
        binding.buttonStop.setOnClickListener(v -> {
            binding.buttonStart.setEnabled(true);
            binding.buttonStop.setEnabled(false);
            isRecording = false;
            stopRecording();
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                updateVolume();
                if (isRecording) {
                    handler.postDelayed(this, 100);
                }
            }
        };

        return view;
    }

    private void startRecording() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/dev/null");

            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaRecorder.start();
            handler.postDelayed(runnable, 100);
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        handler.removeCallbacks(runnable);
        binding.textViewResult.setText("0 dB");
    }

    private void updateVolume() {
        if (mediaRecorder != null) {
            int amplitude = mediaRecorder.getMaxAmplitude();
            double volume = 20 * Math.log10((double) Math.abs(amplitude));
            binding.textViewResult.setText(String.format("%.1f dB", volume));
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }

        handler.removeCallbacks(runnable);
    }
}
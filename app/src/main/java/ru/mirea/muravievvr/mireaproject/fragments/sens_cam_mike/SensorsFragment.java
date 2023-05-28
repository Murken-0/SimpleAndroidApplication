package ru.mirea.muravievvr.mireaproject.fragments.sens_cam_mike;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ru.mirea.muravievvr.mireaproject.databinding.FragmentSensorsBinding;

public class SensorsFragment extends Fragment implements SensorEventListener {
    private static final int REQUEST_CODE_ACTIVITY_RECOGNITION = 1;
    private FragmentSensorsBinding binding;
    private SensorManager sensorManager;
    private Sensor countSensor;
    private int stepCount = 0;
    private int lastSeenStepCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSensorsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        int stepPermissionStatus = ContextCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.ACTIVITY_RECOGNITION);

        if (stepPermissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[] {
                    android.Manifest.permission.ACTIVITY_RECOGNITION
            }, REQUEST_CODE_ACTIVITY_RECOGNITION);
        }

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            Toast.makeText(requireActivity(), "Started Counting Steps", Toast.LENGTH_LONG).show();
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(requireActivity(), "Device not Compatible!", Toast.LENGTH_LONG).show();
        }

        binding.buttonReset.setOnClickListener(v -> {;
            stepCount = 0;
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (countSensor != null) {
            sensorManager.unregisterListener(this, countSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (lastSeenStepCount == 0) {
            lastSeenStepCount = (int) event.values[0];
        } else if ((int) event.values[0] - lastSeenStepCount > 0) {
            stepCount += ((int) event.values[0] - lastSeenStepCount);
            lastSeenStepCount = (int) event.values[0];
        }
        binding.textViewStepCount.setText("Шагов: " + stepCount);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
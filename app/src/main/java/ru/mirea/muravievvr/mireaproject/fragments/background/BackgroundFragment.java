package ru.mirea.muravievvr.mireaproject.fragments.background;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.mirea.muravievvr.mireaproject.databinding.FragmentBackgroundBinding;

public class BackgroundFragment extends Fragment {
    private FragmentBackgroundBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBackgroundBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.buttonToast.setOnClickListener(v1 -> {
            Toast toast = Toast.makeText(getActivity(),
                    "Дзынь!", Toast.LENGTH_SHORT);
            toast.show();
        });
        binding.buttonDownload.setOnClickListener(v1 -> {
            binding.buttonDownload.setEnabled(false);

            Toast toast = Toast.makeText(getActivity(),
                    "Загружаем видео...", Toast.LENGTH_LONG);
            toast.show();

            DownloadVideoTask task = new DownloadVideoTask(binding.videoView);
            String link = "https://archive.org/download/Rick_Astley_Never_Gonna_Give_You_Up/Rick_Astley_Never_Gonna_Give_You_Up.mp4";

            task.execute(link);
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class DownloadVideoTask extends AsyncTask<String, Void, Uri> {
        VideoView videoView;

        public DownloadVideoTask(VideoView videoView) {
            this.videoView = videoView;
        }

        protected Uri doInBackground(String... urls) {
            String videoUrl = urls[0];
            Uri videoUri = null;
            try {
                Thread.sleep(5000);
                URL url = new URL(videoUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                File videoFile = File.createTempFile("video", ".mp4");
                FileOutputStream output = new FileOutputStream(videoFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
                output.close();
                input.close();
                videoUri = Uri.fromFile(videoFile);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return videoUri;
        }

        protected void onPostExecute(Uri result) {
            videoView.setVideoURI(result);
            videoView.start();
        }
    }
}
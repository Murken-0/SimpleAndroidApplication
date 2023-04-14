package ru.mirea.muravievvr.mireaproject.ui;

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

import ru.mirea.muravievvr.mireaproject.R;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentBackgroundBinding;

public class BackgroundFragment extends Fragment {

    private FragmentBackgroundBinding binding;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public BackgroundFragment() {
        // Required empty public constructor
    }

    public static BackgroundFragment newInstance(String param1, String param2) {
        BackgroundFragment fragment = new BackgroundFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

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
            String link = "https://save4k.com/video.php?url=UUZGFkBYGkkRB1ZbRQYFFQpDHVJdVl4JUk5fAQELFlFWXx0QWgZQCRMOU0tVAgUKWlRLRVtLXFgGDg5UUF0ICwgAFANaX1IlW1doe00FIw5IdWR7AWpJB3Z6EAwUWQ0DFwUFSAJTDEhXVhRbU14JTCR-SV9-UVATb1VpAzAxX0tYdHkIYyZxPDUnYUN7EANSV1hGXXZ-WyZoCE4EQg1MU14PA14VEVoTEQFXD04MExUQU1YTQFxIEF5KUxYXCAVLXEEUC1tfAgVFD18PBFJDUyYDChNfVwQWWRUEUAMIXVxVQBdUcBFbS1FXVVcADRxXQ1xACFNMHFd0SlIQQglOD1QUXxBaXwdAEw4PAAVFDw8MRVBCXF1bFUQFBFRXUw0CH0RCFEVfBEAOC19XChUPBQBeFgd0VElREVtYF1lVDBRLU0YDURtFBxARD0tSEEAFEEMOBwMLF1UOCRAJCRAFAw8FBlQAUQJSUFEHAg9RXkcIRQ4EBAEIUQEADlBTQl5EUEIPVxUEUB4TXwAGB1NRU1EHFVYPeHchZXd/IUIQQEIEBgdVA1YGUkURQlNFAgsSWFRLRVtLXEAFe1MMQVZ7W0kXACVaBhBUIAtGU1BGVCIWXkZHUVwcV3RKUxQRDUpXSkFeQwEhQxYRFBcAdA4PDAAUAXZRV0tABXtEBBABWktJU0EVFlB2AhYQFwB0DwsVQ0JaUg94dhQHaXxdEzZ5e15qQD8KL14MWzd3RkYxXwILcHR/YE5fDk8IdBMvVkx8amUKD1IRYS8XVHZndConVDZCd1NgUU4uUwBCNlUtbnBNUXQfYxZnDw5ad1hEAR87MwldUkFpeDpbHloWFAVKU1RBDwtbRwclDg8XAHQOCERXcl5GFwt6CEEdBCYJElEXC3FCChZQdg8NC0ZRQA0CAxVCFVlBUF5Ydn8FJjsceUVrY3sOcitxX1oTBFNdBlc1FWFkeFNQQxxWAWYSPQF7QHULSyl1LEM1KC1gaF4nPygkWHIFS1JRNE4JWB0MIAB4VnhcVkk3QRYgI0N2VQk3BjNBdUcEQ3pRA1d-K1w1HQF9FwEi&sid=9c3bd780&title=Rick+Astley+-+Never+Gonna+Give+You+Up.mp4";
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
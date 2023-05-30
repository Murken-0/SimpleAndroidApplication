package ru.mirea.muravievvr.mireaproject.fragments.profile_data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import ru.mirea.muravievvr.mireaproject.databinding.FragmentFilesBinding;

public class FilesFragment extends Fragment {
    private FragmentFilesBinding binding;
    private boolean isWork;
    private static final int REQUEST_CODE_PERMISSION = 100;
    private Uri selectedUri = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.button.setEnabled(false);

        binding.fabSearchImage.setOnClickListener(v -> {
            if (checkPermissions()) {
                getPicture();
            }
        });

        binding.button.setOnClickListener(v -> {
            new ConvertTask().execute();
        });

        return view;
    }

    private void getPicture() {
        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (isWork) {
            galleryActivityResultLauncher.launch(intent);
        }
    }

    ActivityResultCallback<ActivityResult> callback = result -> {
        if ( result.getResultCode() == Activity.RESULT_OK) {
            selectedUri = result.getData().getData();
            try {
                binding.imageViewJPG.setImageBitmap(
                        MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedUri));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            binding.button.setEnabled(true);
        }
    };

    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), callback);

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(System.currentTimeMillis());
        File storageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
        if (!storageDir.exists())
            storageDir.mkdirs();
        File image = File.createTempFile(
                timeStamp,                   /* prefix */
                ".png",                     /* suffix */
                storageDir                   /* directory */
        );
        return image;
    }

    public void addPicToGallery(Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private boolean checkPermissions() {
        int storagePermissionStatus = ContextCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storagePermissionStatus == PackageManager.PERMISSION_GRANTED) {
            isWork = true;
            return true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[] {
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_CONTACTS,
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE_PERMISSION);
            return false;
        }
    }

    private class ConvertTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            File file = null;
            try {
                file = createImageFile();
                InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, file.getName());
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());

            Uri uri = getActivity().getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            getActivity().sendBroadcast(mediaScanIntent);
            return file.getName();
        }
        protected void onPostExecute(String result) {
            Toast.makeText(getActivity(), "Создан файл " + result, Toast.LENGTH_LONG).show();
        }
    }
}
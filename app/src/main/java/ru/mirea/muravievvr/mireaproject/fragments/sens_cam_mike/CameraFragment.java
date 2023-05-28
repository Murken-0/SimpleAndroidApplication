package ru.mirea.muravievvr.mireaproject.fragments.sens_cam_mike;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.mirea.muravievvr.mireaproject.MainActivity;
import ru.mirea.muravievvr.mireaproject.R;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentCameraBinding;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentSensorsBinding;

public class CameraFragment extends Fragment {
    private static final int REQUEST_CODE_PERMISSION = 100;
    private static boolean isWork;
    private FragmentCameraBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.buttonPhoto.setOnClickListener(view1 -> {
            if (checkPermissions()) {
                takePicture();
            }
        });

        return view;
    }

    private boolean checkPermissions() {
        int cameraPermissionStatus = ContextCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.CAMERA);
        int contactsWritePermissionStatus = ContextCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.WRITE_CONTACTS);
        int contactsReadPermissionStatus = ContextCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.READ_CONTACTS);
        int storagePermissionStatus = ContextCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (cameraPermissionStatus == PackageManager.PERMISSION_GRANTED &&
                contactsWritePermissionStatus == PackageManager.PERMISSION_GRANTED &&
                contactsReadPermissionStatus == PackageManager.PERMISSION_GRANTED &&
                storagePermissionStatus == PackageManager.PERMISSION_GRANTED) {
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

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (isWork) {
            cameraActivityResultLauncher.launch(takePictureIntent);
        }
    }

    ActivityResultCallback<ActivityResult> callback = result -> {
        if ( result.getResultCode() == getActivity().RESULT_OK) {
            Bundle extras = result.getData().getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            createContact(imageBitmap);
        }
    };

    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), callback);

    private void createContact(Bitmap imageBitmap) {
        try {
            String displayName = binding.editTextName.getText().toString();
            String phoneNumber = binding.editTextPhone.getText().toString();

            ContentResolver contentResolver = requireActivity().getContentResolver();

            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, displayName);

            Uri contactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
            long contactId = ContentUris.parseId(contactUri);

            contentValues.clear();
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
            contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);

            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
            contentValues.clear();
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            byte[] photoBytes = stream.toByteArray();

            Uri imageUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
            if (imageUri == null) {
                ContentValues values = new ContentValues();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
                values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);
            } else {
                AssetFileDescriptor fd = contentResolver.openAssetFileDescriptor(imageUri, "rw");
                OutputStream os = fd.createOutputStream();
                os.write(photoBytes);
                os.flush();
                os.close();
                fd.close();
            }
            Toast.makeText(requireActivity(),
                    "Контакт " + displayName + " успешно создан", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(requireActivity(),
                    "Ошибка в ходе создания", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
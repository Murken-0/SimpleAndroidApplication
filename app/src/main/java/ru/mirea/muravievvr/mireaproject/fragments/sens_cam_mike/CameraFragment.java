package ru.mirea.muravievvr.mireaproject.fragments.sens_cam_mike;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.mirea.muravievvr.mireaproject.R;
import ru.mirea.muravievvr.mireaproject.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private EditText numberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberTextView = findViewById(R.id.numberTextView);
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            createContact(imageBitmap);
        }
    }

    private void createContact(Bitmap imageBitmap) {
        String displayName = "Photo Contact";
        String phoneNumber = numberTextView.getText().toString();

        ContentResolver contentResolver = getContentResolver();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, displayName);

        Uri contactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        long contactId = ContentUris.parseId(contactUri);

        contentValues.clear();
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);

        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

        try {
            Uri imageUri = contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap scaledImage = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, scaledImage.getWidth(), scaledImage.getHeight(), false);

            contentResolver.delete(imageUri, null, null);

            OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
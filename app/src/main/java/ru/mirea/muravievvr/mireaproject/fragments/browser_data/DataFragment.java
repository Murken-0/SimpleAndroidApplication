package ru.mirea.muravievvr.mireaproject.fragments.browser_data;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ru.mirea.muravievvr.mireaproject.R;

public class DataFragment extends Fragment {
    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_data, container, false);
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(v1 -> {
            String url = "https://about.google/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        return v;
    }
}
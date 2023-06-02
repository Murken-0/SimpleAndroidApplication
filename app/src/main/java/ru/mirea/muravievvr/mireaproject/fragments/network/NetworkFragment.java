package ru.mirea.muravievvr.mireaproject.fragments.network;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.Locale;

import ru.mirea.muravievvr.mireaproject.databinding.FragmentNetworkBinding;

public class NetworkFragment extends Fragment {
    private FragmentNetworkBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNetworkBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        updateExchangeRate();
        binding.buttonUpdate.setOnClickListener(v -> updateExchangeRate());

        return view;
    }

    private void updateExchangeRate() {

        String url = "https://www.cbr.ru/scripts/XML_daily.asp";

        RequestQueue queue = Volley.newRequestQueue(requireActivity());

        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url, response -> {
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(new StringReader(response));

                        String curCode = "";
                        float rate = 1;
                        while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                            if (parser.getEventType() == XmlPullParser.START_TAG) {
                                String tagName = parser.getName();
                                if (tagName.equals("CharCode")) {
                                    curCode = parser.nextText();
                                } else if (tagName.equals("Value")) {
                                    rate = Float.parseFloat(parser.nextText().replace(",", "."));
                                }
                            }
                            if (parser.getEventType() == XmlPullParser.END_TAG) {
                                String tagName = parser.getName();
                                if (tagName.equals("Valute")) {
                                    switch (curCode) {
                                        case "USD":
                                            binding.usdTextView.setText("Доллар: "
                                                    + String.format(Locale.getDefault(), "%.2f", rate));
                                            break;
                                        case "EUR":
                                            binding.eurTextView.setText("Евро: " +
                                                    String.format(Locale.getDefault(), "%.2f", rate));
                                            break;
                                        case "CNY":
                                            binding.cnyTextView.setText("Юань: " +
                                                    String.format(Locale.getDefault(), "%.2f", rate));
                                            break;
                                    }
                                }
                            }
                            parser.next();
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireActivity(), "Error parsing XML", Toast.LENGTH_SHORT).show();
                        Log.e("InternetFragment", "Error parsing XML: " + e.getMessage());
                    }
                }, error -> {
            Toast.makeText(requireActivity(), "Error downloading exchange rate", Toast.LENGTH_SHORT).show();
            Log.e("InternetFragment", "Error downloading exchange rate: " + error.getMessage());
        });

        queue.add(stringRequest);
    }
}
package ru.mirea.muravievvr.mireaproject.fragments.browser_data;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.mirea.muravievvr.mireaproject.R;

public class BrowserFragment extends Fragment {
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);
        webView = (WebView) view.findViewById(R.id.webView);
        webView.loadUrl("https://www.google.com/");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        return view;
    }
}
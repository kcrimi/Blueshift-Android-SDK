package com.blueshift.inappmessage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blueshift.BlueshiftLogger;
import com.blueshift.model.Configuration;
import com.blueshift.util.BlueshiftUtils;

public class InAppMessageViewHTML extends InAppMessageView {
    private static final String TAG = InAppMessageViewHTML.class.getSimpleName();
    private static final String CONTENT_HTML = "html";

    public InAppMessageViewHTML(Context context, InAppMessage inAppMessage) {
        super(context, inAppMessage);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View getView(InAppMessage inAppMessage) {
        String htmlContent = inAppMessage.getContentString(CONTENT_HTML);
        if (!TextUtils.isEmpty(htmlContent)) {
            WebView webView = new WebView(getContext());

            // taking consent from dev to enable js
            Configuration config = BlueshiftUtils.getConfiguration(getContext());
            if (config != null && config.isInAppEnableJavascript()) {
                webView.getSettings().setJavaScriptEnabled(true);
            }

            webView.setWebViewClient(new InAppWebViewClient());
            webView.loadData(htmlContent, "text/html; charset=UTF-8", null);

            // Note: For WebView, the parent's size depends on the size of WebView, so we are
            // using the same dimensions of the template for this view.
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    inAppMessage.getTemplateWidth(getContext()),
                    inAppMessage.getTemplateHeight(getContext()));

            webView.setLayoutParams(lp);

            return webView;
        }

        return null;
    }

    private void launchUri(Uri uri) {
        if (uri != null) {
            Log.d(TAG, "URL: " + uri.toString());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            getContext().startActivity(intent);
        }
    }

    private class InAppWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri uri = request.getUrl();
                    launchUri(uri);
                }
            } catch (Exception e) {
                BlueshiftLogger.e(TAG, e);
            }

            onDismiss(getInAppMessage());
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Uri uri = Uri.parse(url);
                launchUri(uri);
            } catch (Exception e) {
                BlueshiftLogger.e(TAG, e);
            }

            onDismiss(getInAppMessage());
            return true;
        }
    }
}
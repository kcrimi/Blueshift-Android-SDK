package com.blueshift.inappmessage;

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

public class InAppMessageHtmlView extends InAppMessageView {
    private static final String TAG = InAppMessageHtmlView.class.getSimpleName();
    private static final String CONTENT_HTML = "html";

    public InAppMessageHtmlView(Context context, InAppMessage inAppMessage) {
        super(context, inAppMessage);
    }

    @Override
    public View getView(InAppMessage inAppMessage) {
        String htmlContent = inAppMessage.getContentString(CONTENT_HTML);
        if (!TextUtils.isEmpty(htmlContent)) {
            WebView webView = new WebView(getContext());
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
package com.blueshift.inappmessage;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blueshift.util.CommonUtils;
import com.blueshift.util.InAppUtils;

import org.json.JSONObject;

public class InAppMessageCenterPopupView extends InAppMessageView {
    public InAppMessageCenterPopupView(Context context, InAppMessage inAppMessage) {
        super(context, inAppMessage);
    }

    @Override
    public View getView(InAppMessage inAppMessage) {
        int dp80 = CommonUtils.dpToPx(80, getContext());

        LinearLayout rootView = new LinearLayout(getContext());
        rootView.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (InAppUtils.isTemplateFullScreen(inAppMessage)) {
            lp2.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        rootView.setLayoutParams(lp2);

        // title
        TextView titleTextView = getContentTextView(inAppMessage, CONTENT_TITLE);
        if (titleTextView != null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = InAppUtils.getContentLayoutGravity(inAppMessage, CONTENT_TITLE);
            rootView.addView(titleTextView, lp);
        }

        // message
        TextView messageTextView = getContentTextView(inAppMessage, CONTENT_MESSAGE);
        if (messageTextView != null) {
            messageTextView.setMinHeight(dp80);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // message window will take the full screen
            if (InAppUtils.isTemplateFullScreen(inAppMessage)) {
                lp.height = 0;
                lp.weight = 1;
            }

            lp.gravity = InAppUtils.getContentLayoutGravity(inAppMessage, CONTENT_MESSAGE);
            rootView.addView(messageTextView, lp);
        }

        // action
        JSONObject actions = inAppMessage.getAction();
        LinearLayout actionsLayout = getActionButtons(actions, LinearLayout.HORIZONTAL);
        if (actionsLayout != null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rootView.addView(actionsLayout, lp);
        }

        return rootView;
    }
}

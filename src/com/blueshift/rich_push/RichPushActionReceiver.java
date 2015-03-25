package com.blueshift.rich_push;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.blueshift.Blueshift;
import com.blueshift.model.Configuration;

/**
 * Created by rahul on 25/2/15.
 */
public class RichPushActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Message message = (Message) intent.getSerializableExtra(RichPushConstants.EXTRA_MESSAGE);
        if (action != null && message != null) {
            if (action.equals(RichPushConstants.ACTION_VIEW(context))) {
                displayProductPage(context, message);
            } else if (action.equals(RichPushConstants.ACTION_BUY(context))) {
                addToCart(context, message);
            } else if (action.equals(RichPushConstants.ACTION_OPEN_CART(context))) {
                displayCartPage(context, message);
            } else if (action.equals(RichPushConstants.ACTION_OPEN_OFFER_PAGE(context))) {
                displayOfferDisplayPage(context, message);
            }

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(intent.getIntExtra(RichPushConstants.EXTRA_NOTIFICATION_ID, 0));

            Blueshift.getInstance(context).trackNotificationClick(message.getId());

            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }

    protected void displayProductPage(Context context, Message message) {
        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null && configuration.getProductPage() != null) {
            Intent pageLauncherIntent = new Intent(context, configuration.getProductPage());
            pageLauncherIntent.putExtra("sku", message.getSku());
            pageLauncherIntent.putExtra("mrp", message.getMrp());
            pageLauncherIntent.putExtra("price", message.getPrice());
            pageLauncherIntent.putExtra("data", message.getData());
            pageLauncherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pageLauncherIntent);

            trackAppOpen(context, message.getId());
        }
    }

    protected void addToCart(Context context, Message message) {
        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null && configuration.getCartPage() != null) {
            Intent pageLauncherIntent = new Intent(context, configuration.getCartPage());
            pageLauncherIntent.putExtra("sku", message.getSku());
            pageLauncherIntent.putExtra("mrp", message.getMrp());
            pageLauncherIntent.putExtra("price", message.getPrice());
            pageLauncherIntent.putExtra("data", message.getData());
            pageLauncherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pageLauncherIntent);

            trackAppOpen(context, message.getId());
        }
    }

    protected void displayCartPage(Context context, Message message) {
        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null && configuration.getCartPage() != null) {
            Intent pageLauncherIntent = new Intent(context, configuration.getCartPage());
            pageLauncherIntent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
            pageLauncherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pageLauncherIntent);

            trackAppOpen(context, message.getId());
        }
    }

    protected void displayOfferDisplayPage(Context context, Message message) {
        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null && configuration.getOfferDisplayPage() != null) {
            Intent pageLauncherIntent = new Intent(context, configuration.getOfferDisplayPage());
            pageLauncherIntent.putExtra("image_url", message.getImage_url());
            pageLauncherIntent.putExtra("data", message.getData());
            pageLauncherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pageLauncherIntent);

            trackAppOpen(context, message.getId());
        }
    }

    protected void trackAppOpen(Context context, String pushId) {
        Blueshift.getInstance(context).trackNotificationPageOpen(pushId);
    }
}

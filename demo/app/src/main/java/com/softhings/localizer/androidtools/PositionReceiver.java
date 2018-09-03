package com.softhings.localizer.com.softhings.localizer.androidtools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.softhings.localizer.com.softhings.localizer.activities.MainActivity;

public class PositionReceiver extends BroadcastReceiver {

    public PositionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.softhings.localizer")) {

            String position = intent.getStringExtra("position");
            Intent positionIntent = new Intent(context, MainActivity.class);
            positionIntent.putExtra("position", position);
            positionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(positionIntent);
        }
    }
}

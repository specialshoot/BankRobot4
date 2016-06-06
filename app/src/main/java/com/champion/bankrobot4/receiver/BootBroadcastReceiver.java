package com.champion.bankrobot4.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.champion.bankrobot4.MainActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION="android.intent.action.BOOT_COMPLETED";
    public BootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION)){
            Intent sayHelloIntent=new Intent(context, MainActivity.class);
            sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sayHelloIntent);
        }
    }
}

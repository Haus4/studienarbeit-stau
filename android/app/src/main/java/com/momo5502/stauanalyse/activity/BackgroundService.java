package com.momo5502.stauanalyse.activity;

import android.app.IntentService;
import android.content.Intent;

public class BackgroundService extends IntentService {
    public BackgroundService() {
        super("Stau - BackgroundService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        String dataString = workIntent.getDataString();
        System.out.println(dataString);
    }
}
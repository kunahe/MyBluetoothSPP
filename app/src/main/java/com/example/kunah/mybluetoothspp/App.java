package com.example.kunah.mybluetoothspp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

public class App extends Application {
    private static final Handler sHandler = new Handler();
    private static Toast sToast;

    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    public static void toast(String txt, int duration) {
        sToast.setText(txt);
        sToast.setDuration(duration);
        sToast.show();
    }

    public static void runUI(Runnable runnable) {
        sHandler.post(runnable);
    }

}
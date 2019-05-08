package ru.dudin.instgclient;

import android.app.Application;

public class ClientApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferencesModule.createInstance(this);
        AuthModule.createInstance();
    }
}
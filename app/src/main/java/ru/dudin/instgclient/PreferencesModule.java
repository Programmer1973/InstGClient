package ru.dudin.instgclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesModule {

    private static PreferencesModule sInstance;

    private static SharedPreferences sharedPref;
    private static Editor editor;

    private static final String PREFERENCES_FILENAME = "preferences.dat";
    private static final String API_ACCESS_TOKEN = "access_token";
    private static final String API_ID = "id";
    private static final String API_USER_NAME = "user_name";
    private static final String API_USER_FULL_NAME = "user_full_name";
    private static final String API_USER_PHOTO = "user_photo";


 	static void createInstance(final Context context) {
    	sInstance = new PreferencesModule(context);
    }

 	static PreferencesModule getInstance() {
 		return sInstance;
    }

    private PreferencesModule(Context context) {
        sharedPref = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    void storeData(String accessToken, String id, String userName, String userFullName, String userPhoto) {
        editor.putString(API_ACCESS_TOKEN, accessToken);
 	    editor.putString(API_ID, id);
        editor.putString(API_USER_NAME, userName);
        editor.putString(API_USER_FULL_NAME, userFullName);
        editor.putString(API_USER_PHOTO, userPhoto);
        editor.commit();
    }

    public void storeAccessToken(String accessToken) {
        editor.putString(API_ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    public void resetData() {
        editor.putString(API_ACCESS_TOKEN, null);
        editor.putString(API_ID, null);
        editor.putString(API_USER_NAME, null);
        editor.putString(API_USER_FULL_NAME, null);
        editor.putString(API_USER_PHOTO, null);
        editor.commit();
    }

    String getAccessToken() {
        return sharedPref.getString(API_ACCESS_TOKEN, null);
    }

    public String getId() {
        return sharedPref.getString(API_ID, null);
    }

    public String getUserName() {
        return sharedPref.getString(API_USER_NAME, null);
    }

    String getUserFullName() {
        return sharedPref.getString(API_USER_FULL_NAME, null);
    }

    String getUserPhoto() {
        return sharedPref.getString(API_USER_PHOTO, null);
    }
}
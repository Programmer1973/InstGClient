package ru.dudin.instgclient;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import ru.dudin.instgclient.fragments.LoginFragment;
import ru.dudin.instgclient.data.InstagramData;
import ru.dudin.instgclient.data.UserData;

public class AuthModule implements LoginFragment.RequestTokenListener {

    private static final String TAG = "AuthModule";

    private static AuthModule sInstance;

    private PreferencesModule mPreferencesModule;

    private static String mAccessToken;

    private final Set<Listener> mListeners = Sets.newHashSet();
    private State mState = State.NOT_INITIALIZED;

    private static String urlString;
    private List<JSONObject> mUserMediaEndpoints = new ArrayList<>();

    static void createInstance() {
        sInstance = new AuthModule();
    }

    static AuthModule getInstance() {
        return sInstance;
    }

    private AuthModule() {}

    void setParameter(PreferencesModule preferencesModule) {
        mPreferencesModule = preferencesModule;
        mPreferencesModule.resetData();
        changeState(State.NOT_AUTHENTICATED);
    }

    @SuppressLint("RestrictedApi")
    private void changeState(final State newState) {
        Preconditions.checkState(mState != newState, "New state is equal to old state: " + newState);

        Log.d(TAG, String.format("changeState: %s -> %s", mState.toString(), newState.toString()));
        mState = newState;
        for (final Listener listener : mListeners) {
            listener.onStateChanged(mState);
        }
    }

    void addListener(final Listener listener) {
        Preconditions.checkState(!mListeners.contains(listener));
        mListeners.add(listener);
        listener.onStateChanged(mState);
    }

    void removeListener(final Listener listener) {
        Preconditions.checkState(mListeners.contains(listener));
        mListeners.remove(listener);
    }

    void logout() {
        mPreferencesModule.resetData();
        mAccessToken = null;
        android.webkit.CookieManager.getInstance().removeAllCookies(null);
        changeState(State.NOT_AUTHENTICATED);
    }

    public State getState() {
        return mState;
    }

    @Override
    public void onLoadRequestTokenSuccessfully(String requestToken) {
        for (final Listener listener : mListeners) {
            listener.onLoadRequestTokenSuccessfully();
        }
        new LoadAccessTokenAsyncTask().execute(requestToken); // <- маркер запроса / request_token
    }

    @Override
    public void onLoadRequestTokenFailed(String description) {
        for (final Listener listener : mListeners) {
            listener.onLoadRequestTokenFailed(description);
        }
    }

    public enum State {
        NOT_INITIALIZED, AUTHENTICATED, NOT_AUTHENTICATED
    }

    public interface Listener {
        void onStateChanged(State state);

        void onLoadRequestTokenSuccessfully();
        void onLoadRequestTokenFailed(String str);

        void onLoadAccessTokenSuccessfully();
        void onLoadAccessTokenFailed(Throwable t);

        void onLoadUserMediaEndpointsSuccessfully();
        void onLoadUserMediaEndpointsFailed(Throwable t);
    }

    //===========================================================================
    private class LoadAccessTokenAsyncTask extends AsyncTask<String, Void, Void> {

        private volatile Throwable mLoadAccessTokenError;

        @Override
        protected Void doInBackground(String... params) {

            try {
                String mTokenUrl = InstagramData.API_TOKEN_URL
                                + "?client_id="
                                + UserData.CLIENT_ID
                                + "&client_secret="
                                + UserData.CLIENT_SECRET
                                + "&redirect_uri="
                                + UserData.CALLBACK_URL
                                + "&grant_type=authorization_code";

                URL url = new URL(mTokenUrl);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());

                writer.write("client_id="
                        + UserData.CLIENT_ID
                        + "&client_secret="
                        + UserData.CLIENT_SECRET
                        + "&grant_type=authorization_code"
                        + "&redirect_uri="
                        + UserData.CALLBACK_URL
                        + "&code="
                        + params[0]); // <- маркер запроса / request_token

                writer.flush();
                String response = streamToString(urlConnection.getInputStream());
                JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();

                mAccessToken = jsonObj.getString("access_token"); // <- маркер доступа / access token
                String id = jsonObj.getJSONObject("user").getString("id");
                String userName = jsonObj.getJSONObject("user").getString("username");
                String fullName = jsonObj.getJSONObject("user").getString("full_name");
                String userPhoto = jsonObj.getJSONObject("user").getString("profile_picture");

                mPreferencesModule.storeData(mAccessToken, id, userName, fullName, userPhoto);

                urlString = InstagramData.API_URL
                        + "/users/"
                        + mPreferencesModule.getId()
                        + "/media/recent/?access_token="
                        + mPreferencesModule.getAccessToken();

            } catch (Exception e) {
                e.printStackTrace();
                mLoadAccessTokenError = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mLoadAccessTokenError == null) {
                for (final Listener listener : mListeners) {
                    listener.onLoadAccessTokenSuccessfully();
                }
                    new LoadUserMediaEndpointsAsyncTask().execute(urlString);
//                }
            } else {
                for (final Listener listener : mListeners) {
                    listener.onLoadAccessTokenFailed(mLoadAccessTokenError);
                }
            }
        }
    }

    //===========================================================================
    private class LoadUserMediaEndpointsAsyncTask extends AsyncTask<String, Void, Void> {

        private volatile Throwable mLoadError;

        @Override
        protected Void doInBackground(String... params) {

            try {
                URL url = new URL(params[0]); // <- urlString

                InputStream inputStream = url.openConnection().getInputStream();
                String response = streamToString(inputStream);

                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int index = 0; index < jsonArray.length(); index++) {
                    mUserMediaEndpoints.add(jsonArray.getJSONObject(index));
                }

            } catch (Exception e) {
                e.printStackTrace();
                mLoadError = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mLoadError == null) {
                for (final Listener listener : mListeners) {
                    listener.onLoadUserMediaEndpointsSuccessfully();
                }
            } else {
                for (final Listener listener : mListeners) {
                    listener.onLoadUserMediaEndpointsFailed(mLoadError);
                }
            }

            changeState(State.AUTHENTICATED);
        }
    }

    private String streamToString(InputStream is) throws IOException {
        String string = "";

        if (is != null) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            string = stringBuilder.toString();
        }
        return string;
    }

    public List<JSONObject> getUserMediaEndpoints() {
        return mUserMediaEndpoints;
    }
}
package ru.dudin.instgclient.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import ru.dudin.instgclient.AuthModule;
import ru.dudin.instgclient.R;
import ru.dudin.instgclient.data.InstagramData;
import ru.dudin.instgclient.data.UserData;

@SuppressWarnings("ConstantConditions")
public class LoginFragment extends Fragment {

    private AuthModule mAuthModule;
    private RequestTokenListener mRequestTokenListener;

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private static final String TAG = "Instagram-WebView";

    public void setParameter(AuthModule authModule) {
        mAuthModule = authModule;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestTokenListener = (RequestTokenListener) mAuthModule;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mWebView = view.findViewById(R.id.view_web);
        setUpWebView();

        mProgressBar = view.findViewById(R.id.login_progress_bar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mRequestTokenListener = (RequestTokenListener) mAuthModule;
    }

    private void setUpWebView() {
        mWebView.setWebViewClient(new AuthWebViewClient());

        mWebView.canGoBack();
        mWebView.goBack();

        mWebView.clearCache(true);
        mWebView.clearHistory();

        WebSettings mWebSettings = mWebView.getSettings();
        mWebSettings.setSaveFormData(false);
        mWebSettings.setJavaScriptEnabled(true);
        String mAuthUrl = InstagramData.API_AUTH_URL
                        + "?client_id="
                        + UserData.CLIENT_ID
                        + "&redirect_uri="
                        + UserData.CALLBACK_URL
                        + "&response_type=code&display=touch&scope=likes+comments+relationships";
        mWebView.loadUrl(mAuthUrl);
    }

    public class AuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.startsWith(UserData.CALLBACK_URL)) {
                String urls[] = url.split("=");
                mRequestTokenListener.onLoadRequestTokenSuccessfully(urls[1]); // <- маркер запроса / request_token

                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            Log.d(TAG, "Page error: " + description);
            mRequestTokenListener.onLoadRequestTokenFailed(description);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            Log.d(TAG, "Loading URL: " + url);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Log.d(TAG, "onPageFinished URL: " + url);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    public void goBack() {
        mWebView.goBack();
    }

    public interface RequestTokenListener{
        void onLoadRequestTokenSuccessfully(String requestToken);
        void onLoadRequestTokenFailed(String error);
    }
}
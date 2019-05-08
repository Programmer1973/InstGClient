package ru.dudin.instgclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import ru.dudin.instgclient.fragments.LoginFragment;
import ru.dudin.instgclient.fragments.PostsFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private final PreferencesModule mPreferencesModule = PreferencesModule.getInstance();
    private final AuthModule mAuthModule = AuthModule.getInstance();

    private final AuthModule.Listener mAuthListener = new AuthModule.Listener() {

        @SuppressLint("RestrictedApi")
        @Override
        public void onStateChanged(final AuthModule.State state) {
            switch (state) {
                case NOT_INITIALIZED:
                    mUserImageView.setVisibility(View.GONE);
                    mUserFullNameTextView.setVisibility(View.GONE);
                    mNotAuthenticatedTextView.setVisibility(View.VISIBLE);
                    mLogoutMenuItem.setVisible(false);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mHeaderView.setBackgroundResource(R.drawable.side_nav_bar);

                    mAuthModule.setParameter(mPreferencesModule);
                    break;

                case NOT_AUTHENTICATED:
                    mUserImageView.setVisibility(View.GONE);
                    mUserFullNameTextView.setVisibility(View.GONE);
                    mNotAuthenticatedTextView.setVisibility(View.VISIBLE);
                    mLogoutMenuItem.setVisible(false);
                    mProgressBar.setVisibility(View.GONE);
                    mHeaderView.setBackgroundResource(R.drawable.side_nav_bar);

                    LoginFragment mLoginFragment = new LoginFragment();
                    mLoginFragment.setParameter(mAuthModule);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.view_content_container, mLoginFragment)
                            .replace(R.id.view_content_container, mLoginFragment)
                            .commit();
                    break;

                case AUTHENTICATED:
                    mUserImageView.setVisibility(View.VISIBLE);
                    mUserFullNameTextView.setVisibility(View.VISIBLE);
                    mNotAuthenticatedTextView.setVisibility(View.GONE);
                    mLogoutMenuItem.setVisible(true);
                    mProgressBar.setVisibility(View.GONE);
                    mHeaderView.setBackgroundResource(R.drawable.side_nav_bar_auth);

                    setUserData();

                    PostsFragment mPostsFragment = new PostsFragment();
                    mPostsFragment.setParameter(mAuthModule);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.view_content_container, mPostsFragment)
                            .commit();
                    break;
            }
        }

        @Override
        public void onLoadRequestTokenSuccessfully() {
            // stub
        }

        @Override
        public void onLoadRequestTokenFailed(String description) {
            Toast.makeText(MainActivity.this, R.string.authorization_failure, Toast.LENGTH_SHORT).show();
            Log.e(TAG, String.format("Authorization failed, %s", description));
        }

        @Override
        public void onLoadAccessTokenSuccessfully() {
            // stub
        }

        @Override
        public void onLoadAccessTokenFailed(Throwable t) {
            Toast.makeText(MainActivity.this, R.string.access_token_failure, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to get access token", t);
        }

        @Override
        public void onLoadUserMediaEndpointsSuccessfully() {
            // stub
        }

        @Override
        public void onLoadUserMediaEndpointsFailed(Throwable t) {
            Toast.makeText(MainActivity.this, R.string.user_information_failure, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to get user information", t);
        }
    };

    private DrawerLayout mDrawerLayout;

    private ImageView mUserImageView;
    private TextView mUserFullNameTextView;

    private TextView mNotAuthenticatedTextView;
    private MenuItem mLogoutMenuItem;
    private ProgressBar mProgressBar;
    private View mHeaderView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.INTERNET},
                    123
            );
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.view_drawer_layout);
        mProgressBar = (ProgressBar) findViewById(R.id.view_progress_bar);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.view_toolbar);
        setSupportActionBar(toolbar);

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.view_navigation);
        navigationView.setNavigationItemSelectedListener(this);

        mHeaderView = navigationView.getHeaderView(0);

        mUserImageView = (ImageView) mHeaderView.findViewById(R.id.user_image_view);
        mUserFullNameTextView = (TextView) mHeaderView.findViewById(R.id.view_full_username);

        mNotAuthenticatedTextView = (TextView) mHeaderView.findViewById(R.id.view_not_authenticated);
        mLogoutMenuItem = navigationView.getMenu().findItem(R.id.nav_logout);
    }

    private void setUserData() {
        Picasso.get().load(mPreferencesModule.getUserPhoto()).into(mUserImageView);
        mUserFullNameTextView.setText(mPreferencesModule.getUserFullName());
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        mAuthModule.addListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mAuthModule.removeListener(mAuthListener);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.nav_logout) {
            mAuthModule.logout();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.view_content_container);

        if (loginFragment.canGoBack()) {
            loginFragment.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
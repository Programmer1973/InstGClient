package ru.dudin.instgclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ru.dudin.instgclient.fragments.PostsFragment;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.view_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView mImageView = findViewById(R.id.picture_view_image);

        Bundle messageBundle = getIntent().getExtras();

        if (messageBundle == null)
            return;

        String imageUrl = messageBundle.getString(PostsFragment.MESSAGE_EXTRA_IMAGE_URL_KEY);

        Picasso
                .get()
                .load(imageUrl)
                .placeholder(R.drawable.my_place_holder)
                .fit()
                .into(mImageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}